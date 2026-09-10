/**
 * Centralized localStorage key names used across the app, so services
 * don't have to duplicate/guess string literals.
 */
export const STORAGE_KEYS = {
  authToken: 'lifelink.authToken',
  coordinatorId: 'lifelink.coordinatorId',
  approvedMatchIds: 'lifelink.approvedMatchIds'
} as const;
