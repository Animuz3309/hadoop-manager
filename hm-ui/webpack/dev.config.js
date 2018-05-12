require('babel-polyfill');
let helpers = require('./helpers');

// Webpack config for development
// we use it for build for local dev-server and for test server on 'hb.codeabvelab.com'
let fs = require('fs');
let path = require('path');
let webpack = require('webpack');
let CleanPlugin = require('clean-webpack-plugin');
let CopyWebpackPlugin = require('copy-webpack-plugin');
let HtmlWebpackPlugin = require('html-webpack-plugin');

let projectRootPath = path.resolve(__dirname, '../');
let distPath = path.resolve(projectRootPath, './dist');

let hostPub = process.env.HOST || "localhost";
let hostDev = process.env.DEV_PORT ? (hostPub.replace(/:\d+$/, "") + ":" + process.env.DEV_PORT) : null;

// https://github.com/halt-hammerzeit/webpack-isomorphic-tools
let WebpackIsomorphicToolsPlugin = require('webpack-isomorphic-tools/plugin');
let webpackIsomorphicToolsPlugin = new WebpackIsomorphicToolsPlugin(require('./webpack-isomorphic-tools'));

let babelrc = fs.readFileSync('./.babelrc');
let babelrcObject = {};

try {
  babelrcObject = JSON.parse(babelrc);
} catch (err) {
  console.error('==>     ERROR: Error parsing your .babelrc.');
  console.error(err);
}


let babelrcObjectDevelopment = babelrcObject.env && babelrcObject.env.development || {};

// merge global and dev-only plugins
let combinedPlugins = babelrcObject.plugins || [];
combinedPlugins = combinedPlugins.concat(babelrcObjectDevelopment.plugins);

let babelLoaderQuery = Object.assign({}, babelrcObjectDevelopment, babelrcObject, {plugins: combinedPlugins});
delete babelLoaderQuery.env;

// Since we use .babelrc for client and server, and we don't want HMR enabled on the server, we have to add
// the babel plugin react-transform-hmr manually here.

// make sure react-transform is enabled
babelLoaderQuery.plugins = babelLoaderQuery.plugins || [];
let reactTransform = null;
for (let i = 0; i < babelLoaderQuery.plugins.length; ++i) {
  let plugin = babelLoaderQuery.plugins[i];
  if (Array.isArray(plugin) && plugin[0] === 'react-transform') {
    reactTransform = plugin;
  }
}

if (!reactTransform) {
  reactTransform = ['react-transform', {transforms: []}];
  babelLoaderQuery.plugins.push(reactTransform);
}

if (!reactTransform[1] || !reactTransform[1].transforms) {
  reactTransform[1] = Object.assign({}, reactTransform[1], {transforms: []});
}

// make sure react-transform-hmr is enabled
reactTransform[1].transforms.push({
  transform: 'react-transform-hmr',
  imports: ['react'],
  locals: ['module']
});

module.exports = {
  devtool: 'eval',
  context: path.resolve(__dirname, '..'),
  entry: {
    "global": [
      './src/js/_global.js'
    ],
    'main': (() => {
      let arr = [
        'bootstrap-loader',
        'font-awesome-webpack!./src/theme/font-awesome.config.js',
        './src/client.js'
      ];
      if (hostDev) {
        arr.unshift('webpack-hot-middleware/client?path=http://' + hostDev + '/__webpack_hmr');
      }
      return arr;
    })()
  },
  output: {
    path: distPath,
    filename: '[name]-[hash].js',
    chunkFilename: '[name]-[chunkhash].js',
    publicPath: hostDev ? 'http://' + hostDev + '/dist/' : null
  },
  module: {
    loaders: [
        { test: require.resolve("jquery"), loader: "expose?$!expose?jQuery" },
        { test: require.resolve("tether"), loader: "expose?Tether" },
        { test: /\.css$/, loader: 'style!css'},
        { test: /\/src\/(?!js).*\.jsx?$/, exclude: /node_modules/, loaders: ['babel?' + JSON.stringify(babelLoaderQuery), 'eslint-loader']},
        { test: /\.json$/, loader: 'json-loader' },
        // Bootstrap 3
        { test: /bootstrap-sass[\/\\]assets[\/\\]javascripts[\/\\]/, loader: 'imports?jQuery=jquery' },
        { test: /\.scss$/, loader: 'style!css?modules&importLoaders=2&sourceMap&localIdentName=[local]___[hash:base64:5]!autoprefixer?browsers=last 2 version!sass?outputStyle=expanded&sourceMap' },
        { test: /\.woff(\?v=\d+\.\d+\.\d+)?$/, loader: "url?limit=10000&mimetype=application/font-woff" },
        { test: /\.woff2(\?v=\d+\.\d+\.\d+)?$/, loader: "url?limit=10000&mimetype=application/font-woff" },
        { test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, loader: "url?limit=10000&mimetype=application/octet-stream" },
        { test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, loader: "file" },
        { test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: "url?limit=10000&mimetype=image/svg+xml" },
        { test: webpackIsomorphicToolsPlugin.regular_expression('images'), loader: 'url-loader?limit=10240' }
    ]
  },
  progress: true,
  resolve: {
    modulesDirectories: [
      'src',
      'node_modules'
    ],
    extensions: ['', '.json', '.js', '.jsx']
  },
  plugins: [
    new webpack.ProvidePlugin({
      "window.Tether": "tether"
    }),
    new CleanPlugin([distPath], {root: projectRootPath}),
    // hot reload
    new webpack.HotModuleReplacementPlugin(),
    new webpack.IgnorePlugin(/webpack-stats\.json$/),
    new webpack.DefinePlugin({
      'process.env': {
        NODE_ENV: '"development"',
        API_HOST: JSON.stringify(process.env.API_HOST)
      },
      __CLIENT__: true,
      __SERVER__: false,
      __API_PROXY__: !!process.env.API_PROXY,
      __DEVELOPMENT__: true,
      __DEVTOOLS__: true  // <-------- DISABLE redux-devtools HERE
    }),
    new CopyWebpackPlugin([{
      from: './static'
    }]),
    new HtmlWebpackPlugin({
      template: 'webpack/index.html',
      chunksSortMode: helpers.packageSort(['tether', 'global', 'main'])
    }),
    webpackIsomorphicToolsPlugin.development()
  ]
};
