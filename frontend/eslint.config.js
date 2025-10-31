// Flat ESLint configuration (migrated from farmer-portal-frontend/.eslintrc.js)
// Applies to the Vue 2 (Vue CLI) project in farmer-portal-frontend
// eslint-disable-next-line import/no-unresolved
import js from '@eslint/js';
import pluginVue from 'eslint-plugin-vue';
import sonarjs from 'eslint-plugin-sonarjs';
import importPlugin from 'eslint-plugin-import';

const isProd = process.env.NODE_ENV === 'production';

export default [
  {
    ignores: [
      'dist/**',
      'build/**',
      'coverage/**',
      'node_modules/**',
      '.vite/**',
    ],
  },
  js.configs.recommended,
  {
    files: ['**/*.{js,vue}'],
    languageOptions: {
      ecmaVersion: 2021,
      sourceType: 'module',
      parser: require.resolve('@babel/eslint-parser'),
      parserOptions: {
        requireConfigFile: false,
        babelOptions: {
          presets: ['@vue/cli-plugin-babel/preset'],
        },
      },
      globals: {
        window: 'readonly',
        document: 'readonly',
        navigator: 'readonly',
        console: 'readonly',
      },
    },
    plugins: {
      vue: pluginVue,
      sonarjs,
      import: importPlugin,
    },
    processor: pluginVue.processors['.vue'],
    rules: {
      'vue/multi-word-component-names': 'off',
      'prefer-destructuring': 'off',
      'linebreak-style': 'off',
      'no-console': isProd ? 'error' : 'off',
      'no-debugger': isProd ? 'error' : 'off',
      'max-len': 'off',
      'sonarjs/cognitive-complexity': 'off',
      'sonarjs/no-identical-functions': 'off',
      'import/no-extraneous-dependencies': 'off',
      'import/extensions': ['error', 'always', {
        js: 'always',
        mjs: 'never',
        jsx: 'never',
        ts: 'never',
        tsx: 'never',
        vue: 'always',
      }],
    },
  },
  {
    files: [
      '**/__tests__/*.{j,t}s?(x)',
      '**/tests/unit/**/*.spec.{j,t}s?(x)',
    ],
    languageOptions: {
      globals: { jest: 'readonly' },
    },
  },
];
