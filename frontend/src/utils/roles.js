// eslint-disable-next-line import/extensions
import store from '@/store';

const getUser = () => store.getters['auth/authenticatedUser'];
const getRole = () => store.getters['auth/role'];
const viewPermissions = (roles = []) => (roles.length === 0
  ? false
  : [...roles].includes(getRole()));
// const user = getUser();
// if (user == null) {
//   return roles.length === 0
//     ? false
//     : [...roles].includes('anybody');
// }
// return roles.length === 0
//   ? false
//   : [...roles].includes(getUser()['custom:role']);
// // : [...roles, ...['admin']].includes(getUser()['custom:role']);
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
