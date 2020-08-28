const proxyConfig = [
  {
    context: [
      '/api',
    ],
    target: 'http://localhost:8095',
    secure: false,
    logLevel: 'error',
    changeOrigin: true
  }
];

module.exports = proxyConfig;
