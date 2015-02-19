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

## Deployment

We're using maven to build the complete project.
Start by updating the design submodule and then make sure all changes have been commited, pushed and that you have the latest version of the repository (pull).

Then in the `streamflow-core/webclient` folder type:

```bash
mvn clean install
```

The build process should start and this will create a .war file in the `target/` folder of `/webclient`, that can be deployed on a java webserver.
The `pom.xml` describes what happens when we build using maven.
In the `pom.xml` we reference to the `build.sh/build.bat` script that defines which webclient specific actions that are performed.

### Glassfish

Redeploy the new `.war` file at <a href="test-sfwc.jayway.com:4848" />under Applications. Account details could be found at: https://confluence.jayway.com/display/streamsource/Windows+server+tips+and+tricks</a>

Remote desktop to:

```
test-sfwc.jayway.com
```

Edit `WEB-INF/web.xml`. Comment out:

```
<param-value>http://localhost/streamflow</param-value>`
```

and uncomment:

```
<param-value>http://test-sf.jayway.com/streamflow</param-value>`
```

Go back to Glassfish and **Reload deployed application**.

