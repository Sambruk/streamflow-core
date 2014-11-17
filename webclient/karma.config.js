/*
 *
 * Copyright 2009-2014 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Karma configuration
module.exports = function(config){
  config.set({

    basePath : '',

    files : [
      'bower_components/angular/angular.js',
      'bower_components/angular-resource/angular-resource.js',
      'bower_components/angular-growl-v2/build/angular-growl.js',
      'bower_components/angular-route/angular-route.js',
      'bower_components/angular-mocks/angular-mocks.js',
      'bower_components/angular-sanitize/angular-sanitize.js',
      'bower_components/underscore/underscore-min.js',
      'app/design/gui/js/jquery-1.10.1.js',
      'app/*.js',
      'app/components/**/*js',
      'app/components/*.js',
      'app/infrastructure/**/*.js',
      'app/routes/**/*.js',
      'test/fixture/backend.js',
      'test/unit/**/*.js'
    ],

    exclude: ['test/e2e/**/*.js'],

    frameworks: ['jasmine'],

    browsers : ['PhantomJS'],

    plugins : ['karma-*'],

    singelRun : true,

    junitReporter : {
      outputFile: 'test_out/unit.xml',
      suite: 'unit'
    },

//    Possible values: LOG_DISABLE, LOG_ERROR, LOG_WARN, LOG_INFO. LOG_DEBUG
    logLevel: config.LOG_ERROR,
    autoWatch : false,
    captureTimeout: 5000,
    port: 9000,
    runnerPort: 9100
  });
};

