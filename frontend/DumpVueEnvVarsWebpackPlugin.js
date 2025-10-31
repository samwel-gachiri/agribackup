const fs = require('fs');

/**
 * We to configure the service-worker to cache calls to both the API and the
 * static content server but these are configurable URLs. We already use the env var
 * system that vue-cli offers so implementing something outside the build
 * process that parses the service-worker file would be messy. This lets us
 * dump the env vars as configured for the rest of the app and import them into
 * the service-worker script to use them.
 *
 * We need to do this as the service-worker script is NOT processed by webpack
 * so we can't put any placeholders in it directly.
 */

module.exports = class DumpVueEnvVarsWebpackPlugin {
  constructor(opts) {
    this.filename = opts.filename || 'env-vars-dump.js';
  }

  // eslint-disable-next-line no-unused-vars
  apply(compiler) {
    const fileContent = Object.keys(process.env)
      .filter((k) => k.startsWith('EX_'))
      .reduce((accum, currKey) => {
        const val = process.env[currKey];
        // eslint-disable-next-line no-param-reassign
        accum += `const ${currKey} = '${val}'\n`;
        return accum;
      }, '');
    fs.writeFileSync(`./public/${this.filename}`, fileContent);
  }
};
