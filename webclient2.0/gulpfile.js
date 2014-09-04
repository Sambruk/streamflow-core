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
    gulpif = require('gulp-if');


gulp.task('connect', ['clean-build'], function() {
  connect.server({
    root: 'build',
    livereload: true
  });
});


gulp.task('clean', function(cb) {
  del(['build'], cb);
});

gulp.task('build', ['copy', 'inject', 'scripts', 'css','fonts'], function(cb) {
  cb();
});

gulp.task('clean-build', function(cb) {
  runSequence('clean', 'build', function() {
    cb();
  });
});

var paths = {
  scripts: ['app/app.js',
            'app/*.js',
            'app/infrastructure/**/*.js',
            'app/components/**/*.js',
            'app/routes/**/*.js',
            '!app/**/*test.js'],
  bower: ['!bower_components/jquery/**/*.js',
          '!bower_components/angular/**/*.js',
          '!bower_components/angular-route/**/*.js',
          '!bower_components/angular-growl-v2/**/*.js',
          '!bower_components/momentjs/**/*.js',
          '!bower_components/**/*.min.js',
          '!bower_components/**/*.min.map',
          'bower_components/**/*js'],
  templates: ['app/**/*.html',
              '!app/index.html',
              '!app/token.html',
              '!app/bower_components/**/*.html'],
  icons: 'app/icons/*.svg',
  css: ['!bower_components/angular-growl-v2/angular-growl.css',
        '!bower_components/angular-growl-v2/angular-growl.min.css',
        '!**/*.min.css',
        'app/**/*.css',
        'bower_components/**/*.css'],
  other: ['app/design/gui/fonts/*',
          'app/design/**/*.png',
          'app/*.html',
          'app/*.json',
          'app/robots.txt',
          'app/favicon.ico']
};

gulp.task('css',function() {
  return gulp.src(paths.css)
    .pipe(concat('app.css'))
    .pipe(gulp.dest('build/css'))
    .pipe(connect.reload());
});

gulp.task('html', function(){
    gulp.src(
        ['!./app/index.html','!app/design/**/*.html','./app/**/*.html'])
    .pipe(gulp.dest('./build'));
});

gulp.task('scripts', function() {
  return es.concat(
    (
      gulp.src(mainBowerFiles())
      .pipe(ngAnnotate())
      // Need to add a directory here so we can do proper sorting before concatenation below
      .pipe(rename(function (path) {
        path.dirname = 'bower/';

      }))
    ),
    (

      gulp.src(paths.scripts)
      .pipe(jshint())
      .pipe(jshint.reporter(stylish))
      .pipe(ngAnnotate())
    ),
    (
      gulp.src(paths.bower)
      .pipe(ngAnnotate())
      .pipe(rename(function(path){
        path.dirname = 'bower/';
      }))
    ),
    (
      gulp.src(paths.templates)
      .pipe(templateCache({module:'sf'}))
    )
  )
  // Specify concatenation order
  .pipe(order([
    'bower/jquery.js',
    'bower/lodash.js',
    'bower/angular.js',
 //   'bower/moment.js',
    'bower/**/*.js',
    'app.js',
    '**/*.js'
  ]))
  .pipe(gulpif(buildMode === 'dev', sourcemaps.init()))
  .pipe(concat('streamflow.js'))
  .pipe(gulpif(buildMode === 'dev', sourcemaps.write()))
  .pipe(gulp.dest('build/scripts'))
  .pipe(connect.reload());
});

gulp.task('fonts', function(){
    gulp.src(['./app/design/gui/fonts/*'])
    .pipe(gulp.dest('./build/fonts'));
});

gulp.task('images', function(){
    gulp.src(['./app/**/*.png'])
    .pipe(gulp.dest('./build/images'));
});

gulp.task('css', function(){
    gulp.src('./app/**/*.css')
        .pipe(plugins.concat('app.css'))
        .pipe(gulp.dest('./build'));
});

gulp.task('copy', function() {
  return gulp.src(paths.other, {base: 'app'})
    .pipe(gulp.dest('build'));
});

gulp.task('inject', ['copy'], function() {
  return gulp.src('build/*.html')
    .pipe(gulp.dest('build'));
});

gulp.task('test', function(){
  return gulp.src(testFiles)
    .pipe(karma({
      configFile: 'karma.conf.js',
      action: 'run'
    }))
    .on('error', function(err){
      throw err;
    });
});


gulp.task('default', ['connect'], function () {
  gulp.watch([paths.other], ['copy']);
  gulp.watch([paths.other], ['inject']);
  gulp.watch(paths.scripts, ['scripts']);
  gulp.watch(paths.css, ['styles']);
  gulp.watch(paths.templates, ['scripts', 'inject']);
});