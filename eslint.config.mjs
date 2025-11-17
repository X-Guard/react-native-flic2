import { fixupConfigRules } from '@eslint/compat';
import { FlatCompat } from '@eslint/eslintrc';
import js from '@eslint/js';
import prettier from 'eslint-plugin-prettier';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import tseslint from '@typescript-eslint/eslint-plugin';
import tsparser from '@typescript-eslint/parser';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
  baseDirectory: __dirname,
  recommendedConfig: js.configs.recommended,
  allConfig: js.configs.all,
});

export default [
  ...fixupConfigRules(compat.extends('@react-native', 'prettier')),
  {
    plugins: { prettier },
    rules: {
      'prettier/prettier': 'off',
      'react/react-in-jsx-scope': 'off',
      // Enforce and autofix two-space indentation across the project
      indent: ['error', 2, { SwitchCase: 1, MemberExpression: 1 }],
      'no-mixed-spaces-and-tabs': 'error',
      'object-curly-newline': [
        'error',
        {
          ObjectExpression: { multiline: true, minProperties: 2 },
          ObjectPattern: { multiline: true, minProperties: 2 },
          ImportDeclaration: { multiline: true, minProperties: 2 },
          ExportDeclaration: { multiline: true, minProperties: 2 },
        },
      ],
      'brace-style': 'off', // Disable to allow custom formatting
      'lines-between-class-members': [
        'error',
        'always',
        { exceptAfterSingleLine: false },
      ],
    },
  },
  {
    files: ['**/*.ts', '**/*.tsx'],
    rules: {
      // Enforce newline after opening brace
      'padded-blocks': ['error', 'always'],
      // Ensure TS files also respect two-space indentation
      indent: ['error', 2, { SwitchCase: 1, MemberExpression: 1 }],
    },
  },
  {
    files: ['**/*.ts', '**/*.tsx'],
    languageOptions: {
      parser: tsparser,
      parserOptions: {
        project: './tsconfig.json',
      },
    },
  },
  {
    ignores: [
      'lib/',
      'node_modules/',
      'android-lib-docs/',
      'old-implementation/',
    ],
  },
];
