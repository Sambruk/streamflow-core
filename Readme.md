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

# Deployment
We're using maven to build the complete project.  
Start by updating the design submodule and then make sure all changes have been commited, pushed and that you have the latest version of the repository (pull).

Then in the streamflow-core/webclient folder type: `mvn clean install`.  
The build process should start and this will create a .war file in the target/ folder of /webclient, that can be deployed on a java webserver.  
The pom.xml describes what happens when we build using maven.  
In the pom.xml we reference to the build.sh/build.bat script that defines which webclient specific actions that are performed.

Glassfish:<br>
Redeploy the new .war file at <a href="test-sfwc.jayway.com:4848" /> under Applications. Account details could be found at: https://confluence.jayway.com/display/streamsource/Windows+server+tips+and+tricks

Remote Desktop:<br>
test-sfwc.jayway.com
Edit web.xml in the WEB-INF/ folder of your application.

Comment<br>
`<param-value>http://localhost/streamflow</param-value>`<br>
and uncomment <br>
`<param-value>http://test-sf.jayway.com/streamflow</param-value>`
(*depending on targetUri).

Glassfish:<br>
Reload deployed application

# Submodules

This project uses a submodule, it need to be initiated with.

    git submodule update --init

This populates the folder `webclient/app/design`.   
