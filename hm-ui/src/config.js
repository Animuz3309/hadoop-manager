require('babel-polyfill');

const environment = {
  development: {
    isProduction: false
  },
  production: {
    isProduction: true
  }
}[process.env.NODE_ENV || 'development'];

const apiHost = process.env.API_HOST || typeof window !== 'undefined' && window.location.host || 'localhost' || null;

module.exports = Object.assign({
  host: process.env.HOST || 'localhost',
  port: process.env.PORT,
  apiHost: apiHost,
  eventServer: apiHost + "/ws/stomp",
  mock: false,
  app: {
    title: 'Hadoop Manager Tool',
    head: {
      titleTemplate: 'Hadoop Manager Tool: %s',
      meta: [
          {charset: 'utf-8'}
      ]
    }
  }
}, environment);
