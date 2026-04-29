# 接口设计初稿

## 技术口径

- 前端：`Vue 3 + Vite + TypeScript + Element Plus`
- 后端：`Spring Boot`
- 数据库：`MySQL`
- 文档：`Markdown`

## 1. 通用返回结构

```json
{
  "success": true,
  "message": "OK",
  "data": {}
}
```

## 2. 基础平台接口

### 认证与用户

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/profile`
- `GET /api/system/users`
- `POST /api/system/users`
- `GET /api/system/roles`
- `GET /api/system/orgs`

### 流程中心

- `GET /api/workflow/definitions`
- `POST /api/workflow/definitions`
- `POST /api/workflow/instances/start`
- `POST /api/workflow/tasks/{taskId}/approve`
- `POST /api/workflow/tasks/{taskId}/reject`
- `POST /api/workflow/tasks/{taskId}/transfer`
- `GET /api/workflow/instances/{instanceId}/timeline`

### 附件与消息

- `POST /api/files/upload`
- `GET /api/files/{id}`
- `GET /api/messages/notices`
- `POST /api/messages/notices/{id}/read`

## 3. 业务模块接口

### 学生事务

- `GET /api/student-affairs/leave-requests`
- `POST /api/student-affairs/leave-requests`
- `GET /api/student-affairs/internships`
- `POST /api/student-affairs/internships`
- `GET /api/student-affairs/alerts`

### 教学教务

- `GET /api/academic/course-standard-reviews`
- `POST /api/academic/course-standard-reviews`
- `GET /api/academic/schedule-adjustments`
- `POST /api/academic/schedule-adjustments`

### 科研管理

- `GET /api/research/project-applications`
- `POST /api/research/project-applications`

### 行政后勤

- `GET /api/logistics/meeting-room-bookings`
- `POST /api/logistics/meeting-room-bookings`
- `GET /api/logistics/maintenance-tickets`
- `POST /api/logistics/maintenance-tickets`

## 4. 统一列表查询约定

- 支持分页参数：`pageNo`、`pageSize`
- 支持状态筛选：`status`
- 支持时间范围：`startDate`、`endDate`
- 支持组织维度：`orgId`
- 支持关键字：`keyword`

## 5. 表单详情返回约定

业务详情接口应统一包含以下结构：

```json
{
  "formData": {},
  "attachments": [],
  "workflow": {
    "instanceId": 1,
    "status": "IN_PROGRESS",
    "timeline": []
  },
  "permissions": {
    "canEdit": false,
    "canApprove": false,
    "canWithdraw": false
  }
}
```
