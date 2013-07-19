# Developer Installation

Note: More current version in develop branch!

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

windows 7

    download and install node-v0.8.14-x64.msi
    add nodejs folder to environment variables


Make sure you don't install 0.9.x or 0.7 version !
npm is the node package manager.

### PhantomJS

PhantomJS is used for running tests in a headless browser, not required but very
nice.

Download the binaries from here: http://phantomjs.org/download.html

    http://phantomjs.org/download.html

Ubuntu:

    tar jxvf phantomjs-1.7.0-linux-i686.tar.bz2

Make sure you have the bin/phantomjs in your PATH environment

Windows 7
    download and install phantomjs-1.7.0-windows.zip
    add phantomjs folder to environment variables


### testacular

Used to runs tests

    sudo npm install -g testacular

Windows 7
    In order to run testacular "start --single-run" with the default script in the streamflow directory you will have to register the browser with the Windows Environment variables.
    Example:
      CHROME_BIN = C:\Program Files (x86)\Google\Chrome\Application\chrome.exe
      PHANTOMJS_BIN = C:\Program Files\Phantomjs\phantomjs-1.7.0-windows\phantomjs.exe

    (Remember to restart your cmd prompt to reload the variables from the system.)

###  Yeoman

Yeoman is used to build the site.
It can also be used for running a webserver and sync. a webbrowser, or
running tests.

   curl -L get.yeoman.io | bash

Yeoman needs:
* Ruby
* NodeJs
* Compass

It will check the environment that everthing is installed.
