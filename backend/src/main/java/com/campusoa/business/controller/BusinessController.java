package com.campusoa.business.controller;

import com.campusoa.business.dto.BusinessRecordDetail;
import com.campusoa.business.dto.BusinessRecordSummary;
import com.campusoa.business.dto.BusinessSelectOption;
import com.campusoa.business.service.BusinessService;
import com.campusoa.common.ApiResponse;
import com.campusoa.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private static final Logger log = LoggerFactory.getLogger(BusinessController.class);

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping("/{businessKey}")
    public ApiResponse<List<BusinessRecordSummary>> list(
            Authentication authentication,
            @PathVariable String businessKey,
            @RequestParam(required = false) String status
    ) {
        AuthenticatedUser currentUser = (AuthenticatedUser) authentication.getPrincipal();
        log.info("List business records. businessKey={}, userId={}, status={}",
                businessKey, currentUser.userId(), status);
        return ApiResponse.ok(businessService.list(currentUser, businessKey, status));
    }

    @PostMapping("/{businessKey}")
    public ApiResponse<Map<String, Long>> create(
            Authentication authentication,
            @PathVariable String businessKey,
            @RequestBody Map<String, Object> payload
    ) {
        AuthenticatedUser currentUser = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Create business draft. businessKey={}, userId={}, payloadKeys={}",
                businessKey, currentUser.userId(), payload.keySet());
        Long id = businessService.createDraft(currentUser, businessKey, payload);
        return ApiResponse.ok("业务草稿已创建", Map.of("id", id));
    }

    @GetMapping("/{businessKey}/options/{fieldKey}")
    public ApiResponse<List<BusinessSelectOption>> fieldOptions(
            Authentication authentication,
            @PathVariable String businessKey,
            @PathVariable String fieldKey
    ) {
        AuthenticatedUser currentUser = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Load business field options. businessKey={}, fieldKey={}, userId={}",
                businessKey, fieldKey, currentUser.userId());
        return ApiResponse.ok(businessService.listFieldOptions(currentUser, businessKey, fieldKey));
    }

    @PostMapping("/{businessKey}/{id}/submit")
    public ApiResponse<Void> submit(
            Authentication authentication,
            @PathVariable String businessKey,
            @PathVariable Long id
    ) {
        AuthenticatedUser currentUser = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Submit business application. businessKey={}, businessId={}, userId={}",
                businessKey, id, currentUser.userId());
        businessService.submit(currentUser, businessKey, id);
        return ApiResponse.ok("业务申请已提交", null);
    }

    @GetMapping("/{businessKey}/{id}")
    public ApiResponse<BusinessRecordDetail> detail(
            Authentication authentication,
            @PathVariable String businessKey,
            @PathVariable Long id
    ) {
        AuthenticatedUser currentUser = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Load business detail. businessKey={}, businessId={}, userId={}",
                businessKey, id, currentUser.userId());
        return ApiResponse.ok(businessService.detail(currentUser, businessKey, id));
    }
}
