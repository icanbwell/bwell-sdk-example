# b.well TypeScript SDK Sample App

This is a sample TypeScript Web application meant to be used in testing the b.well TypeScript SDK.

It is built with react, redux + redux toolkit, vite, and material-ui.

## System Requirements
- Node 22 (or higher)
  - Install via [nvm](https://github.com/nvm-sh/nvm) or direct from [node's site](https://nodejs.org/).
 
## How to run it

1. `npm i` (if you haven't run it before)
2. `npm run dev`

## How to run the tests

1. `npm run e2e`

A .env file will need to be present in this directory. See .env.example for the keys that will need to be supported.

### Docker Devcontainer

It is also recommended that you run tests in a container to ensure consistent execution across environments. If you are using VS Code, follow these steps:

1. Navigate into this folder (bwell-typescript-react)
2. Click the little green button in the lower-left corner of your screen
3. Select "Reopen in container"
4. You are now running in a Docker container. `npm run e2e` should run the tests, and a report will open in your web browser once the tests finish running.

See also: https://playwright.dev/docs/docker

## Notes

* On Health Data pages, clicking a row in a "Groups" grid will filter the rows shown in the second grid. For example, clicking "Cashew Nuts" under "Allergy Intolerance Groups" should filter the "Allergy Intolerances" grid to show only cashew-related incidents.
* On Lab and Medication pages, it should be possible to view "Knowlege" associated with a given detail row. Clicking on a row under "Labs" or "Medications" (not their corresponding Groups) will cause the "Knowledge" associated with that row to be retrieved and displayed, if available.
* Command-clicking on a selected row in one of the grids will deselect it.
* Global application state is automatically saved to local storage and rehydrated at the start of the session; this means the application will recall its state when you return to it, including page number and HTML/JSON toggle status for each grid.
