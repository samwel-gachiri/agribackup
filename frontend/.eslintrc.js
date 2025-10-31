/* eslint-disable */
module.exports = {
  root: false,
  env: {
    node: true,
  },
  extends: [],
  plugins: [],
  rules: {},
  parserOptions: {
    parser: 'babel-eslint',
  },
  overrides: [
    {
      files: [
        '**/__tests__/*.{j,t}s?(x)',
        '**/tests/unit/**/*.spec.{j,t}s?(x)',
      ],
      env: {
        jest: true,
      },
    },
  ],
};
