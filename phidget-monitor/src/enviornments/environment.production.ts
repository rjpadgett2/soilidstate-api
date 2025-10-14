export const environment = {
  production: true,
  auth0: {
    domain: 'dev-rvtdlwraphwqkmke.us.auth0.com',
    clientId: 'nLWvF7BwDSiYa5O6ztyW8tjXvZ8DJLmY',
    audience: 'https://api.phidget.pasture.com',
    redirectUri: window.location.origin + '/callback'
  },
  apiUrl: 'https://api.your-production-domain.com'
};
