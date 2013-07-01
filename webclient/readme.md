## Streamflow Client

## Using the real Streamflow backend

If you are using the real streamflow backend (see config below) you need to
disable the web security since we are accessing the backend at a different
port:

Mac OSX:
  alias chrome='/Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome'
  alias chromex="chrome --disable-web-security"


## Learning AngularJs

* Check the tutorial at http://angularjs.org
* http://roytruelove.blogspot.se/2012/09/angularjs-dependency-injection-modules.html
* http://www.youtube.com/watch?v=GJey_oygU3Y
* http://vxtindia.com/blog/8-tips-for-angular-js-beginners/
* http://www.cheatography.com/proloser/cheat-sheets/angularjs/
* IRC help: http://webchat.freenode.net/?channels=angularjs&uio=d4

## Installation

see [install.md](install.md)

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

   yeoman test

Or run it from WebStrom IDEA, see docs/testacular.png and http://vojtajina.github.com/testacular/

### End to End Tests

Start the web-server (./web-server.js)
Navigate to http://localhost:8000/test/e2e/runner.html

The test files are located here: `test/e2e/scenarios.js'
It uses the mock data located in app/api.

### Manual Testing

You can use the real server, see the test_server folder for scripts etc.

## Angular Debug

Install the AngularJS Batarang chrome extension

## Debug and Logging in WebStorm/IDEA

* Install the the Jetbrains Chrome Extension
* Click on "Edit configuration..."
* Create a new JavaScript remote debug
* Start the debugging session
* Map the javascript file you have breakpoints to, e.g http://localhost:3501/modules/main/controllers.main.js

You now get all the console.log output in the WebStorm IDE and can set break points
