var gulp = require('gulp');
var plugins = require('gulp-load-plugins')({lazy:false});
var clean = require('gulp-clean');

gulp.task('html', function(){
    gulp.src(
        ['./app/**/*.html'])
    .pipe(gulp.dest('./build'));
});

gulp.task('clean', function(){
    gulp.src('./build/*.js', {read: false})
    .pipe(clean());
});

gulp.task('scripts', function(){
    //combine all js files of the app
    gulp.src(
        ['./bower_components/jquery/dist/*.js'])
        .pipe(gulp.dest('./build'));
    gulp.src(
        ['!./app/**/*_test.js',
        '!./bower_components/**/*.min.js',
        '!./bower_components/jquery/**/*.js',
        '!./app/design/**/*.js',
        './bower_components/angular/*.js',
        './bower_components/**/*.js',
        './app/app.js',
        './app/**/*.js'])
        .pipe(plugins.jshint())
        .pipe(plugins.jshint.reporter('default'))
        .pipe(plugins.concat('app.js'))
        //.pipe(plugins.rename({suffix: 'min'}))
        //.pipe(plugins.uglify())
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
    gulp.watch(['./app/**/*.js','!./app/**/*test.js'],['clean', 'scripts']);
    gulp.watch(['!./app/index.html','./app/**/*.html'], ['html']);
    gulp.watch('./app/**/*.css',['css']);
    gulp.watch('./app/index.html',['copy-index']);

});

gulp.task('connect', plugins.connect.server({
    root: ['build'],
    port: 9000,
    livereload: true
}));

gulp.task('default',['clean','connect','fonts', 'images','html','scripts','css','copy-index','watch']);