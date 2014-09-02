var gulp = require('gulp');
var plugins = require("gulp-load-plugins")({lazy:false});

gulp.task('html', function(){
    gulp.src(
        ['./app/**/*.html'])
    .pipe(gulp.dest('./build'));
});

gulp.task('scripts', function(){
    //combine all js files of the app
    gulp.src(
        ['!./app/**/*_test.js',
        '!./bower_components/**/*.min.js',
        './bower_components/**/*.js',
        './app/app.js',
        './app/**/*.js'])
        .pipe(plugins.jshint())
        .pipe(plugins.jshint.reporter('default'))
        .pipe(plugins.concat('app.js'))
        .pipe(gulp.dest('./build'));
});

gulp.task('css', function(){
    gulp.src('./app/**/*.css')
        .pipe(plugins.concat('app.css'))
        .pipe(gulp.dest('./build'));
});

gulp.task('copy-index', function() {
    gulp.src('./app/index.html')    
        .pipe(gulp.dest('./build'));
});

gulp.task('watch',function(){
    gulp.watch([
        'build/**/*.html',        
        'build/**/*.js',
        'build/**/*.css'        
    ], function(event) {
        return gulp.src(event.path)
            .pipe(plugins.connect.reload());
    });
    gulp.watch(['./app/**/*.js','!./app/**/*test.js'],['scripts']);
    gulp.watch(['!./app/index.html','./app/**/*.html']);
    gulp.watch('./app/**/*.css',['css']);
    gulp.watch('./app/index.html',['copy-index']);

});

gulp.task('connect', plugins.connect.server({
    root: ['build'],
    port: 9000,
    livereload: true
}));

gulp.task('default',['connect','html','scripts','css','copy-index','watch']);