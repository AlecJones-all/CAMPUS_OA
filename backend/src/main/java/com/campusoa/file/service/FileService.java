package com.campusoa.file.service;

import com.campusoa.security.AuthenticatedUser;
import com.campusoa.system.exception.SystemException;
import com.campusoa.workflow.service.WorkflowService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileService {

    private static final String WORKFLOW_APPLICATION = "WORKFLOW_APPLICATION";

    private final JdbcTemplate jdbcTemplate;
    private final WorkflowService workflowService;

    public FileService(JdbcTemplate jdbcTemplate, WorkflowService workflowService) {
        this.jdbcTemplate = jdbcTemplate;
        this.workflowService = workflowService;
    }

    public List<Map<String, Object>> listFiles(AuthenticatedUser currentUser, String businessType, Long businessId) {
        if (businessType == null || businessType.isBlank() || businessId == null) {
            ensureAdmin(currentUser);
            return queryFiles(null, null);
        }
        ensureReadable(currentUser, businessType, businessId);
        return queryFiles(businessType.trim(), businessId);
    }

    @Transactional
    public Long upload(AuthenticatedUser currentUser, String businessType, Long businessId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new SystemException("请选择要上传的附件");
        }
        if (businessType == null || businessType.isBlank() || businessId == null) {
            throw new SystemException("附件归属不能为空");
        }

        String normalizedBusinessType = businessType.trim();
        ensureWritable(currentUser, normalizedBusinessType, businessId);

        Path uploadDir = resolveUploadDir(normalizedBusinessType);
        String originalFilename = file.getOriginalFilename() == null ? "attachment.bin" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String storedFileName = UUID.randomUUID().toString().replace("-", "") + "-" + originalFilename;
        Path target = uploadDir.resolve(storedFileName);

        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new SystemException("附件保存失败");
        }

        jdbcTemplate.update("""
                        INSERT INTO file_attachment (
                            business_type, business_id, file_name, file_path, file_size, content_type, uploaded_by, deleted_flag
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, 0)
                        """,
                normalizedBusinessType,
                businessId,
                originalFilename,
                target.toAbsolutePath().toString(),
                file.getSize(),
                file.getContentType(),
                currentUser.userId()
        );
        Number key = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Number.class);
        if (key == null) {
            throw new SystemException("附件上传失败");
        }
        return key.longValue();
    }

    public ResponseEntity<ByteArrayResource> download(AuthenticatedUser currentUser, Long attachmentId) {
        Attachment attachment = findAttachment(attachmentId);
        ensureReadable(currentUser, attachment.businessType(), attachment.businessId());

        Path filePath = Path.of(attachment.filePath());
        if (!Files.exists(filePath)) {
            throw new SystemException("附件文件不存在");
        }

        try {
            byte[] bytes = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(bytes);
            String encodedFilename = URLEncoder.encode(attachment.fileName(), StandardCharsets.UTF_8).replace("+", "%20");
            MediaType mediaType = attachment.contentType() == null || attachment.contentType().isBlank()
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(attachment.contentType());
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename(encodedFilename, StandardCharsets.UTF_8).build().toString())
                    .contentLength(bytes.length)
                    .body(resource);
        } catch (IOException exception) {
            throw new SystemException("附件读取失败");
        }
    }

    @Transactional
    public void delete(AuthenticatedUser currentUser, Long attachmentId) {
        Attachment attachment = findAttachment(attachmentId);
        boolean isAdmin = isAdmin(currentUser);
        boolean isUploader = Objects.equals(currentUser.userId(), attachment.uploadedBy());
        ensureReadable(currentUser, attachment.businessType(), attachment.businessId());
        if (!isAdmin && !isUploader) {
            throw new SystemException("仅上传人或管理员可删除附件");
        }

        jdbcTemplate.update("""
                        UPDATE file_attachment
                        SET deleted_flag = 1, updated_at = NOW()
                        WHERE id = ?
                        """,
                attachmentId
        );

        try {
            Files.deleteIfExists(Path.of(attachment.filePath()));
        } catch (IOException ignored) {
        }
    }

    private List<Map<String, Object>> queryFiles(String businessType, Long businessId) {
        StringBuilder sql = new StringBuilder("""
                SELECT f.id,
                       f.business_type,
                       f.business_id,
                       f.file_name,
                       f.file_size,
                       f.content_type,
                       f.uploaded_by,
                       u.real_name AS uploaded_by_name,
                       f.created_at,
                       f.updated_at
                FROM file_attachment f
                JOIN sys_user u ON u.id = f.uploaded_by
                WHERE f.deleted_flag = 0
                """);
        if (businessType != null && businessId != null) {
            sql.append(" AND f.business_type = ? AND f.business_id = ?");
            return jdbcTemplate.query(sql.append(" ORDER BY f.id DESC").toString(),
                    (rs, rowNum) -> toAttachmentMap(rs),
                    businessType,
                    businessId
            );
        }
        return jdbcTemplate.query(sql.append(" ORDER BY f.id DESC").toString(), (rs, rowNum) -> toAttachmentMap(rs));
    }

    private Map<String, Object> toAttachmentMap(java.sql.ResultSet rs) throws java.sql.SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rs.getLong("id"));
        row.put("businessType", rs.getString("business_type"));
        row.put("businessId", rs.getLong("business_id"));
        row.put("fileName", rs.getString("file_name"));
        row.put("fileSize", rs.getLong("file_size"));
        row.put("contentType", rs.getString("content_type"));
        row.put("uploadedBy", rs.getLong("uploaded_by"));
        row.put("uploadedByName", rs.getString("uploaded_by_name"));
        row.put("createdAt", toLocalDateTime(rs.getTimestamp("created_at")));
        row.put("updatedAt", toLocalDateTime(rs.getTimestamp("updated_at")));
        return row;
    }

    private Attachment findAttachment(Long attachmentId) {
        List<Attachment> attachments = jdbcTemplate.query("""
                        SELECT id, business_type, business_id, file_name, file_path, file_size, content_type, uploaded_by
                        FROM file_attachment
                        WHERE id = ?
                          AND deleted_flag = 0
                        """,
                (rs, rowNum) -> new Attachment(
                        rs.getLong("id"),
                        rs.getString("business_type"),
                        rs.getLong("business_id"),
                        rs.getString("file_name"),
                        rs.getString("file_path"),
                        rs.getLong("file_size"),
                        rs.getString("content_type"),
                        rs.getLong("uploaded_by")
                ),
                attachmentId
        );
        if (attachments.isEmpty()) {
            throw new SystemException("附件不存在");
        }
        return attachments.get(0);
    }

    private void ensureReadable(AuthenticatedUser currentUser, String businessType, Long businessId) {
        if (WORKFLOW_APPLICATION.equalsIgnoreCase(businessType)) {
            workflowService.ensureReadable(currentUser, businessId);
            return;
        }
        ensureAdmin(currentUser);
    }

    private void ensureWritable(AuthenticatedUser currentUser, String businessType, Long businessId) {
        if (WORKFLOW_APPLICATION.equalsIgnoreCase(businessType)) {
            if (!workflowService.canManageAttachments(currentUser, businessId)) {
                throw new SystemException("当前用户无权上传该申请单附件");
            }
            return;
        }
        ensureAdmin(currentUser);
    }

    private Path resolveUploadDir(String businessType) {
        String dayPath = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now());
        return Path.of(System.getProperty("user.dir"), "uploads", businessType.toLowerCase(), dayPath);
    }

    private void ensureAdmin(AuthenticatedUser currentUser) {
        if (!isAdmin(currentUser)) {
            throw new SystemException("仅系统管理员可执行该操作");
        }
    }

    private boolean isAdmin(AuthenticatedUser currentUser) {
        return currentUser != null && (currentUser.roles().contains("ADMIN") || "ADMIN".equalsIgnoreCase(currentUser.userType()));
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private record Attachment(
            Long id,
            String businessType,
            Long businessId,
            String fileName,
            String filePath,
            Long fileSize,
            String contentType,
            Long uploadedBy
    ) {
    }
}
