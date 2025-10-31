export default {
  clearForm(data) {
    Object.keys(data).forEach((key, index) => { // eslint-disable-line no-unused-vars
      // eslint-disable-next-line no-param-reassign
      data[key] = '';
    });
  },
  numberToString(num) {
    return Number(num).toLocaleString('en');
  },
  stringToNumber(num) {
    return parseInt(num.replace(/\D/g, ''), 10);
  },
  objIsEmpty(obj) {
    // eslint-disable-next-line no-restricted-syntax
    for (const key in obj) {
      // eslint-disable-next-line no-prototype-builtins
      if (obj.hasOwnProperty(key)) return false;
    }
    return true;
  },
  isPWA() {
    return window?.matchMedia?.('(display-mode: standalone)')?.matches;
  },
};
