package com.campusoa.system.service;

import com.campusoa.security.AuthenticatedUser;
import com.campusoa.system.exception.SystemException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class SystemAdminService {

    private final JdbcTemplate jdbcTemplate;

    public SystemAdminService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> listUsers(AuthenticatedUser currentUser) {
        ensureAdmin(currentUser);
        return jdbcTemplate.query("""
                        SELECT u.id,
                               u.username,
                               u.real_name,
                               u.user_type,
                               u.org_id,
                               o.org_name,
                               u.phone,
                               u.email,
                               u.status,
                               u.created_at,
                               u.updated_at,
                               GROUP_CONCAT(DISTINCT r.role_code ORDER BY r.id) AS role_codes,
                               GROUP_CONCAT(DISTINCT r.role_name ORDER BY r.id) AS role_names,
                               GROUP_CONCAT(DISTINCT r.id ORDER BY r.id) AS role_ids
                        FROM sys_user u
                        LEFT JOIN sys_org o ON o.id = u.org_id
                        LEFT JOIN sys_user_role ur ON ur.user_id = u.id
                        LEFT JOIN sys_role r ON r.id = ur.role_id
                        GROUP BY u.id, u.username, u.real_name, u.user_type, u.org_id, o.org_name,
                                 u.phone, u.email, u.status, u.created_at, u.updated_at
                        ORDER BY u.id ASC
                        """,
                (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("username", rs.getString("username"));
                    row.put("realName", rs.getString("real_name"));
                    row.put("userType", rs.getString("user_type"));
                    row.put("orgId", getLong(rs, "org_id"));
                    row.put("orgName", rs.getString("org_name"));
                    row.put("phone", rs.getString("phone"));
                    row.put("email", rs.getString("email"));
                    row.put("status", rs.getInt("status"));
                    row.put("createdAt", toLocalDateTime(rs.getTimestamp("created_at")));
                    row.put("updatedAt", toLocalDateTime(rs.getTimestamp("updated_at")));
                    row.put("roleCodes", splitCsv(rs.getString("role_codes")));
                    row.put("roleNames", splitCsv(rs.getString("role_names")));
                    row.put("roleIds", splitLongCsv(rs.getString("role_ids")));
                    return row;
                });
    }

    @Transactional
    public Long createUser(AuthenticatedUser currentUser, Map<String, Object> payload) {
        ensureAdmin(currentUser);
        String username = requiredString(payload, "username", "用户名不能为空");
        String password = requiredString(payload, "password", "密码不能为空");
        String realName = requiredString(payload, "realName", "姓名不能为空");
        String userType = requiredString(payload, "userType", "用户类型不能为空");
        Long orgId = optionalLong(payload, "orgId");
        String phone = optionalString(payload, "phone");
        String email = optionalString(payload, "email");
        int status = normalizeStatus(payload.get("status"));

        ensureUserUnique(username, null);
        if (orgId != null) {
            ensureOrgExists(orgId);
        }

        jdbcTemplate.update("""
                        INSERT INTO sys_user (org_id, username, password_hash, real_name, user_type, phone, email, status)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                orgId, username, password, realName, userType, phone, email, status
        );
        Long userId = queryLastInsertId();
        assignUserRolesInternal(userId, extractLongList(payload.get("roleIds")));
        return userId;
    }

    @Transactional
    public void updateUser(AuthenticatedUser currentUser, Long userId, Map<String, Object> payload) {
        ensureAdmin(currentUser);
        ensureUserExists(userId);

        String username = requiredString(payload, "username", "用户名不能为空");
        String realName = requiredString(payload, "realName", "姓名不能为空");
        String userType = requiredString(payload, "userType", "用户类型不能为空");
        Long orgId = optionalLong(payload, "orgId");
        String phone = optionalString(payload, "phone");
        String email = optionalString(payload, "email");
        int status = normalizeStatus(payload.get("status"));

        ensureUserUnique(username, userId);
        if (orgId != null) {
            ensureOrgExists(orgId);
        }

        jdbcTemplate.update("""
                        UPDATE sys_user
                        SET org_id = ?, username = ?, real_name = ?, user_type = ?, phone = ?, email = ?, status = ?, updated_at = NOW()
                        WHERE id = ?
                        """,
                orgId, username, realName, userType, phone, email, status, userId
        );
    }

    @Transactional
    public void assignUserRoles(AuthenticatedUser currentUser, Long userId, List<Long> roleIds) {
        ensureAdmin(currentUser);
        ensureUserExists(userId);
        assignUserRolesInternal(userId, roleIds);
    }

    @Transactional
    public void updateUserStatus(AuthenticatedUser currentUser, Long userId, int status) {
        ensureAdmin(currentUser);
        ensureUserExists(userId);
        jdbcTemplate.update("""
                        UPDATE sys_user
                        SET status = ?, updated_at = NOW()
                        WHERE id = ?
                        """,
                normalizeStatus(status), userId
        );
    }

    public List<Map<String, Object>> listOrgTree(AuthenticatedUser currentUser) {
        ensureAdmin(currentUser);
        List<Map<String, Object>> flat = jdbcTemplate.query("""
                        SELECT id, parent_id, org_code, org_name, org_type, sort_no, status, created_at, updated_at
                        FROM sys_org
                        ORDER BY sort_no ASC, id ASC
                        """,
                (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("parentId", getLong(rs, "parent_id"));
                    row.put("orgCode", rs.getString("org_code"));
                    row.put("orgName", rs.getString("org_name"));
                    row.put("orgType", rs.getString("org_type"));
                    row.put("sortNo", rs.getInt("sort_no"));
                    row.put("status", rs.getInt("status"));
                    row.put("createdAt", toLocalDateTime(rs.getTimestamp("created_at")));
                    row.put("updatedAt", toLocalDateTime(rs.getTimestamp("updated_at")));
                    row.put("children", new ArrayList<Map<String, Object>>());
                    return row;
                });

        Map<Long, Map<String, Object>> indexed = new LinkedHashMap<>();
        for (Map<String, Object> row : flat) {
            indexed.put(((Number) row.get("id")).longValue(), row);
        }

        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map<String, Object> row : flat) {
            Long parentId = (Long) row.get("parentId");
            if (parentId == null) {
                roots.add(row);
                continue;
            }
            Map<String, Object> parent = indexed.get(parentId);
            if (parent == null) {
                roots.add(row);
                continue;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
            children.add(row);
        }
        return roots;
    }

    @Transactional
    public Long createOrg(AuthenticatedUser currentUser, Map<String, Object> payload) {
        ensureAdmin(currentUser);
        Long parentId = optionalLong(payload, "parentId");
        String orgCode = requiredString(payload, "orgCode", "组织编码不能为空");
        String orgName = requiredString(payload, "orgName", "组织名称不能为空");
        String orgType = requiredString(payload, "orgType", "组织类型不能为空");
        int sortNo = optionalInt(payload, "sortNo", 0);
        int status = normalizeStatus(payload.get("status"));

        ensureOrgUnique(orgCode, null);
        if (parentId != null) {
            ensureOrgExists(parentId);
        }

        jdbcTemplate.update("""
                        INSERT INTO sys_org (parent_id, org_code, org_name, org_type, sort_no, status)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                parentId, orgCode, orgName, orgType, sortNo, status
        );
        return queryLastInsertId();
    }

    @Transactional
    public void updateOrg(AuthenticatedUser currentUser, Long orgId, Map<String, Object> payload) {
        ensureAdmin(currentUser);
        ensureOrgExists(orgId);

        Long parentId = optionalLong(payload, "parentId");
        String orgCode = requiredString(payload, "orgCode", "组织编码不能为空");
        String orgName = requiredString(payload, "orgName", "组织名称不能为空");
        String orgType = requiredString(payload, "orgType", "组织类型不能为空");
        int sortNo = optionalInt(payload, "sortNo", 0);
        int status = normalizeStatus(payload.get("status"));

        ensureOrgUnique(orgCode, orgId);
        if (parentId != null) {
            ensureOrgExists(parentId);
        }
        if (Objects.equals(parentId, orgId)) {
            throw new SystemException("组织不能挂在自己下面");
        }

        jdbcTemplate.update("""
                        UPDATE sys_org
                        SET parent_id = ?, org_code = ?, org_name = ?, org_type = ?, sort_no = ?, status = ?, updated_at = NOW()
                        WHERE id = ?
                        """,
                parentId, orgCode, orgName, orgType, sortNo, status, orgId
        );
    }

    public List<Map<String, Object>> listRoles(AuthenticatedUser currentUser) {
        ensureAdmin(currentUser);
        return jdbcTemplate.query("""
                        SELECT r.id,
                               r.role_code,
                               r.role_name,
                               r.status,
                               r.created_at,
                               r.updated_at,
                               GROUP_CONCAT(DISTINCT rm.menu_id ORDER BY rm.menu_id) AS menu_ids,
                               GROUP_CONCAT(DISTINCT rp.permission_id ORDER BY rp.permission_id) AS permission_ids
                        FROM sys_role r
                        LEFT JOIN sys_role_menu rm ON rm.role_id = r.id
                        LEFT JOIN sys_role_permission rp ON rp.role_id = r.id
                        GROUP BY r.id, r.role_code, r.role_name, r.status, r.created_at, r.updated_at
                        ORDER BY r.id ASC
                        """,
                (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("roleCode", rs.getString("role_code"));
                    row.put("roleName", rs.getString("role_name"));
                    row.put("status", rs.getInt("status"));
                    row.put("createdAt", toLocalDateTime(rs.getTimestamp("created_at")));
                    row.put("updatedAt", toLocalDateTime(rs.getTimestamp("updated_at")));
                    row.put("assignedMenuIds", splitLongCsv(rs.getString("menu_ids")));
                    row.put("assignedPermissionIds", splitLongCsv(rs.getString("permission_ids")));
                    return row;
                });
    }

    @Transactional
    public Long createRole(AuthenticatedUser currentUser, Map<String, Object> payload) {
        ensureAdmin(currentUser);
        String roleCode = requiredString(payload, "roleCode", "角色编码不能为空");
        String roleName = requiredString(payload, "roleName", "角色名称不能为空");
        int status = normalizeStatus(payload.get("status"));

        ensureRoleUnique(roleCode, null);
        jdbcTemplate.update("""
                        INSERT INTO sys_role (role_code, role_name, status)
                        VALUES (?, ?, ?)
                        """,
                roleCode, roleName, status
        );
        return queryLastInsertId();
    }

    @Transactional
    public void updateRole(AuthenticatedUser currentUser, Long roleId, Map<String, Object> payload) {
        ensureAdmin(currentUser);
        ensureRoleExists(roleId);

        String roleCode = requiredString(payload, "roleCode", "角色编码不能为空");
        String roleName = requiredString(payload, "roleName", "角色名称不能为空");
        int status = normalizeStatus(payload.get("status"));

        ensureRoleUnique(roleCode, roleId);
        jdbcTemplate.update("""
                        UPDATE sys_role
                        SET role_code = ?, role_name = ?, status = ?, updated_at = NOW()
                        WHERE id = ?
                        """,
                roleCode, roleName, status, roleId
        );
    }

    public List<Map<String, Object>> listMenus(AuthenticatedUser currentUser) {
        ensureAdmin(currentUser);
        return jdbcTemplate.query("""
                        SELECT id, parent_id, menu_name, menu_type, route_path, permission_code, sort_no, status
                        FROM sys_menu
                        WHERE status = 1
                        ORDER BY COALESCE(parent_id, 0) ASC, sort_no ASC, id ASC
                        """,
                (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("parentId", getLong(rs, "parent_id"));
                    row.put("menuName", rs.getString("menu_name"));
                    row.put("menuType", rs.getString("menu_type"));
                    row.put("routePath", rs.getString("route_path"));
                    row.put("permissionCode", rs.getString("permission_code"));
                    row.put("sortNo", rs.getInt("sort_no"));
                    row.put("status", rs.getInt("status"));
                    return row;
                });
    }

    @Transactional
    public void assignRoleMenus(AuthenticatedUser currentUser, Long roleId, List<Long> menuIds) {
        ensureAdmin(currentUser);
        ensureRoleExists(roleId);
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_id = ?", roleId);
        for (Long menuId : sanitizeIds(menuIds)) {
            ensureMenuExists(menuId);
            jdbcTemplate.update("""
                            INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
                            VALUES (?, ?)
                            """,
                    roleId, menuId
            );
        }
    }

    public List<Map<String, Object>> listPermissions(AuthenticatedUser currentUser) {
        ensureAdmin(currentUser);
        return jdbcTemplate.query("""
                        SELECT id, permission_code, permission_name, permission_group, status
                        FROM sys_permission
                        WHERE status = 1
                        ORDER BY permission_group ASC, id ASC
                        """,
                (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("permissionCode", rs.getString("permission_code"));
                    row.put("permissionName", rs.getString("permission_name"));
                    row.put("permissionGroup", rs.getString("permission_group"));
                    row.put("status", rs.getInt("status"));
                    return row;
                });
    }

    @Transactional
    public void assignRolePermissions(AuthenticatedUser currentUser, Long roleId, List<Long> permissionIds) {
        ensureAdmin(currentUser);
        ensureRoleExists(roleId);
        jdbcTemplate.update("DELETE FROM sys_role_permission WHERE role_id = ?", roleId);
        for (Long permissionId : sanitizeIds(permissionIds)) {
            ensurePermissionExists(permissionId);
            jdbcTemplate.update("""
                            INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
                            VALUES (?, ?)
                            """,
                    roleId, permissionId
            );
        }
    }

    public List<Map<String, Object>> listWorkflowDefinitions(AuthenticatedUser currentUser) {
        ensureAdmin(currentUser);
        return jdbcTemplate.query("""
                        SELECT d.id,
                               d.business_type,
                               d.definition_code,
                               d.definition_name,
                               d.version_no,
                               d.status,
                               d.created_at,
                               d.updated_at,
                               COUNT(n.id) AS node_count
                        FROM wf_definition d
                        LEFT JOIN wf_node_definition n ON n.definition_id = d.id AND n.status = 1
                        GROUP BY d.id, d.business_type, d.definition_code, d.definition_name,
                                 d.version_no, d.status, d.created_at, d.updated_at
                        ORDER BY d.id ASC
                        """,
                (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("businessType", rs.getString("business_type"));
                    row.put("definitionCode", rs.getString("definition_code"));
                    row.put("definitionName", rs.getString("definition_name"));
                    row.put("versionNo", rs.getInt("version_no"));
                    row.put("status", rs.getInt("status"));
                    row.put("nodeCount", rs.getInt("node_count"));
                    row.put("createdAt", toLocalDateTime(rs.getTimestamp("created_at")));
                    row.put("updatedAt", toLocalDateTime(rs.getTimestamp("updated_at")));
                    return row;
                });
    }

    @Transactional
    public Long createWorkflowDefinition(AuthenticatedUser currentUser, Map<String, Object> payload) {
        ensureAdmin(currentUser);
        String businessType = requiredString(payload, "businessType", "业务类型不能为空");
        String definitionCode = requiredString(payload, "definitionCode", "流程编码不能为空");
        String definitionName = requiredString(payload, "definitionName", "流程名称不能为空");
        int versionNo = optionalInt(payload, "versionNo", 1);
        int status = normalizeStatus(payload.get("status"));

        ensureWorkflowDefinitionUnique(definitionCode, versionNo, null);
        jdbcTemplate.update("""
                        INSERT INTO wf_definition (business_type, definition_code, definition_name, version_no, status)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                businessType, definitionCode, definitionName, versionNo, status
        );
        return queryLastInsertId();
    }

    @Transactional
    public void updateWorkflowDefinition(AuthenticatedUser currentUser, Long definitionId, Map<String, Object> payload) {
        ensureAdmin(currentUser);
        ensureWorkflowDefinitionExists(definitionId);

        String businessType = requiredString(payload, "businessType", "业务类型不能为空");
        String definitionCode = requiredString(payload, "definitionCode", "流程编码不能为空");
        String definitionName = requiredString(payload, "definitionName", "流程名称不能为空");
        int versionNo = optionalInt(payload, "versionNo", 1);
        int status = normalizeStatus(payload.get("status"));

        ensureWorkflowDefinitionUnique(definitionCode, versionNo, definitionId);
        jdbcTemplate.update("""
                        UPDATE wf_definition
                        SET business_type = ?, definition_code = ?, definition_name = ?, version_no = ?, status = ?, updated_at = NOW()
                        WHERE id = ?
                        """,
                businessType, definitionCode, definitionName, versionNo, status, definitionId
        );
    }

    public List<Map<String, Object>> listWorkflowNodes(AuthenticatedUser currentUser, Long definitionId) {
        ensureAdmin(currentUser);
        ensureWorkflowDefinitionExists(definitionId);
        return jdbcTemplate.query("""
                        SELECT id, node_code, node_name, node_type, approver_type, approver_role_code, sort_no, status
                        FROM wf_node_definition
                        WHERE definition_id = ?
                        ORDER BY sort_no ASC, id ASC
                        """,
                (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("nodeCode", rs.getString("node_code"));
                    row.put("nodeName", rs.getString("node_name"));
                    row.put("nodeType", rs.getString("node_type"));
                    row.put("approverType", rs.getString("approver_type"));
                    row.put("approverRoleCode", rs.getString("approver_role_code"));
                    row.put("sortNo", rs.getInt("sort_no"));
                    row.put("status", rs.getInt("status"));
                    return row;
                },
                definitionId
        );
    }

    @Transactional
    public void saveWorkflowNodes(AuthenticatedUser currentUser, Long definitionId, List<Map<String, Object>> nodes) {
        ensureAdmin(currentUser);
        ensureWorkflowDefinitionExists(definitionId);
        jdbcTemplate.update("DELETE FROM wf_node_definition WHERE definition_id = ?", definitionId);

        int sortNo = 1;
        for (Map<String, Object> node : nodes) {
            String nodeCode = requiredString(node, "nodeCode", "节点编码不能为空");
            String nodeName = requiredString(node, "nodeName", "节点名称不能为空");
            String nodeType = requiredString(node, "nodeType", "节点类型不能为空");
            String approverType = requiredString(node, "approverType", "审批人类型不能为空");
            String approverRoleCode = requiredString(node, "approverRoleCode", "审批角色不能为空");
            int currentSort = optionalInt(node, "sortNo", sortNo);
            int status = normalizeStatus(node.get("status"));

            jdbcTemplate.update("""
                            INSERT INTO wf_node_definition (
                                definition_id, node_code, node_name, node_type, approver_type, approver_role_code, sort_no, status
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    definitionId, nodeCode, nodeName, nodeType, approverType, approverRoleCode, currentSort, status
            );
            sortNo++;
        }
    }

    private void assignUserRolesInternal(Long userId, List<Long> roleIds) {
        jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", userId);
        for (Long roleId : sanitizeIds(roleIds)) {
            ensureRoleExists(roleId);
            jdbcTemplate.update("""
                            INSERT IGNORE INTO sys_user_role (user_id, role_id)
                            VALUES (?, ?)
                            """,
                    userId, roleId
            );
        }
    }

    private List<Long> sanitizeIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private void ensureAdmin(AuthenticatedUser currentUser) {
        if (currentUser == null || (!currentUser.roles().contains("ADMIN") && !"ADMIN".equalsIgnoreCase(currentUser.userType()))) {
            throw new SystemException("仅系统管理员可执行该操作");
        }
    }

    private void ensureUserUnique(String username, Long excludeId) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(1)
                FROM sys_user
                WHERE username = ?
                """);
        args.add(username);
        if (excludeId != null) {
            sql.append(" AND id <> ?");
            args.add(excludeId);
        }
        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        if (count != null && count > 0) {
            throw new SystemException("用户名已存在");
        }
    }

    private void ensureOrgUnique(String orgCode, Long excludeId) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(1)
                FROM sys_org
                WHERE org_code = ?
                """);
        args.add(orgCode);
        if (excludeId != null) {
            sql.append(" AND id <> ?");
            args.add(excludeId);
        }
        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        if (count != null && count > 0) {
            throw new SystemException("组织编码已存在");
        }
    }

    private void ensureRoleUnique(String roleCode, Long excludeId) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(1)
                FROM sys_role
                WHERE role_code = ?
                """);
        args.add(roleCode);
        if (excludeId != null) {
            sql.append(" AND id <> ?");
            args.add(excludeId);
        }
        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        if (count != null && count > 0) {
            throw new SystemException("角色编码已存在");
        }
    }

    private void ensureWorkflowDefinitionUnique(String definitionCode, int versionNo, Long excludeId) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(1)
                FROM wf_definition
                WHERE definition_code = ?
                  AND version_no = ?
                """);
        args.add(definitionCode);
        args.add(versionNo);
        if (excludeId != null) {
            sql.append(" AND id <> ?");
            args.add(excludeId);
        }
        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        if (count != null && count > 0) {
            throw new SystemException("同版本流程编码已存在");
        }
    }

    private void ensureUserExists(Long userId) {
        ensureExists("sys_user", userId, "用户不存在");
    }

    private void ensureOrgExists(Long orgId) {
        ensureExists("sys_org", orgId, "组织不存在");
    }

    private void ensureRoleExists(Long roleId) {
        ensureExists("sys_role", roleId, "角色不存在");
    }

    private void ensureMenuExists(Long menuId) {
        ensureExists("sys_menu", menuId, "菜单不存在");
    }

    private void ensurePermissionExists(Long permissionId) {
        ensureExists("sys_permission", permissionId, "权限不存在");
    }

    private void ensureWorkflowDefinitionExists(Long definitionId) {
        ensureExists("wf_definition", definitionId, "流程模板不存在");
    }

    private void ensureExists(String tableName, Long id, String message) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM " + tableName + " WHERE id = ?",
                Integer.class,
                id
        );
        if (count == null || count == 0) {
            throw new SystemException(message);
        }
    }

    private String requiredString(Map<String, Object> payload, String key, String message) {
        String value = optionalString(payload, key);
        if (value == null || value.isBlank()) {
            throw new SystemException(message);
        }
        return value;
    }

    private String optionalString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private Long optionalLong(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException exception) {
            throw new SystemException("字段 " + key + " 必须为数字");
        }
    }

    private int optionalInt(Map<String, Object> payload, String key, int fallback) {
        Object value = payload.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException exception) {
            throw new SystemException("字段 " + key + " 必须为数字");
        }
    }

    private int normalizeStatus(Object statusValue) {
        int status;
        if (statusValue == null || String.valueOf(statusValue).isBlank()) {
            status = 1;
        } else {
            try {
                status = Integer.parseInt(String.valueOf(statusValue).trim());
            } catch (NumberFormatException exception) {
                throw new SystemException("状态值必须为数字");
            }
        }
        if (status != 0 && status != 1) {
            throw new SystemException("状态值仅允许 0 或 1");
        }
        return status;
    }

    private List<Long> extractLongList(Object rawValue) {
        if (rawValue == null) {
            return List.of();
        }
        if (rawValue instanceof List<?> list) {
            List<Long> values = new ArrayList<>();
            for (Object item : list) {
                if (item == null || String.valueOf(item).isBlank()) {
                    continue;
                }
                try {
                    values.add(Long.parseLong(String.valueOf(item).trim()));
                } catch (NumberFormatException exception) {
                    throw new SystemException("列表项必须为数字");
                }
            }
            return values;
        }
        if (rawValue instanceof String text && !text.isBlank()) {
            return Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .map(Long::parseLong)
                    .toList();
        }
        throw new SystemException("列表字段格式错误");
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private List<Long> splitLongCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(Long::parseLong)
                .toList();
    }

    private Long queryLastInsertId() {
        Number key = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Number.class);
        if (key == null) {
            throw new SystemException("保存失败，未获取到主键");
        }
        return key.longValue();
    }

    private Long getLong(java.sql.ResultSet rs, String columnName) throws java.sql.SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
