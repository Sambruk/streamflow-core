## Streamflow Client


## Learning AngularJs

* Check the tutorial at http://angularjs.org
* http://roytruelove.blogspot.se/2012/09/angularjs-dependency-injection-modules.html
* http://www.youtube.com/watch?v=GJey_oygU3Y
* http://vxtindia.com/blog/8-tips-for-angular-js-beginners/
* http://www.cheatography.com/proloser/cheat-sheets/angularjs/
* IRC help: http://webchat.freenode.net/?channels=angularjs&uio=d4

## Installation

see [install.md](readme.md)

## Run

   grunt --config Gruntfile.js server

The server tries to mimic the real server, see the node express file 'mock-server.js'
Only used for development and returns the static JSON api in the app/api folder with the same URLs.

## Build

   yeoman build

## Test

   yeoman test

Or run it from WebStrom IDEA, see docs/testacular.png and http://vojtajina.github.com/testacular/


## Angular Debug

Install the AngularJS Batarang chrome extension

## Debug and Logging in WebStorm/IDEA

* Install the the Jetbrains Chrome Extension
* Click on "Edit configuration..."
* Create a new JavaScript remote debug
* Start the debugging session
* Map the javascript file you have breakpoints to, e.g http://localhost:3501/modules/main/controllers.main.js

You now get all the console.log output in the WebStorm IDE and can set break points


## API

Function Streamflow:

My Cases:
http://localhost:8082/streamflow/workspace/projects/b35873ba-4007-40ac-9936-975eab38395a-3f/inbox/cases?tq=select+*
Inbox:
http://localhost:8082/streamflow/workspace/projects/b35873ba-4007-40ac-9936-975eab38395a-3f/assignments/cases?tq=select+*
