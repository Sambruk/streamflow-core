## Streamflow Client

# Developer Installation

## Quick way to get the application up and running

Clone the repo and then initialize the submodule(s)

    git submodule update --init

Checkout the develop branch
    
    git checkout develop

Go to the webclient folder

    cd webclient
    
Install the node dependencies (you need to have node.js installed)

    npm install

If you just want to run the site, you can run `bower install` from you local `node_modules`, like this:

    node_modules/bower/bin/bower install

Otherwise, you can install yeoman (see below) and use the globally installed `bower`

Open a chrome/chromium browser with the flag --disable-web-security

    chrome --disable-web-security
    
Visit the API, to get an authentication token. Surf to the following url and enter the credentials

    https://test.sf.streamsource.se/streamflow/

Start the local web server

    ./web-server.js
    
Open the application! :)

    http://localhost:8000/app/
    
If you don't get a populated case list, the console log is your friend.
    
The design will probably look very strange. Pull the latest changes from the submodule to fix that

    cd app/design
    git pull origin master
    

## Installing yeoman

From https://github.com/yeoman/yeoman/wiki/Getting-Started

    npm install -g yo grunt-cli bower

If you get lots of errors, including

    "Please try running this command again as root/Administrator."

..then you should NOT run the command with sudo. Instead, you should chown your usr/local. How this is done (and why) is described here: http://howtonode.org/introduction-to-npm


## Using the real Streamflow backend

If you are using the real streamflow backend (see config below) you need to
disable the web security since we are accessing the backend at a different
port:

Mac OSX:
  alias chrome='/Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome'
  alias chromex="chrome --disable-web-security"
  
Windows:
    chrome.exe --user-data-dir="C:/Chrome dev session" --disable-web-security
http://superuser.com/questions/593726/is-it-possible-to-run-chrome-with-and-without-web-security-at-the-same-time


## Learning AngularJs

* Check the tutorial at http://angularjs.org
* http://roytruelove.blogspot.se/2012/09/angularjs-dependency-injection-modules.html
* http://www.youtube.com/watch?v=GJey_oygU3Y
* http://vxtindia.com/blog/8-tips-for-angular-js-beginners/
* http://www.cheatography.com/proloser/cheat-sheets/angularjs/
* IRC help: http://webchat.freenode.net/?channels=angularjs&uio=d4

## Get Bower Dependencies

To get the dependencies like jQuery and Angular run the bower install command. 
This will create and populate the components folder.

    bower install


## Run

   ./web-server.js
   open browser localhost:8000/app/index.html

The server tries to mimic the real server.
Only used for development and returns the static JSON api in the app/api folder with the same URLs.
Notice it's possible to simulate HTTP error codes etc with the web-server.js (check source code).

## Build

   yeoman build

## Test

### Unit Tests

Install phantom.js

    npm install -g phantomjs

Install and start karma

    npm install -g karma
    karma start
   
Now open a new terminal and run

    karma run


Or run it from WebStrom IDEA, see docs/testacular.png and http://vojtajina.github.com/testacular/

### End to End Tests

Start the web-server (./web-server.js)
Navigate to http://localhost:8000/test/e2e/runner.html

The test files are located here: `test/e2e/scenarios.js'
The default configuration is to run against the live server. That makes it a bit fragile. Instead, you can use the mock data located in app/api.

### Manual Testing

You can use the real server, see the test_server folder for scripts etc.

## Angular Debug

Install the AngularJS Batarang chrome extension

## Debug and Logging in WebStorm/IDEA

* Install the the Jetbrains Chrome Extension
* Click on "Edit configuration..."
* Create a new JavaScript remote debug
* Start the debugging session
* Map the javascript file you have breakpoints to, e.g http://localhost:3501/modules/main/controllers.X.js

You now get all the console.log output in the WebStorm IDE and can set break points
