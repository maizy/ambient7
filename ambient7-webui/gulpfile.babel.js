// based on https://github.com/Granze/react-starterify

import gulp from 'gulp';
import autoprefixer from 'less-plugin-autoprefix';
import browserify from 'browserify';
import watchify from 'watchify';
import source from 'vinyl-source-stream';
import buffer from 'vinyl-buffer';
import eslint from 'gulp-eslint';
import babelify from 'babelify';
import uglify from 'gulp-uglify';
import rimraf from 'rimraf';
import notify from 'gulp-notify';
import browserSync, { reload } from 'browser-sync';
import sourcemaps from 'gulp-sourcemaps';
import rename from 'gulp-rename';
import htmlReplace from 'gulp-html-replace';
import runSequence from 'run-sequence';
import ghPages from 'gulp-gh-pages';
import less from 'gulp-less';

const paths = {
  bundle: 'app.js',
  entry: 'src/Index.js',
  srcCss: 'src/**/*.less',
  srcLint: ['src/**/*.js', 'test/**/*.js'],
  dist: 'dist',
  distJs: 'dist/js'
};

const customOpts = {
  entries: [paths.entry],
  debug: true,
  cache: {},
  packageCache: {}
};


const opts = Object.assign({}, watchify.args, customOpts);

gulp.task('clean', cb => {
  rimraf('dist', cb);
});

gulp.task('browserSync', () => {
  browserSync({
    server: {
      baseDir: './'
    }
  });
});

gulp.task('watchify', () => {
  const bundler = watchify(browserify(opts));

  function rebundle() {
    return bundler.bundle()
      .on('error', notify.onError())
      .pipe(source(paths.bundle))
      .pipe(buffer())
      .pipe(sourcemaps.init({ loadMaps: true }))
      .pipe(sourcemaps.write('.'))
      .pipe(gulp.dest(paths.distJs))
      .pipe(reload({ stream: true }));
  }

  bundler.transform(babelify)
  .on('update', rebundle);
  return rebundle();
});

gulp.task('browserify', () => {
  browserify(paths.entry, { debug: true })
  .transform(babelify)
  .bundle()
  .pipe(source(paths.bundle))
  .pipe(buffer())
  .pipe(sourcemaps.init({ loadMaps: true }))
  .pipe(uglify())
  .pipe(sourcemaps.write('.'))
  .pipe(gulp.dest(paths.distJs));
});

const autoprefix = new autoprefixer({ browsers: ['last 2 versions'] });

gulp.task('styles', () => {
  gulp.src(paths.srcCss)
  .pipe(rename({ extname: '.css' }))
  .pipe(sourcemaps.init())
  .pipe(less({
    plugins: [autoprefix]
  }))
  .pipe(sourcemaps.write('.'))
  .pipe(gulp.dest(paths.dist))
  .pipe(reload({ stream: true }));
});

gulp.task('htmlReplace', () => {
  gulp.src('index.html')
  .pipe(htmlReplace({ css: '/styles/main.css', js: '/js/app.js' }))
  .pipe(gulp.dest(paths.dist));
});

gulp.task('lint', () => {
  gulp.src(paths.srcLint)
  .pipe(eslint())
  .pipe(eslint.format());
});

gulp.task('watchTask', () => {
  gulp.watch(paths.srcCss, ['styles']);
  gulp.watch(paths.srcLint, ['lint']);
});

gulp.task('watch', cb => {
  runSequence('clean', ['browserSync', 'watchTask', 'watchify', 'styles', 'lint'], cb);
});

gulp.task('build', cb => {
  process.env.NODE_ENV = 'production';
  runSequence('clean', ['browserify', 'styles', 'htmlReplace'], cb);
});

gulp.task('default', ['watch']);
