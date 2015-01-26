/*
 *
 * Copyright 2009-2015 Jayway Products AB
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

'use strict';

var gulp = require('gulp');
var args = require('yargs').argv;
var mainBowerFiles = require('main-bower-files');
var ngAnnotate = require('gulp-ng-annotate');
var jshint = require('gulp-jshint');
var stylish = require('jshint-stylish');
var connect = require('gulp-connect');
var concat = require('gulp-concat');
var uglify = require('gulp-uglify');
var del = require('del');
var runSequence = require('run-sequence');
var karma = require('gulp-karma');
var minifyCSS = require('gulp-minify-css');
var ngConstant = require('gulp-ng-constant');
var debug = require('gulp-debug');

var testFiles = ['unit/filters-unit.js'];
var buildMode = args.prod || args.dev || 'dev';

var paths = {
  scripts: [
    'app/**/*.js',
    '!app/design/**/*.js'
  ],
  css: [
    'app/design/gui/css/**/*.css',
    'bower_components/angular-growl-v2/build/angular-growl.css',
    'bower_components/pickadate/lib/themes/default.*',
    'bower_components/chosen/chosen.css'
  ],
  templates: [
    'app/**/*.html'
  ],
  images: [
    'app/design/gui/i/**/*',
    'bower_components/chosen/*.png'
  ],
  fonts: [
    'app/design/gui/fonts/**/*'
  ]
};

gulp.task('config', function () {
  gulp.src('app/config/config.json')
    .pipe(ngConstant({
      name: 'sf.config',
      constants: { buildMode: buildMode }
    }))
    .pipe(gulp.dest('app/config'));
});

gulp.task('unit-test', function () {
  return gulp.src(testFiles)
    .pipe(karma({
      configFile: 'karma.config.js',
      action: 'run'
    }))
    .on('error', function (err) {
      throw err;
    });
});

gulp.task('e2e-test', function () {
  return gulp.src(testFiles)
    .pipe(karma({
      configFile: 'karma-e2e.conf.js',
      action: 'run'
    }))
    .on('error', function (err) {
      throw err;
    });
});

gulp.task('build-scripts', function () {
  return gulp.src(paths.scripts)
    //.pipe(jshint())
    //.pipe(jshint.reporter(stylish))
    .pipe(ngAnnotate())
    //.pipe(uglify())
    .pipe(concat('streamflow.js'))
    .pipe(gulp.dest('build/app'))
    .pipe(connect.reload());
});

gulp.task('build-vendor-scripts', function () {
  return gulp.src(mainBowerFiles({filter: /\.js$/i}))
    .pipe(ngAnnotate())
    //.pipe(uglify())
    .pipe(concat('vendor.js'))
    .pipe(gulp.dest('build/app'))
    .pipe(connect.reload());
});

gulp.task('build-css', function () {
  return gulp.src(paths.css)
    .pipe(minifyCSS())
    .pipe(concat('streamflow.css'))
    .pipe(gulp.dest('build/app/css'))
    .pipe(connect.reload());
});

gulp.task('copy-templates', function () {
  return gulp.src(paths.templates)
    .pipe(gulp.dest('build'));
});

gulp.task('copy-fonts', function () {
  return gulp.src(paths.fonts)
    .pipe(gulp.dest('build/app/fonts'));
});

gulp.task('copy-images', function () {
  return gulp.src(paths.images)
    .pipe(gulp.dest('build/app/i'));
});

gulp.task('connect', function () {
  connect.server({
    root: 'build',
    port: 9999,
    livereload: true
  });
});

gulp.task('clean', function () {
  del(['build']);
});

gulp.task('build', [
  'config',
  'build-scripts',
  'build-vendor-scripts',
  'build-css',
  'copy-templates',
  'copy-fonts',
  'copy-images',
  'unit-test'
]);

gulp.task('watch', function () {
  gulp.watch(paths.scripts, ['build-scripts', 'build-vendor-scripts']);
  gulp.watch(paths.css, ['build-css']);
  gulp.watch(paths.templates, ['copy-templates']);
  gulp.watch(paths.images, ['copy-images']);
  gulp.watch(paths.fonts, ['copy-fonts']);
});

gulp.task('default', function () {
  runSequence('build', 'connect', 'watch');
});

