package com.campusoa.workflow.controller;

import com.campusoa.common.ApiResponse;
import com.campusoa.security.AuthenticatedUser;
import com.campusoa.workflow.dto.ApplicationTypeDto;
import com.campusoa.workflow.dto.CreateApplicationRequest;
import com.campusoa.workflow.dto.WorkflowActionRequest;
import com.campusoa.workflow.dto.WorkflowApplicationDetail;
import com.campusoa.workflow.dto.WorkflowApplicationSummary;
import com.campusoa.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping("/types")
    public ApiResponse<List<ApplicationTypeDto>> listTypes() {
        return ApiResponse.ok(workflowService.listTypes());
    }

    @PostMapping("/applications")
    public ApiResponse<Map<String, Long>> createApplication(
            Authentication authentication,
            @Valid @RequestBody CreateApplicationRequest request
    ) {
        AuthenticatedUser currentUser = (AuthenticatedUser) authentication.getPrincipal();
        Long applicationId = workflowService.createDraft(currentUser, request);
        return ApiResponse.ok("申请草稿已创建", Map.of("id", applicationId));
    }

    @PostMapping("/applications/{id}/submit")
    public ApiResponse<Void> submitApplication(Authentication authentication, @PathVariable Long id) {
        AuthenticatedUser currentUser = (AuthenticatedUser) authentication.getPrincipal();
        workflowService.submit(currentUser, id);
        return ApiResponse.ok("申请已提交", null);
    }

    @GetMapping("/applications")
    public ApiResponse<List<WorkflowApplicationSummary>> listApplications(Authentication authentication) {
        AuthenticatedUser currentUser = (AuthenticatedUser) authentication.getPrincipal();
        return ApiResponse.ok(workflowService.listMyApplications(currentUser));
    }

    @GetMapping("/applications/{id}")
    public ApiResponse<WorkflowApplicationDetail> getApplicationDetail(Authentication authentication, @PathVariable Long id) {
        AuthenticatedUser currentUser = (AuthenticatedUser) authentication.getPrincipal();
        return ApiResponse.ok(workflowService.getDetail(currentUser, id));
    }

    @GetMapping("/todos")
    public ApiResponse<List<WorkflowApplicationSummary>> listTodos(Authentication authentication) {
        AuthenticatedUser currentUser = (AuthenticatedUser) authentication.getPrincipal();
        return ApiResponse.ok(workflowService.listTodos(currentUser));
    }

    @PostMapping("/applications/{id}/approve")
    public ApiResponse<Void> approve(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody(required = false) WorkflowActionRequest request
    ) {
        AuthenticatedUser currentUser = (AuthenticatedUser) authentication.getPrincipal();
        workflowService.approve(currentUser, id, request == null ? null : request.comment());
        return ApiResponse.ok("审批通过", null);
    }

    @PostMapping("/applications/{id}/reject")
    public ApiResponse<Void> reject(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody(required = false) WorkflowActionRequest request
    ) {
        AuthenticatedUser currentUser = (AuthenticatedUser) authentication.getPrincipal();
        workflowService.reject(currentUser, id, request == null ? null : request.comment());
        return ApiResponse.ok("审批已驳回", null);
    }

    @PostMapping("/applications/{id}/withdraw")
    public ApiResponse<Void> withdraw(Authentication authentication, @PathVariable Long id) {
        AuthenticatedUser currentUser = (AuthenticatedUser) authentication.getPrincipal();
        workflowService.withdraw(currentUser, id);
        return ApiResponse.ok("申请已撤回", null);
    }
}
