export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface HealthData {
  service: string;
  status: string;
  timestamp: string;
}

export interface UserProfile {
  userId: number;
  username: string;
  realName: string;
  userType: string;
  roles: string[];
}

export interface LoginResponse {
  token: string;
  profile: UserProfile;
  menus: string[];
  permissions: string[];
}

export interface ProfileResponse {
  profile: UserProfile;
  menus: string[];
  permissions: string[];
}

export interface ApplicationType {
  id: number;
  typeCode: string;
  typeName: string;
  description: string;
  approverRoleCode: string;
}

export interface WorkflowApplicationSummary {
  id: number;
  applicationNo: string;
  typeId: number;
  typeName: string;
  title: string;
  status: string;
  applicantName: string;
  currentApproverName: string | null;
  submittedAt: string | null;
  updatedAt: string;
}

export interface ApprovalRecord {
  id: number;
  actionType: string;
  actorName: string;
  comment: string | null;
  createdAt: string;
}

export interface WorkflowApplicationDetail {
  id: number;
  applicationNo: string;
  typeId: number;
  typeName: string;
  title: string;
  content: string;
  status: string;
  applicantId: number;
  applicantName: string;
  currentApproverId: number | null;
  currentApproverName: string | null;
  submittedAt: string | null;
  finishedAt: string | null;
  createdAt: string;
  updatedAt: string;
  canSubmit: boolean;
  canWithdraw: boolean;
  canApprove: boolean;
  canReject: boolean;
  records: ApprovalRecord[];
}

export interface BusinessFieldValue {
  key: string;
  label: string;
  value: string;
}

export interface BusinessRecordSummary {
  id: number;
  title: string;
  status: string;
  applicantName: string;
  currentApproverName: string | null;
  submittedAt: string | null;
  updatedAt: string;
}

export interface BusinessRecordDetail {
  id: number;
  businessKey: string;
  businessName: string;
  title: string;
  fields: BusinessFieldValue[];
  workflow: WorkflowApplicationDetail;
}

export interface BusinessSelectOption {
  value: string;
  label: string;
}

export interface SystemUserRecord {
  id: number;
  username: string;
  realName: string;
  userType: string;
  orgId: number | null;
  orgName: string | null;
  phone: string | null;
  email: string | null;
  status: number;
  roleCodes: string[];
  roleNames: string[];
  roleIds: number[];
  createdAt: string;
  updatedAt: string;
}

export interface OrgNode {
  id: number;
  parentId: number | null;
  orgCode: string;
  orgName: string;
  orgType: string;
  sortNo: number;
  status: number;
  children: OrgNode[];
}

export interface SystemRoleRecord {
  id: number;
  roleCode: string;
  roleName: string;
  status: number;
  assignedMenuIds: number[];
  assignedPermissionIds: number[];
}

export interface SystemMenuRecord {
  id: number;
  parentId: number | null;
  menuName: string;
  menuType: string;
  routePath: string | null;
  permissionCode: string | null;
  sortNo: number;
  status: number;
}

export interface SystemPermissionRecord {
  id: number;
  permissionCode: string;
  permissionName: string;
  permissionGroup: string | null;
  status: number;
}

export interface AttachmentRecord {
  id: number;
  businessType: string;
  businessId: number;
  fileName: string;
  fileSize: number;
  contentType: string | null;
  uploadedBy: number;
  uploadedByName: string;
  createdAt: string;
  updatedAt: string;
}

