package com.campusoa.auth.service;

import com.campusoa.auth.dto.LoginResponse;
import com.campusoa.auth.dto.RegisterRequest;
import com.campusoa.auth.dto.UserProfile;
import com.campusoa.auth.exception.AuthException;
import com.campusoa.security.AuthenticatedUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final JdbcTemplate jdbcTemplate;
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

    public AuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public LoginResponse login(String username, String password) {
        DbUser user = findUserByUsername(username);
        if (user == null || !user.passwordHash().equals(password)) {
            throw new AuthException("用户名或密码错误");
        }
        if (user.status() != 1) {
            throw new AuthException("用户已被禁用");
        }

        AuthenticatedUser authenticatedUser = toAuthenticatedUser(user, findRolesByUserId(user.userId()));
        String token = UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, authenticatedUser.userId());

        return new LoginResponse(
                token,
                toUserProfile(authenticatedUser),
                menusFor(authenticatedUser),
                permissionsFor(authenticatedUser)
        );
    }

    @Transactional
    public Long registerStudent(RegisterRequest request) {
        String username = normalize(request.username());
        String password = normalize(request.password());
        String confirmPassword = normalize(request.confirmPassword());
        String realName = normalize(request.realName());
        String phone = optionalNormalize(request.phone());
        String email = optionalNormalize(request.email());

        if (!username.matches("^[A-Za-z0-9_]{4,32}$")) {
            throw new AuthException("用户名仅支持 4 到 32 位字母、数字或下划线");
        }
        if (!password.equals(confirmPassword)) {
            throw new AuthException("两次输入的密码不一致");
        }
        if (findUserByUsername(username) != null) {
            throw new AuthException("用户名已存在");
        }

        Long studentRoleId = findRoleIdByCode("STUDENT");
        if (studentRoleId == null) {
            throw new AuthException("学生角色未初始化，请联系管理员");
        }

        jdbcTemplate.update("""
                        INSERT INTO sys_user (org_id, username, password_hash, real_name, user_type, phone, email, status)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                null, username, password, realName, "STUDENT", phone, email, 1
        );
        Long userId = queryLastInsertId();
        jdbcTemplate.update("""
                        INSERT INTO sys_user_role (user_id, role_id)
                        VALUES (?, ?)
                        """,
                userId, studentRoleId
        );
        return userId;
    }

    public AuthenticatedUser findByToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        Long userId = tokens.get(token);
        if (userId == null) {
            return null;
        }
        AuthenticatedUser currentUser = findActiveAuthenticatedUser(userId);
        if (currentUser == null) {
            tokens.remove(token);
        }
        return currentUser;
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        tokens.remove(token);
    }

    public UserProfile profile(AuthenticatedUser user) {
        return toUserProfile(requireActiveAuthenticatedUser(user));
    }

    public Map<String, Object> profilePayload(AuthenticatedUser user) {
        AuthenticatedUser currentUser = requireActiveAuthenticatedUser(user);
        return Map.of(
                "profile", toUserProfile(currentUser),
                "menus", menusForAuthenticatedUser(currentUser),
                "permissions", permissionsForAuthenticatedUser(currentUser)
        );
    }

    public List<String> menusFor(AuthenticatedUser user) {
        return menusForAuthenticatedUser(requireActiveAuthenticatedUser(user));
    }

    public List<String> permissionsFor(AuthenticatedUser user) {
        return permissionsForAuthenticatedUser(requireActiveAuthenticatedUser(user));
    }

    private List<String> menusForAuthenticatedUser(AuthenticatedUser user) {
        List<String> roles = user.roles();
        if (isAdmin(user, roles)) {
            return findAllTopLevelMenus();
        }
        List<String> menus = findMenusByRoles(roles);
        return menus.isEmpty() ? buildMenus(roles) : menus;
    }

    private List<String> permissionsForAuthenticatedUser(AuthenticatedUser user) {
        List<String> roles = user.roles();
        if (isAdmin(user, roles)) {
            return findAllPermissions();
        }
        return findPermissionsByRoles(roles);
    }

    private AuthenticatedUser requireActiveAuthenticatedUser(AuthenticatedUser user) {
        if (user == null || user.userId() == null) {
            throw new AuthException("当前用户不存在或已被禁用");
        }
        AuthenticatedUser currentUser = findActiveAuthenticatedUser(user.userId());
        if (currentUser == null) {
            throw new AuthException("当前用户不存在或已被禁用");
        }
        return currentUser;
    }

    private AuthenticatedUser findActiveAuthenticatedUser(Long userId) {
        DbUser current = findUserById(userId);
        if (current == null || current.status() == null || current.status() != 1) {
            return null;
        }
        return toAuthenticatedUser(current, findRolesByUserId(userId));
    }

    private DbUser findUserByUsername(String username) {
        List<DbUser> users = jdbcTemplate.query("""
                        SELECT id, username, password_hash, real_name, user_type, status
                        FROM sys_user
                        WHERE username = ?
                        """,
                (rs, rowNum) -> new DbUser(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("real_name"),
                        rs.getString("user_type"),
                        rs.getInt("status")
                ),
                username
        );
        return users.isEmpty() ? null : users.get(0);
    }

    private DbUser findUserById(Long userId) {
        List<DbUser> users = jdbcTemplate.query("""
                        SELECT id, username, password_hash, real_name, user_type, status
                        FROM sys_user
                        WHERE id = ?
                        """,
                (rs, rowNum) -> new DbUser(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("real_name"),
                        rs.getString("user_type"),
                        rs.getInt("status")
                ),
                userId
        );
        return users.isEmpty() ? null : users.get(0);
    }

    private List<String> findRolesByUserId(Long userId) {
        return jdbcTemplate.query("""
                        SELECT r.role_code
                        FROM sys_role r
                        JOIN sys_user_role ur ON ur.role_id = r.id
                        WHERE ur.user_id = ?
                          AND r.status = 1
                        ORDER BY r.id
                        """,
                (rs, rowNum) -> rs.getString("role_code"),
                userId
        );
    }

    private Long findRoleIdByCode(String roleCode) {
        List<Long> roleIds = jdbcTemplate.query("""
                        SELECT id
                        FROM sys_role
                        WHERE role_code = ?
                          AND status = 1
                        """,
                (rs, rowNum) -> rs.getLong("id"),
                roleCode
        );
        return roleIds.isEmpty() ? null : roleIds.get(0);
    }

    private AuthenticatedUser toAuthenticatedUser(DbUser user, List<String> roles) {
        return new AuthenticatedUser(user.userId(), user.username(), user.realName(), user.userType(), roles);
    }

    private UserProfile toUserProfile(AuthenticatedUser user) {
        return new UserProfile(user.userId(), user.username(), user.realName(), user.userType(), user.roles());
    }

    private List<String> findMenusByRoles(List<String> roles) {
        if (roles.isEmpty()) {
            return List.of();
        }
        return jdbcTemplate.query("""
                        SELECT m.permission_code
                        FROM sys_menu m
                        JOIN sys_role_menu rm ON rm.menu_id = m.id
                        JOIN sys_role r ON r.id = rm.role_id
                        WHERE m.status = 1
                          AND m.parent_id IS NULL
                          AND r.status = 1
                          AND r.role_code IN (%s)
                        GROUP BY m.permission_code
                        ORDER BY MIN(m.sort_no) ASC, MIN(m.id) ASC
                        """.formatted(placeholders(roles.size())),
                (rs, rowNum) -> rs.getString("permission_code"),
                roles.toArray()
        );
    }

    private List<String> findPermissionsByRoles(List<String> roles) {
        if (roles.isEmpty()) {
            return List.of();
        }
        return jdbcTemplate.query("""
                        SELECT p.permission_code
                        FROM sys_permission p
                        JOIN sys_role_permission rp ON rp.permission_id = p.id
                        JOIN sys_role r ON r.id = rp.role_id
                        WHERE p.status = 1
                          AND r.status = 1
                          AND r.role_code IN (%s)
                        GROUP BY p.permission_code
                        ORDER BY MIN(p.id) ASC
                        """.formatted(placeholders(roles.size())),
                (rs, rowNum) -> rs.getString("permission_code"),
                roles.toArray()
        );
    }

    private List<String> findAllTopLevelMenus() {
        return jdbcTemplate.query("""
                        SELECT permission_code
                        FROM sys_menu
                        WHERE status = 1
                          AND parent_id IS NULL
                        ORDER BY sort_no ASC, id ASC
                        """,
                (rs, rowNum) -> rs.getString("permission_code")
        );
    }

    private List<String> findAllPermissions() {
        return jdbcTemplate.query("""
                        SELECT permission_code
                        FROM sys_permission
                        WHERE status = 1
                        ORDER BY id ASC
                        """,
                (rs, rowNum) -> rs.getString("permission_code")
        );
    }

    private boolean isAdmin(AuthenticatedUser user, List<String> roles) {
        return user != null && (roles.contains("ADMIN") || "ADMIN".equalsIgnoreCase(user.userType()));
    }

    private String placeholders(int size) {
        return String.join(", ", java.util.Collections.nCopies(size, "?"));
    }

    private Long queryLastInsertId() {
        Number key = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Number.class);
        if (key == null) {
            throw new AuthException("注册失败，未获取到用户编号");
        }
        return key.longValue();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String optionalNormalize(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? null : normalized;
    }

    private List<String> buildMenus(List<String> roles) {
        if (roles.contains("ADMIN")) {
            return List.of("dashboard", "workflow", "student-affairs", "academic", "research", "logistics", "system");
        }
        if (roles.contains("STUDENT")) {
            return List.of("dashboard", "workflow", "student-affairs", "logistics");
        }
        if (roles.contains("ADVISER")) {
            return List.of("dashboard", "workflow", "student-affairs", "logistics");
        }
        if (roles.contains("TEACHER")) {
            return List.of("dashboard", "workflow", "student-affairs", "academic", "research", "logistics");
        }
        if (roles.contains("RESEARCH")) {
            return List.of("dashboard", "workflow", "research", "logistics");
        }
        if (roles.contains("OFFICE")) {
            return List.of("dashboard", "workflow", "academic", "logistics");
        }
        if (roles.contains("REVIEWER")) {
            return List.of("dashboard", "workflow", "research");
        }
        if (roles.contains("STUDENT_AFFAIRS")) {
            return List.of("dashboard", "workflow", "student-affairs", "logistics");
        }
        return new ArrayList<>(List.of("dashboard", "workflow"));
    }

    private record DbUser(
            Long userId,
            String username,
            String passwordHash,
            String realName,
            String userType,
            Integer status
    ) {
    }
}
