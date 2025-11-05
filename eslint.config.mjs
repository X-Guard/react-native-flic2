import { fixupConfigRules } from '@eslint/compat';
import { FlatCompat } from '@eslint/eslintrc';
import js from '@eslint/js';
import prettier from 'eslint-plugin-prettier';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

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
      'prettier/prettier': 'error',
      'react/react-in-jsx-scope': 'off',
      'object-curly-newline': [
        'error',
        {
          ObjectExpression: { multiline: true, minProperties: 2 },
          ObjectPattern: { multiline: true, minProperties: 2 },
          ImportDeclaration: { multiline: true, minProperties: 2 },
          ExportDeclaration: { multiline: true, minProperties: 2 },
        },
      ],
    },
  },
  {
    ignores: [
      'node_modules/',
      'android-lib-docs/',
      'old-project-example/',
    ],
  },
];
