module.exports = {
  root: false,
  env: {
    node: true,
  },
  extends: [
    'plugin:vue/essential',
    '@vue/airbnb',
    'plugin:sonarjs/recommended',
  ],
  plugins: [
    'sonarjs',
  ],
  rules: {
    'vue/multi-word-component-names': 'off',
    'prefer-destructuring': 'off',
    'linebreak-style': 0,
    'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'import/extensions': ['error', 'always', { // Enforce extensions
      js: 'always', // .js files don't need extensions
      mjs: 'never',
      jsx: 'never',
      ts: 'never',
      tsx: 'never',
      vue: 'always', // .vue files must have extensions
    }],
    'max-len': 0,
    // "no-unused-vars": ["error", { "argsIgnorePattern": "^_" }],
    'sonarjs/cognitive-complexity': 'off',
    'sonarjs/no-identical-functions': 'off',
    'import/no-extraneous-dependencies': 'off',
  },
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