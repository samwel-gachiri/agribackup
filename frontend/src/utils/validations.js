/* eslint no-mixed-operators:0 */
/* eslint no-shadow:0 */
import helpers from '@/utils/helpers.js';

const required = (propertyType) => (v) => v && v.length > 0 || `${propertyType} field is required`;

const minLength = (propertyType, minLength) => (v) => (v === '' || v && v.length >= minLength) || `${propertyType} must not be less than ${minLength} characters`;

const confirmPassword = (propertyType, password) => (v) => v && v === password || `${propertyType} must be same as entered password`;

const maxLength = (propertyType, maxLength) => (v) => v && v.length <= maxLength || `${propertyType} must be less than ${maxLength}`;

const max = (propertyType, max) => (v) => v && v <= max || `${propertyType} must not be more than ${max}`;

const min = (propertyType, min) => (v) => v && v >= min || `${propertyType} must be ${min} or more`;

const minAmount = (propertyType, num) => (v) => helpers.stringToNumber(v) && helpers.stringToNumber(v) >= num || `${propertyType} must be Ksh ${helpers.numberToString(num)} or more`;

const emailFormat = () => {
  const regex = /^(([^<>()[\]\\.,;:\s@"]+([^.<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[([0-9]{1,3}\.){3}[0-9]{1,3}\])|(([a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}))$/;

  return (v) => !v || regex.test(v) || 'Must be a valid email';
};

const mobileFormat = () => {
  const regex = /^\+254\d{9}$/; // [- ]?
  return (v) => v && regex.test(v) || 'Must start with +254...';
};

const kraPinFormat = () => {
  const regex = /^a\d{9}[a-z]$/i;
  return (v) => v && regex.test(v) || 'Must be a valid KRA PIN';
};

const numberFormat = () => {
  const regex = /^[0-9]+$/;
  return (v) => v && regex.test(v) || 'Must be a valid number';
};

const lowerCaseFormat = () => {
  const regex = /^(.*[a-z].*)$/;
  return (v) => v && regex.test(v) || 'Must have at least 1 lowercase character';
};
const upperCaseFormat = () => {
  const regex = /^(.*[A-Z].*)$/;
  return (v) => v && regex.test(v) || 'Must have at least 1 uppercase character';
};

const digitFormat = () => {
  const regex = /^(.*[0-9].*)$/;
  return (v) => v && regex.test(v) || 'Must have at least 1 digit';
};

const specialCharFormat = () => {
  const regex = /^(.*[!@#$%^&*)(+=.,_-].*)$/;
  return (v) => v && regex.test(v) || 'Must have at least 1 special character';
};

const numberPlateFormat = () => {
  const regex = /^[A-Za-z]{3}.*?[0-9]{3}.*?[A-Za-z]$/;
  return (v) => v && regex.test(v) || 'Registration Number must be valid';
};

const isObjectField = () => (v) => v && typeof v === 'object' || 'Must be valid object';

const check = () => (v) => v === true || 'You must agree to continue!';

const phoneRules = () => [
  (value) => !!value || 'Phone number is required.',
  (value) => /^\d{10}$/.test(value) || 'Invalid phone number format.',
];

const noDigitFormat = () => (v) => !/\d/.test(v) || 'Name should not contain numbers';

export default {
  required,
  minLength,
  maxLength,
  emailFormat,
  numberFormat,
  kraPinFormat,
  confirmPassword,
  check,
  mobileFormat,
  lowerCaseFormat,
  minAmount,
  max,
  min,
  isObjectField,
  numberPlateFormat,
  upperCaseFormat,
  digitFormat,
  specialCharFormat,
  phoneRules,
  noDigitFormat,
};
