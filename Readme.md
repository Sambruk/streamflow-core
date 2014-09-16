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

# Submodules

This project uses a submodule, it need to be initiated with.

    git submodule update --init

This populates the folder `webclient/app/design`.   
