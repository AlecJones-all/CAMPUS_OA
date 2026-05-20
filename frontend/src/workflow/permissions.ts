const GENERIC_WORKFLOW_CREATE_ROLES = new Set(['ADMIN', 'STUDENT', 'TEACHER', 'ADVISER', 'OFFICE', 'RESEARCH'])

export function canCreateGenericWorkflow(roles: string[]): boolean {
  return roles.some((role) => GENERIC_WORKFLOW_CREATE_ROLES.has(role))
}
