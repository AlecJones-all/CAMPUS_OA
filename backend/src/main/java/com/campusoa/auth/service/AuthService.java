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
    private final Map<String, AuthenticatedUser> tokens = new ConcurrentHashMap<>();

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
        tokens.put(token, authenticatedUser);

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
        return tokens.get(token);
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        tokens.remove(token);
    }

    public UserProfile profile(AuthenticatedUser user) {
        DbUser current = findUserById(user.userId());
        if (current == null) {
            throw new AuthException("当前用户不存在");
        }
        return toUserProfile(toAuthenticatedUser(current, findRolesByUserId(user.userId())));
    }

    public List<String> menusFor(AuthenticatedUser user) {
        List<String> roles = findRolesByUserId(user.userId());
        List<String> menus = findMenusByRoles(roles);
        return menus.isEmpty() ? buildMenus(roles) : menus;
    }

    public List<String> permissionsFor(AuthenticatedUser user) {
        return findPermissionsByRoles(findRolesByUserId(user.userId()));
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
                          AND r.role_code IN (%s)
                        GROUP BY p.permission_code
                        ORDER BY MIN(p.id) ASC
                        """.formatted(placeholders(roles.size())),
                (rs, rowNum) -> rs.getString("permission_code"),
                roles.toArray()
        );
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
            return List.of("dashboard", "workflow", "academic", "research", "logistics");
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
