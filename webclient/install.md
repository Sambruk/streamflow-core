# Developer Installation

## Quick way to get the application up and running

Clone the repo and checkout the develop branch
    
    git checkout develop

Go to the webclient folder

    cd webclient
    
Install the node dependencies

    npm install

If you just want to run the site, you can run `bower install` from you local `node_modules`, like this:

    node_modules/bower/bin/bower install

Otherwise, you can install yeoman (see below) and use the globally installed `bower`

Open a chrome/chromium browser with the flag --disable-web-security

    chrome --disable-web-security   (or whatever you open your chrome/chromium)
    
Visit the API, to get an authentication token. Surf to the following url and enter the credentials

    https://test.sf.streamsource.se/streamflow/

Start the local web server

    ./web-server.js
    
Open the application! :)

    http://localhost:8000/app/
    
If you don't get a populated case list, the console log is your friend.
    

## Full guide, including how to run the tests

### Node.js and npm

Install node js version 0.8.x

On ubuntu (12.04):

Notice, sudo apt-get install installs the wrong version.

    sudo apt-get update && sudo apt-get install curl build-essential openssl libssl-dev git python g++
    export node_version_to_install='v0.8.9'
    curl https://raw.github.com/bevry/community/master/install-node/install-node.sh | sh

    sudo apt-get install npm

See https://github.com/bevry/community/wiki/Installing-Node

Mac

    brew install node
    curl http://npmjs.org/install.sh | sh

Make sure you don't install 0.9.x or 0.7 version !
npm is the node package manager.

### PhantomJS

PhantomJS is used for running tests in a headless browser, not required but very
nice.

Download the binaries from here: http://phantomjs.org/download.html

    http://phantomjs.org/download.html

Make sure you have the bin/phantomjs in your PATH environment


### karma (TODO, verify this!)

Used to runs tests

    (sudo npm install -g testacular) # Outdated! Do not use!

###  Yeoman

Yeoman is used to build the site.
It can also be used for running a webserver and sync. a webbrowser, or
running tests.

   npm install -g yo grunt-cli bower

Yeoman needs:
* Ruby
* NodeJs
* Compass

It will check the environment that everything is installed.