export interface WorkflowDefinitionRecord {
  id: number;
  businessType: string;
  definitionCode: string;
  definitionName: string;
  versionNo: number;
  status: number;
  nodeCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface WorkflowNodeRecord {
  id: number;
  nodeCode: string;
  nodeName: string;
  nodeType: string;
  approverType: string;
  approverRoleCode: string;
  sortNo: number;
  status: number;
}

const API_BASE_URL = '/api';

function getToken() {
  return localStorage.getItem('campus_oa_token') ?? '';
}

export async function request<T>(path: string, init?: RequestInit): Promise<ApiResponse<T>> {
  const headers = new Headers(init?.headers ?? {});
  if (getToken() && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${getToken()}`);
  }
  if (!(init?.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers
    });
  } catch {
    throw new Error('无法连接后端服务，请确认 Spring Boot 已在 8080 端口启动');
  }

  const result = await parseApiResponse<T>(response);

  if (!response.ok || !result.success) {
    throw new Error(result.message || `请求失败，HTTP 状态码 ${response.status}`);
  }

  return result;
}

async function parseApiResponse<T>(response: Response): Promise<ApiResponse<T>> {
  const text = await response.text();
  if (!text) {
    return {
      success: response.ok,
      message: response.ok ? 'OK' : `请求失败，HTTP 状态码 ${response.status}`,
      data: null as T
    };
  }

  try {
    return JSON.parse(text) as ApiResponse<T>;
  } catch {
    return {
      success: false,
      message: response.ok ? '后端返回了非 JSON 数据' : `请求失败，HTTP 状态码 ${response.status}`,
      data: null as T
    };
  }
}

export function getHealth() {
  return request<HealthData>('/health');
}

export function login(username: string, password: string) {
  return request<LoginResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  });
}

export function getProfile() {
  return request<ProfileResponse>('/auth/profile');
}

export function logout() {
  return request<null>('/auth/logout', {
    method: 'POST'
  });
}

export function getWorkflowTypes() {
  return request<ApplicationType[]>('/workflow/types');
}

export function createWorkflowApplication(payload: { typeId: number; title: string; content: string }) {
  return request<{ id: number }>('/workflow/applications', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function submitWorkflowApplication(id: number) {
  return request<null>(`/workflow/applications/${id}/submit`, {
    method: 'POST'
  });
}

export function listMyWorkflowApplications() {
  return request<WorkflowApplicationSummary[]>('/workflow/applications');
}

export function getWorkflowApplicationDetail(id: number) {
  return request<WorkflowApplicationDetail>(`/workflow/applications/${id}`);
}

export function listWorkflowTodos() {
  return request<WorkflowApplicationSummary[]>('/workflow/todos');
}

export function approveWorkflowApplication(id: number, comment: string) {
  return request<null>(`/workflow/applications/${id}/approve`, {
    method: 'POST',
    body: JSON.stringify({ comment })
  });
}

export function rejectWorkflowApplication(id: number, comment: string) {
  return request<null>(`/workflow/applications/${id}/reject`, {
    method: 'POST',
    body: JSON.stringify({ comment })
  });
}

export function withdrawWorkflowApplication(id: number) {
  return request<null>(`/workflow/applications/${id}/withdraw`, {
    method: 'POST'
  });
}

export function listBusinessRecords(businessKey: string, status?: string) {
  const search = status ? `?status=${encodeURIComponent(status)}` : '';
  return request<BusinessRecordSummary[]>(`/business/${businessKey}${search}`);
}

export function createBusinessRecord(businessKey: string, payload: Record<string, string>) {
  return request<{ id: number }>(`/business/${businessKey}`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function submitBusinessRecord(businessKey: string, id: number) {
  return request<null>(`/business/${businessKey}/${id}/submit`, {
    method: 'POST'
  });
}

export function getBusinessRecordDetail(businessKey: string, id: number) {
  return request<BusinessRecordDetail>(`/business/${businessKey}/${id}`);
}

export function getBusinessFieldOptions(businessKey: string, fieldKey: string) {
  return request<BusinessSelectOption[]>(`/business/${businessKey}/options/${fieldKey}`);
}

export function listSystemUsers() {
  return request<SystemUserRecord[]>('/system/users');
}

export function createSystemUser(payload: Record<string, unknown>) {
  return request<{ id: number }>('/system/users', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function updateSystemUser(id: number, payload: Record<string, unknown>) {
  return request<null>(`/system/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export function assignSystemUserRoles(id: number, roleIds: number[]) {
  return request<null>(`/system/users/${id}/roles`, {
    method: 'POST',
    body: JSON.stringify({ roleIds })
  });
}

export function updateSystemUserStatus(id: number, status: number) {
  return request<null>(`/system/users/${id}/status`, {
    method: 'POST',
    body: JSON.stringify({ status })
  });
}

export function listSystemOrgs() {
  return request<OrgNode[]>('/system/orgs/tree');
}

export function createSystemOrg(payload: Record<string, unknown>) {
  return request<{ id: number }>('/system/orgs', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function updateSystemOrg(id: number, payload: Record<string, unknown>) {
  return request<null>(`/system/orgs/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export function listSystemRoles() {
  return request<SystemRoleRecord[]>('/system/roles');
}

export function createSystemRole(payload: Record<string, unknown>) {
  return request<{ id: number }>('/system/roles', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function updateSystemRole(id: number, payload: Record<string, unknown>) {
  return request<null>(`/system/roles/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export function listSystemMenus() {
  return request<SystemMenuRecord[]>('/system/menus');
}

export function assignSystemRoleMenus(id: number, menuIds: number[]) {
  return request<null>(`/system/roles/${id}/menus`, {
    method: 'POST',
    body: JSON.stringify({ menuIds })
  });
}

export function listSystemPermissions() {
  return request<SystemPermissionRecord[]>('/system/permissions');
}

export function assignSystemRolePermissions(id: number, permissionIds: number[]) {
  return request<null>(`/system/roles/${id}/permissions`, {
    method: 'POST',
    body: JSON.stringify({ permissionIds })
  });
}

export function listAttachments(businessType?: string, businessId?: number) {
  const search = new URLSearchParams();
  if (businessType) {
    search.set('businessType', businessType);
  }
  if (typeof businessId === 'number' && Number.isFinite(businessId)) {
    search.set('businessId', String(businessId));
  }
  const query = search.toString();
  return request<AttachmentRecord[]>(`/files${query ? `?${query}` : ''}`);
}

export function uploadAttachment(businessType: string, businessId: number, file: File) {
  const formData = new FormData();
  formData.append('businessType', businessType);
  formData.append('businessId', String(businessId));
  formData.append('file', file);
  return request<{ id: number }>('/files/upload', {
    method: 'POST',
    body: formData
  });
}

export function deleteAttachment(id: number) {
  return request<null>(`/files/${id}`, {
    method: 'DELETE'
  });
}

export async function downloadAttachment(id: number, fileName: string) {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/files/${id}/download`, {
      headers: {
        ...(getToken() ? { Authorization: `Bearer ${getToken()}` } : {})
      }
    });
  } catch {
    throw new Error('附件下载失败，无法连接后端服务');
  }

  if (!response.ok) {
    const result = await parseApiResponse<null>(response);
    throw new Error(result.message || '附件下载失败');
  }

  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

export function listWorkflowDefinitions() {
  return request<WorkflowDefinitionRecord[]>('/system/workflow/definitions');
}

export function createWorkflowDefinition(payload: Record<string, unknown>) {
  return request<{ id: number }>('/system/workflow/definitions', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function updateWorkflowDefinition(id: number, payload: Record<string, unknown>) {
  return request<null>(`/system/workflow/definitions/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export function listWorkflowNodes(id: number) {
  return request<WorkflowNodeRecord[]>(`/system/workflow/definitions/${id}/nodes`);
}

export function saveWorkflowNodes(id: number, nodes: Record<string, unknown>[]) {
  return request<null>(`/system/workflow/definitions/${id}/nodes`, {
    method: 'POST',
    body: JSON.stringify({ nodes })
  });
}
