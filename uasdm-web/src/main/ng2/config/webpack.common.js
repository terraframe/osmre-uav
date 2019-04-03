var webpack = require('webpack');
var helpers = require('./helpers');
var ngToolsWebpack = require('@ngtools/webpack');

module.exports = {
  entry: {
    'uasdm-polyfills': './src/polyfills.ts',
    'uasdm-vendor': './src/vendor.ts',
    'uasdm-app': './src/main.ts'
  },

  resolve: {
    extensions: ['.ts', '.js']
  },
  node: {
	    fs: "empty"
  },
  module: {
    rules: [
      {
          test: /(?:\.ngfactory\.js|\.ngstyle\.js|\.ts)$/,
          loader: '@ngtools/webpack'
      },      
      {
        test: /\.html$/,
        loader: 'html-loader'
      },
      {
        test: /\.(png|jpe?g|gif|svg|woff|woff2|ttf|eot|ico)$/,
        loader: 'file-loader?name=assets/[name].[hash].[ext]'
      },
      {
        test: /\.css$/,
        include: helpers.root('src', 'app'),
        loader: 'raw-loader'
      },
      {
        test: /\.scss$/,
        use: [
            "style-loader", // creates style nodes from JS strings
            "css-loader", // translates CSS into CommonJS
            "sass-loader" // compiles Sass to CSS, using Node Sass by default
        ]
      }      
    ]
  },

  plugins: [
    // Workaround for angular/angular#11580
    new webpack.ContextReplacementPlugin(
      // The (\\|\/) piece accounts for path separators in *nix and Windows
      /\@angular(\\|\/)core(\\|\/)esm5/,
      helpers.root('./src'), // location of your src
      {} // a map of your routes
    ),
    new ngToolsWebpack.AngularCompilerPlugin({
      tsConfigPath: './tsconfig.json',
      entryModule: './src/app/uasdm-app.module#UasdmAppModule',
      sourceMap: true
    })    
  ]
};