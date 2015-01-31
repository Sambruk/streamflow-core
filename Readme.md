# Streamflow Webclient

## Getting Started

Navigate to `webclient`:

    git checkout develop
    npm install
    bower install
    gulp

The application is now served on `localhost:8080`.

## Build System

Streamflow is using the build tool [gulp](https://github.com/gulpjs/gulp/blob/master/docs/README.md).

There are different tasks for different purposes but the `gulp` command runs all
the neccessary ones.

Tasks that are not being run by default are:

    gulp unit-test
    gulp e2e-test

Gulp will by default build to the `webclient/build/` folder.

## Submodules

This project uses a submodule, it need to be initiated with.

    git submodule update --init

This populates the folder `webclient/app/design`.
