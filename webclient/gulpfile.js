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
'use strict';
var buildMode = 'dev';
var gulp = require('gulp'),
    args   = require('yargs').argv,
    order = require('gulp-order'),
    es = require('event-stream'),
    inject = require('gulp-inject'),
    templateCache = require('gulp-angular-templatecache'),
    mainBowerFiles = require('main-bower-files'),
    ngAnnotate = require('gulp-ng-annotate'),
    rename = require('gulp-rename'),
    jshint = require('gulp-jshint'),
    stylish = require('jshint-stylish'),
    connect = require('gulp-connect'),
    browserSync = require('browser-sync'),
    concat = require('gulp-concat'),
    uglify = require('gulp-uglify'),
    sourcemaps = require('gulp-sourcemaps'),
    del = require('del'),
    runSequence = require('run-sequence'),
    karma = require('gulp-karma'),
    gulpif = require('gulp-if'),
    minifyCSS = require('gulp-minify-css'),
    testFiles = ['unit/filters-unit.js'],
    ngConstant = require('gulp-ng-constant');


gulp.task('connect', ['clean-build'], function() {
  connect.server({
    root: 'build',
    port: 9999,
    livereload: true
  });
});


gulp.task('clean', function(cb) {
  del(['build'], cb);
});

//gulp.task('build', ['datepicker-scripts', 'datepicker-styles', 'copy', 'images', 'inject', 'scripts', 'css','fonts'], function(cb) {
//  cb();
//});


gulp.task('build', ['copy', 'app-plugins', 'app-fonts', 'app-css', 'config','app-scripts', 'app-css', 'app-images', 'vendor-images','vendor-scripts', 'vendor-css', 'app-templates', 'unit-test'], function(cb){
  cb();
});

gulp.task('clean-build', function(cb) {
  runSequence('clean', 'build', function() {
    cb();
  });
});
var buildMode,
    endpoint;

if(args.prod){
  buildMode = 'prod';
} else if(args.dev){
  buildMode = 'dev';
} else {
  buildMode = 'dev';
}

console.log('ENDPOINT');
console.log(endpoint);

console.log('BUILD MODE');
console.log(buildMode);

gulp.task('config', function () {
  gulp.src('app/config/config.json')
    .pipe(ngConstant({
      name: 'sf.config',
      constants: { buildMode: buildMode },
      //wrap: 'commonjs'
      //wrap: 'amd',
    }))
    // Writes config.js to dist/ folder
    .pipe(gulp.dest('app/config'));
});

var mainBowerFiles = mainBowerFiles();

var paths = {
  scripts: ['app/**/*.js',
            '!app/design/**/*.*',     
            '!app/**/*test.js',
            '!app/routes/cases/forms/dynamicformdirective/**/*.*'],
  templates: ['app/**/*.html',
              '!app/index.html',
              '!app/token.html',
              '!bower_components/**/*.html'],
  icons: 'app/icons/*.svg',
  images: 'app/design/gui/i/*.*',
  css: ['app/design/gui/css/basic.css',
        'app/design/gui/css/print.css',
        'app/design/gui/css/fonts.css',
        'app/design/gui/css/retina.css',
        'app/design/gui/vendor/pickadate/themes/default.css',
        'app/design/gui/vendor/pickadate/themes/default.date.css'],
  design: {
    images: ['app/design/gui/i/*.*'],
    css: ['app/design/gui/css/basic.css',     
        'app/design/gui/css/print.css',
        'app/design/gui/css/fonts.css',
        'app/design/gui/css/main.css',
        'app/design/gui/css/retina.css'],
    scripts: ['app/design/gui/js/**/*.js',
              '!app/design/gui/js/jquery-1.10.1.js',
              'app/design/gui/vendor/pickadate/picker.js',
              'app/design/gui/vendor/pickadate/picker.date.js',
              'app/design/gui/vendor/pickadate/translations/sv_SE.js'],
    fonts: 'app/design/gui/fonts/*'
  },
  vendor: {
    images: ['bower_components/chosen/*.png'],
    scripts: ['bower_components/**/*.js',
          '!bower_components/underscore/index.js',
          '!bower_components/underscore/docs/**/*.js',
          '!bower_components/underscore/test/**/*.js',
          '!bower_components/jquery/**/*.js',
          '!bower_components/angular/**/*.js',
          '!bower_components/angular-route/**/*.js',
          '!bower_components/angular-growl-v2/**/*.js',
          '!bower_components/momentjs/**/*.js',
          '!bower_components/bootstrap/**/*.*',
          '!bower_components/pickadate/**/*.*',
          '!bower_components/**/*.min.js',
          '!bower_components/**/*.min.map',
          'bower_components/underscore/underscore.js'],
    css: ['app/design/gui/vendor/pickadate/themes/default.css',
          'app/design/gui/vendor/pickadate/themes/default.date.css',
          'bower_components/chosen/chosen.css']
  },
  other: [,
          //'app/*.html',
          'app/*.json',
          'app/robots.txt',
          'app/favicon.ico']
};

