package com.campusoa.system.controller;

import com.campusoa.common.ApiResponse;
import com.campusoa.security.AuthenticatedUser;
import com.campusoa.system.exception.SystemException;
import com.campusoa.system.service.SystemAdminService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemAdminController {

    private final SystemAdminService systemAdminService;

    public SystemAdminController(SystemAdminService systemAdminService) {
        this.systemAdminService = systemAdminService;
    }

    @GetMapping("/users")
    public ApiResponse<List<Map<String, Object>>> listUsers(Authentication authentication) {
        return ApiResponse.ok(systemAdminService.listUsers(currentUser(authentication)));
    }

    @PostMapping("/users")
    public ApiResponse<Map<String, Long>> createUser(Authentication authentication, @RequestBody Map<String, Object> payload) {
        Long id = systemAdminService.createUser(currentUser(authentication), payload);
        return ApiResponse.ok("用户已创建", Map.of("id", id));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<Void> updateUser(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        systemAdminService.updateUser(currentUser(authentication), id, payload);
        return ApiResponse.ok("用户已更新", null);
    }

    @PostMapping("/users/{id}/roles")
    public ApiResponse<Void> assignUserRoles(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        List<Long> roleIds = extractIds(payload, "roleIds");
        if (roleIds.isEmpty()) {
            throw new SystemException("roleIds 不能为空");
        }
        systemAdminService.assignUserRoles(currentUser(authentication), id, roleIds);
        return ApiResponse.ok("用户角色已更新", null);
    }

    @PostMapping("/users/{id}/status")
    public ApiResponse<Void> updateUserStatus(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        Object status = payload.get("status");
        int normalized;
        try {
            normalized = status == null ? 1 : Integer.parseInt(String.valueOf(status));
        } catch (NumberFormatException exception) {
            throw new SystemException("status 参数格式错误");
        }
        systemAdminService.updateUserStatus(currentUser(authentication), id, normalized);
        return ApiResponse.ok("用户状态已更新", null);
    }

    @GetMapping("/orgs/tree")
    public ApiResponse<List<Map<String, Object>>> listOrgTree(Authentication authentication) {
        return ApiResponse.ok(systemAdminService.listOrgTree(currentUser(authentication)));
    }

    @PostMapping("/orgs")
    public ApiResponse<Map<String, Long>> createOrg(Authentication authentication, @RequestBody Map<String, Object> payload) {
        Long id = systemAdminService.createOrg(currentUser(authentication), payload);
        return ApiResponse.ok("组织已创建", Map.of("id", id));
    }

    @PutMapping("/orgs/{id}")
    public ApiResponse<Void> updateOrg(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        systemAdminService.updateOrg(currentUser(authentication), id, payload);
        return ApiResponse.ok("组织已更新", null);
    }

    @GetMapping("/roles")
    public ApiResponse<List<Map<String, Object>>> listRoles(Authentication authentication) {
        return ApiResponse.ok(systemAdminService.listRoles(currentUser(authentication)));
    }

    @PostMapping("/roles")
    public ApiResponse<Map<String, Long>> createRole(Authentication authentication, @RequestBody Map<String, Object> payload) {
        Long id = systemAdminService.createRole(currentUser(authentication), payload);
        return ApiResponse.ok("角色已创建", Map.of("id", id));
    }

    @PutMapping("/roles/{id}")
    public ApiResponse<Void> updateRole(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        systemAdminService.updateRole(currentUser(authentication), id, payload);
        return ApiResponse.ok("角色已更新", null);
    }

    @GetMapping("/menus")
    public ApiResponse<List<Map<String, Object>>> listMenus(Authentication authentication) {
        return ApiResponse.ok(systemAdminService.listMenus(currentUser(authentication)));
    }

    @PostMapping("/roles/{id}/menus")
    public ApiResponse<Void> assignRoleMenus(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        List<Long> menuIds = extractIds(payload, "menuIds");
        if (menuIds.isEmpty()) {
            throw new SystemException("menuIds 不能为空");
        }
        systemAdminService.assignRoleMenus(currentUser(authentication), id, menuIds);
        return ApiResponse.ok("角色菜单已更新", null);
    }

    @GetMapping("/permissions")
    public ApiResponse<List<Map<String, Object>>> listPermissions(Authentication authentication) {
        return ApiResponse.ok(systemAdminService.listPermissions(currentUser(authentication)));
    }

    @PostMapping("/roles/{id}/permissions")
    public ApiResponse<Void> assignRolePermissions(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        List<Long> permissionIds = extractIds(payload, "permissionIds");
        if (permissionIds.isEmpty()) {
            throw new SystemException("permissionIds 不能为空");
        }
        systemAdminService.assignRolePermissions(currentUser(authentication), id, permissionIds);
        return ApiResponse.ok("角色权限已更新", null);
    }

    @GetMapping("/workflow/definitions")
    public ApiResponse<List<Map<String, Object>>> listWorkflowDefinitions(Authentication authentication) {
        return ApiResponse.ok(systemAdminService.listWorkflowDefinitions(currentUser(authentication)));
    }

    @PostMapping("/workflow/definitions")
    public ApiResponse<Map<String, Long>> createWorkflowDefinition(
            Authentication authentication,
            @RequestBody Map<String, Object> payload
    ) {
        Long id = systemAdminService.createWorkflowDefinition(currentUser(authentication), payload);
        return ApiResponse.ok("流程模板已创建", Map.of("id", id));
    }

    @PutMapping("/workflow/definitions/{id}")
    public ApiResponse<Void> updateWorkflowDefinition(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        systemAdminService.updateWorkflowDefinition(currentUser(authentication), id, payload);
        return ApiResponse.ok("流程模板已更新", null);
    }

    @GetMapping("/workflow/definitions/{id}/nodes")
    public ApiResponse<List<Map<String, Object>>> listWorkflowNodes(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok(systemAdminService.listWorkflowNodes(currentUser(authentication), id));
    }

    @PostMapping("/workflow/definitions/{id}/nodes")
    public ApiResponse<Void> saveWorkflowNodes(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        Object nodes = payload.get("nodes");
        if (!(nodes instanceof List<?> list)) {
            throw new SystemException("nodes 参数格式错误");
        }
        if (list.isEmpty()) {
            throw new SystemException("nodes 不能为空");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodeList = (List<Map<String, Object>>) list;
        systemAdminService.saveWorkflowNodes(currentUser(authentication), id, nodeList);
        return ApiResponse.ok("流程节点已更新", null);
    }

    private AuthenticatedUser currentUser(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }

    private List<Long> extractIds(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .filter(item -> item != null && !String.valueOf(item).isBlank())
                .map(item -> {
                    try {
                        return Long.parseLong(String.valueOf(item));
                    } catch (NumberFormatException exception) {
                        throw new SystemException("ID 列表格式错误");
                    }
                })
                .toList();
    }
}
