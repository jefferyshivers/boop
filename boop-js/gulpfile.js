
const gulp = require('gulp')
const series = gulp.series
const spawn = require('child_process').spawn

const build = (cb) => {
  const process = spawn('yarn', ['build'], { stdio: 'inherit' })
  process.on('exit', code => {
    if (code !== 0) {
      return cb(new Error('failed to build'))
    } else {
      return cb()
    }
  })
}

const test = (cb) => {
  const process = spawn('yarn', ['test'], { stdio: 'inherit' })
  process.on('exit', code => {
    if (code !== 0) {
      return cb(new Error('failed to test'))
    } else {
      return cb()
    }
  })
}

const watch = () => {
  gulp.watch(['./src/**/*.ts'], series(build, test))
}

module.exports = {
  build,
  test,
  watch
}
