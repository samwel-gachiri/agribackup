const CompressionPlugin = require('compression-webpack-plugin');
const DumpVueEnvVarsWebpackPlugin = require('./DumpVueEnvVarsWebpackPlugin.js');
const manifestJSON = require('./public/manifest.json');

module.exports = {
  transpileDependencies: [
    'vuetify',
    'vue-apexcharts',
    'apexcharts',
    '@stomp/stompjs',
    'marked',
    'markdown-it',
    'ethers',
    '@noble/curves',
  ],
  devServer: {
    proxy: process.env.VUE_APP_API_BASE_URL,
  },
  configureWebpack: {
    module: {
      rules: [
        {
          test: /\.mjs$/,
          include: /node_modules/,
          type: 'javascript/auto',
        },
      ],
    },
    plugins: [
      new DumpVueEnvVarsWebpackPlugin({ filename: 'sw-extended.js' }),
      new CompressionPlugin(),
    ],
  },
  chainWebpack: (config) => {
    // Handle .mjs files properly
    config.module
      .rule('mjs')
      .test(/\.mjs$/)
      .include.add(/node_modules/)
      .end()
      .type('javascript/auto');
  },
  pwa: {
    themeColor: manifestJSON.theme_color,
    name: manifestJSON.short_name,
    msTileColor: manifestJSON.background_color,
    appleMobileWebAppCapable: 'yes',
    appleMobileWebAppStatusBarStyle: 'black',
    workboxPluginMode: 'InjectManifest',
    workboxOptions: {
      swSrc: 'src/service-worker.js',
      exclude: [
        /\.map$/,
        /manifest\.json$/,
      ],
    },
  },
  // No changes needed for offline page caching if swSrc is set and /offline is part of your build
};
