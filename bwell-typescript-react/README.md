# b.well TypeScript SDK Sample App

This is a sample TypeScript Web application meant to be used in testing the b.well TypeScript SDK.

It is built with react, redux + redux toolkit, vite, and material-ui.

## How to run it

1. `npm i` (if you haven't run it before)
2. `npm run dev`

## How to run run the tests

1. `npm run e2e`

A .env file will need to be present in this directory. See .env.example for the keys that will need to be supported.

### Docker Devcontainer

It is also recommended that you run tests in a container to ensure consistent execution across environments. If you are using VS Code, follow these steps:

1. Navigate into this folder (bwell-typescript-react)
2. Click the little green button in the lower-left corner of your screen
3. Select "Reopen in container"
4. You are now running in a Docker container. `npm run e2e` should run the tests, and a report will open in your web browser once the tests finish running.

See also: https://playwright.dev/docs/docker

## How to test SDK code under development

Once code in `bwell-sdk` has been merged to main, it should be testable from the published NPM package. In the event that you want to test code that has not yet merged, you'll need to publish the SDK to a local NPM registry and make sure this application uses it from there.

1. Clone the `bwell-sdk` repo, checkout the branch you want to test, and make sure you can build.
1. `npm i - g yalc`
2. In the the `bwell-sdk` repo's root folder, run `yalc publish --push`
4. In the root of the folder this README is in, run `yalc add @icanbwell/bwell-sdk-ts`, then `npm i`
5. `npm run dev` 

Note that this will change the `package.json` entry for `@icanbwell/bwell-sdk-ts` to something like this:

`"@icanbwell/bwell-sdk-ts": "file:.yalc/@icanbwell/bwell-sdk-ts"`

In order to change it back, you can run:

`yalc remove @icanbwell/bwell-sdk-ts`
`npm i`