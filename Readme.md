# Up and running
navigate to `webclient`

    npm install  

    bower install  
You need to remove jquery from bower_components since wrong version gets installed as a dependency  

navigate to `webclient/app/design`  
    git checkout develop  

navigate to `webclient`  
    gulp  
 
The application is now served on localhost:8080  

# Gulp
We're using gulp as build tool now.  
https://github.com/gulpjs/gulp/blob/master/docs/README.md  
  
To start the client when all dependencies are installed via npm and bower just type:  
    gulp  

There are different tasks for different purposes but the gulp command runs all neccessary ones.  

Tasks that are not being run by default (gulp command) is:  
    gulp unit-test  
    gulp e2e-test  
To run them simply type the commands in the webclient root folder.  

Gulp will by default build to the `webclient/build/` folder.
This will also be done by using the `.build.sh` script.  


When building with gulp all vendor files will be concatenated into a single vendor.js file under `build/vendor/`  
and all app specific files will be concatenated into a streamflow.js file under `build/app/`  

# Submodules

This project uses a submodule, it need to be initiated with.

    git submodule update --init

This populates the folder `webclient/app/design`.   


# Building the webclient with Maven

Make sure you do git pull in ./webclient/app/design to get the latest files from
the submodule before you run mvn clean install in the webclient folder.