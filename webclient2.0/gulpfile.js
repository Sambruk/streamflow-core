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

gulp.task('build', ['copy', 'images', 'datepicker', 'inject', 'scripts', 'css','fonts'], function(cb) {
  cb();
});

gulp.task('clean-build', function(cb) {
  runSequence('clean', 'build', function() {
    cb();
  });
});

var mainBowerFiles = mainBowerFiles();

var paths = {
  scripts: ['app/app.js',
            'app/*.js',
            'app/infrastructure/**/*.js',
            'app/components/**/*.js',
            'app/routes/**/*.js',
            '!app/**/*test.js'],
  bower: ['bower_components/**/*.js',
          '!bower_components/jquery/**/*.js',
          '!bower_components/angular/**/*.js',
          '!bower_components/angular-route/**/*.js',
          '!bower_components/angular-growl-v2/**/*.js',
          '!bower_components/momentjs/**/*.js',
          '!bower_components/bootstrap/**/*.*',
          'bower_components/bootstrap/dist/js/bootstrap.js',
          '!bower_components/pickadate/**/*.*',
          'bower_components/pickadate/lib/picker.js',
          'bower_components/pickadate/lib/picker.date.js',
          '!bower_components/**/*.min.js',
          '!bower_components/**/*.min.map'
          ],
  templates: ['app/**/*.html',
              '!app/index.html',
              '!app/token.html',
              '!bower_components/**/*.html'],
  vendortTemplates: {
    datepicker: {
      origin: 'bower_components/bootstrap/template/datepicker/*.html',
      dest: 'datepicker'
    }
  },
  icons: 'app/icons/*.svg',
  images: 'app/design/gui/i/*.png',
  css: ['app/design/gui/css/**/*.css'],
  other: ['app/design/gui/fonts/*',
          'bower_components/bootstrap/dist/fonts/*.*',
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
    .pipe(gulp.dest('./build/template'));
});


gulp.task('datepicker', function(){
  gulp.src(['./app/components/bootstrap/datepicker/*.html'])
  .pipe(gulp.dest('./build/template/datepicker'));
});

gulp.task('scripts', function() {
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
    'bower/jquery-ui.js',
    'bower/lodash.js',
    'bower/picker.js',
    'bower/picker.date.js',
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
    gulp.src([paths.images])
    .pipe(gulp.dest('./build/i'));
});

gulp.task('css', function(){
    gulp.src('./app/design/gui/css/**/*.css')
      .pipe(concat('app.css'))
      .pipe(gulp.dest('./build/css'));
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
  gulp.watch([paths.images], ['images']);
  gulp.watch(paths.scripts, ['scripts']);
  gulp.watch(paths.css, ['css']);
  gulp.watch(paths.templates, ['scripts', 'inject']);
});