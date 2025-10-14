export const environment = {
  production: false,
  auth0: {
    domain: 'dev-rvtdlwraphwqkmke.us.auth0.com',
    clientId: 'nLWvF7BwDSiYa5O6ztyW8tjXvZ8DJLmY',
    audience: 'https://api.phidget.pasture.com',
    redirectUri: window.location.origin + '/callback'
  },
  apiUrl: 'http://localhost:8080'
};