gulp.task('app-scripts', function(){
  return gulp.src(paths.scripts)
  .pipe(ngAnnotate())
  .pipe(rename(function(path){
    path.dirname = 'app/';
  }))
  .pipe(order([
  'app/app.js',
  'app/infrastructure/**/*.js',
  'app/components/**/*.js',
  'app/routes/**/*.js']))
  .pipe(gulpif(buildMode === 'prod', uglify()))
  .pipe(gulpif(buildMode === 'dev', sourcemaps.init()))
  .pipe(concat('streamflow.js'))
  .pipe(gulpif(buildMode === 'dev', sourcemaps.write()))
  .pipe(gulp.dest('build/app'))
  .pipe(connect.reload());
});

gulp.task('app-templates', function(){
  return gulp.src('app/**/*.html')
  .pipe(gulp.dest('build'))
});

gulp.task('app-css', function(){
  return gulp.src(paths.design.css)
  .pipe(gulpif(buildMode === 'prod', minifyCSS({keepBreaks:true})))
  .pipe(concat('streamflow.css'))
  .pipe(gulp.dest('build/app/css'))
  .pipe(connect.reload());
});

gulp.task('app-fonts', function(){
  return gulp.src(paths.design.fonts)
  .pipe(gulp.dest('build/app/fonts'))
});

gulp.task('app-images', function(){
  return gulp.src(paths.design.images)
  .pipe(gulp.dest('build/app/i'))
});

gulp.task('app-plugins', function(){
  return gulp.src(paths.design.scripts)
  .pipe(gulp.dest('build/app/plugins'))
});

gulp.task('vendor-images', function(){
  return gulp.src(paths.vendor.images)
  .pipe(gulp.dest('build/vendor'))
});

gulp.task('vendor-css', function(){
  return gulp.src(paths.vendor.css)
  .pipe(gulpif(buildMode === 'prod', minifyCSS({keepBreaks:true})))
  .pipe(concat('vendor.css'))
  .pipe(gulp.dest('build/vendor'))
});


gulp.task('vendor-scripts', function() {
  //Check that no css files are mistakenly added to mainBowerFiles
  mainBowerFiles = mainBowerFiles.filter(function(file){
    return file.substring(file.length - 2) === 'js';
  });
  console.log(mainBowerFiles);
  return es.concat(
    (
      gulp.src(mainBowerFiles)
      .pipe(ngAnnotate())
      // Need to add a directory here so we can do proper sorting before concatenation below
      .pipe(rename(function (path) {
        path.dirname = 'bower/';

      }))
    ),
    (
      gulp.src(paths.vendor.scripts)
      .pipe(ngAnnotate())
    )
  )
  // Specify concatenation order
  .pipe(order([
    'bower/picker.js',
    'bower/picker.date.js',
    'bower/sv_SE.js',
    'bower/underscore.js',
    'bower/angular-file-upload-shim.js',
    'bower/angular.js',
    'bower/angular-file-upload.js',
    'bower/moment.js',
    'bower/**/*.js'
  ]))
  .pipe(gulpif(buildMode === 'prod', uglify()))
  .pipe(gulpif(buildMode === 'dev', sourcemaps.init()))
  .pipe(concat('vendor.js'))
  .pipe(gulpif(buildMode === 'dev', sourcemaps.write()))
  .pipe(gulp.dest('build/vendor'))
  .pipe(connect.reload());
});

gulp.task('copy', function() {
  return gulp.src(paths.other, {base: 'app'})
    .pipe(gulp.dest('build'));
});

gulp.task('inject', ['copy'], function() {
  return gulp.src('build/*.html')
    .pipe(gulp.dest('build'));
});

gulp.task('unit-test', function(){
  return gulp.src(testFiles)
    .pipe(karma({
      configFile: 'karma.config.js',
      action: 'run'
    }))
    .on('error', function(err){
      throw err;
    });
});

gulp.task('e2e-test', function(){
  return gulp.src(testFiles)
    .pipe(karma({
      configFile: 'karma-e2e.conf.js',
      action: 'run'
    }))
    .on('error', function(err){
      throw err;
    });
});


gulp.task('default', ['connect'], function () {
  gulp.watch(paths.scripts, ['build']);
  gulp.watch(paths.design.css, ['build']);
  gulp.watch(['app/**/*.html'], ['build']);
});