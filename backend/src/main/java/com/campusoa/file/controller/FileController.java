package com.campusoa.file.controller;

import com.campusoa.common.ApiResponse;
import com.campusoa.file.service.FileService;
import com.campusoa.security.AuthenticatedUser;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> listFiles(
            Authentication authentication,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) Long businessId
    ) {
        return ApiResponse.ok(fileService.listFiles(currentUser(authentication), businessType, businessId));
    }

    @PostMapping("/upload")
    public ApiResponse<Map<String, Long>> upload(
            Authentication authentication,
            @RequestParam String businessType,
            @RequestParam Long businessId,
            @RequestParam("file") MultipartFile file
    ) {
        Long id = fileService.upload(currentUser(authentication), businessType, businessId, file);
        return ApiResponse.ok("附件已上传", Map.of("id", id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> download(Authentication authentication, @PathVariable Long id) {
        return fileService.download(currentUser(authentication), id);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(Authentication authentication, @PathVariable Long id) {
        fileService.delete(currentUser(authentication), id);
        return ApiResponse.ok("附件已删除", null);
    }

    private AuthenticatedUser currentUser(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
