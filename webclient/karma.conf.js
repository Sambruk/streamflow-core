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

    files : [
      'app/components/angular/angular.js',
      'app/components/angular-route/angular-route.js',
      'app/components/angular-mocks/angular-mocks.js',
      'app/components/underscore/underscore-min.js',
      'app/components/jquery/jquery.min.js',
      'app/modules/*.js',
      'app/modules/**/*.js',
      'test/**/*.js'
    ],

    exclude: ['test/e2e/**/*.js'],

    frameworks: ['jasmine'],

    browsers : ['PhantomJS'],

    plugins : ['karma-*'],

    junitReporter : {
      outputFile: 'test_out/unit.xml',
      suite: 'unit'
    },

    logLevel: config.LOG_DEBUG,
    autoWatch : false,
    captureTimeout: 5000,
    port: 8080,
    runnerPort: 9100
  });
};

