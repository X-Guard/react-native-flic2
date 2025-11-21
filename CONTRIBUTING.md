# Contributing

Contributions are always welcome, no matter how large or small!

We want this community to be friendly and respectful to each other. Please follow it in all your interactions with the project. Before contributing, please read the [code of conduct](./CODE_OF_CONDUCT.md).

## Development workflow

To get started with the project, make sure you have the correct version of [Node.js](https://nodejs.org/) installed. See the [`.nvmrc`](./.nvmrc) file for the version used in this project.

Run `npm install` in the root directory to install the required dependencies:

```sh
npm install
```

This is a React Native library package. To test your changes, you'll need to integrate the library into a React Native app. You can:

1. Link the library locally in your test app using `npm link` or by pointing to the local path
2. Use the library in your own React Native project and test changes there

If you want to use Android Studio or Xcode to edit the native code:
- To edit the Objective-C or Swift files, open your React Native app's iOS project in Xcode and find the source files in the `react-native-flic2` pod
- To edit the Java or Kotlin files, open your React Native app's `android` directory in Android Studio and find the source files at `react-native-flic2` under the Android project structure

Make sure your code passes TypeScript and ESLint. Run the following to verify:

```sh
npm run typecheck
npm run lint
```

To fix formatting errors, run the following:

```sh
npm run lint -- --fix
```

Remember to add tests for your change if possible. Currently, the project uses TypeScript for type checking and ESLint for code quality.

### Linting and tests

[ESLint](https://eslint.org/), [Prettier](https://prettier.io/), [TypeScript](https://www.typescriptlang.org/)

We use [TypeScript](https://www.typescriptlang.org/) for type checking, [ESLint](https://eslint.org/) with [Prettier](https://prettier.io/) for linting and formatting the code.

### Publishing to npm

Releases to npm are handled exclusively by the code owners. Contributors should not attempt to publish new versions.

### Scripts

The `package.json` file contains various scripts for common tasks:

- `npm install`: setup project by installing dependencies.
- `npm run typecheck`: type-check files with TypeScript.
- `npm run lint`: lint files with ESLint.
- `npm run lint -- --fix`: automatically fix linting and formatting errors.
- `npm run clean`: clean build artifacts.
- `npm run prepare`: build the library (runs automatically on install).

### Sending a pull request

> **Working on your first pull request?** You can learn how from this _free_ series: [How to Contribute to an Open Source Project on GitHub](https://app.egghead.io/playlists/how-to-contribute-to-an-open-source-project-on-github).

When you're sending a pull request:

- Prefer small pull requests focused on one change.
- Verify that linters and tests are passing.
- Review the documentation to make sure it looks good.
- Follow the pull request template when opening a pull request.
- For pull requests that change the API or implementation, discuss with maintainers first by opening an issue.
