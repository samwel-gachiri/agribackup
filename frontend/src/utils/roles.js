// eslint-disable-next-line import/extensions
import store from '@/store';

const getUser = () => store.getters['auth/authenticatedUser'];
const getRole = () => store.getters['auth/role'];

/**
 * Check if user has permission to view based on allowed roles
 * @param {string[]} roles - Array of allowed roles (UPPERCASE)
 * @returns {boolean} - true if user has permission
 */
const viewPermissions = (roles = []) => {
  // Empty roles array = no one can view (use ['*'] for everyone)
  if (roles.length === 0) {
    return false;
  }

  // Wildcard = everyone can view
  if (roles.includes('*') || roles.includes('anybody') || roles.includes('ANYBODY')) {
    return true;
  }

  const userRole = getRole();
  if (!userRole) {
    return false; // Not logged in
  }

  // Check if user's role is in allowed roles (case-insensitive comparison)
  return roles.some((role) => role.toUpperCase() === userRole.toUpperCase());
};

const getCurrentUserRole = () => getRole();

const getCurrentUserId = () => {
  const user = getUser();
  if (user == null) return '';
  return user.id || user.uid;
};

const isAuthenticated = () => {
  const user = getUser();
  return user != null;
};

export {
  viewPermissions, getCurrentUserRole, getCurrentUserId, isAuthenticated,
};
