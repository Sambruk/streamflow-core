'use strict';
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
    browserSync = require('browser-sync'),
    concat = require('gulp-concat'),
    uglify = require('gulp-uglify'),
    sourcemaps = require('gulp-sourcemaps'),
    del = require('del'),
    runSequence = require('run-sequence'),
    karma = require('gulp-karma'),
    gulpif = require('gulp-if');

// Building for development
var buildMode = args.build || 'dev';

var paths = {
  scripts: ['app/app.js',
            'app/infrastructure/**/*.js',
            'app/components/**/*.js',
            'app/routes/**/*.js',
            '!app/**/*test.js'],
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

gulp.task('clean', function(cb) {
  del(['build'], cb);
});

gulp.task('build', ['copy', 'inject', 'scripts', 'styles','fonts'], function(cb) {
  cb();
});

gulp.task('clean-build', function(cb) {
  runSequence('clean', 'build', function() {
    cb();
  });
});

gulp.task('browser-sync', ['clean-build'], function() {
  // The server
  browserSync({
    port: 9000,
    server: {
      baseDir: 'build'
    }
  });
});

gulp.task('styles',function() {
  // Compiles Sass and prefixes the output CSS
  return gulp.src(paths.css)
    .pipe(concat('app.css'))
    .pipe(gulp.dest('build/css'))
    .pipe(browserSync.reload({stream:true}));
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
      gulp.src(paths.templates)
      .pipe(templateCache({module:'sf'}))
    )
  )
  // Specify concatenation order so we never end up with unmet dependencies
  .pipe(order([
    'bower/jquery.js',
    'bower/angular.js',
    'bower/**/*.js',
    'app.js',
    '**/*.js'
  ]))
  .pipe(gulpif(buildMode === 'dev', sourcemaps.init()))
  .pipe(concat('streamflow.js'))
  .pipe(gulpif(buildMode === 'dev', sourcemaps.write()))
  .pipe(gulp.dest('build/scripts'))
  .pipe(browserSync.reload({stream:true}));
});

gulp.task('html', function(){
    gulp.src(
        ['!./app/index.html','!app/design/**/*.html','./app/**/*.html'])
    .pipe(gulp.dest('./build'));
});


gulp.task('fonts', function(){
    gulp.src(['./app/design/gui/fonts/*'])
    .pipe(gulp.dest('./build/fonts'));
});

gulp.task('images', function(){
    gulp.src(['./app/**/*.png'])
    .pipe(gulp.dest('./build/img'));
});

gulp.task('css', function(){
    gulp.src('./app/**/*.css')
        .pipe(plugins.concat('app.css'))
        .pipe(gulp.dest('./build'));
});

gulp.task('copy', function() {
  // Copy assets to build folder
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


gulp.task('default', ['browser-sync'], function () {
  gulp.watch([paths.other], ['copy']);
  gulp.watch([paths.other], ['inject']);
  gulp.watch(paths.scripts, ['scripts']);
  gulp.watch(paths.sass, ['styles']);
  gulp.watch(paths.templates, ['scripts', 'inject']);
});

//gulp.task('default',['connect','fonts', 'images', 'dependencies','copy-index', 'scripts-index','html','scripts','css','watch']);