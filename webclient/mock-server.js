/*
 *
 * Copyright 2009-2012 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#!/usr/bin/env node

var express = require('express'),
  path = require('path'),
  fs = require('fs');


var app = module.exports = express();

// Configuration

app.configure(function () {
  app.set('port', process.env.PORT || 3501);
  app.use(express['static'](path.join(__dirname, 'app')));
});

app.get(/\.json/, function (req, res) {
  var file = path.join(__dirname, 'app', req.path);
  console.log("--FILE: " + file);
  sendJsonFile(req,res,file);
});

app.get(/api.*\/$/, function (req, res) {
  var file = path.join(__dirname, 'app', req.path, '.json');
  sendJsonFile(req,res,file);
});

function sendJsonFile(req, res, path) {
  var self = this;
  var file = fs.createReadStream(path);
  res.writeHead(200, {
    'Content-Type':'text/plain'
  });

  if (req.method === 'HEAD') {
    res.end();
  } else {
    file.on('data', res.write.bind(res));
    file.on('close', function () {
      res.end();
    });
    file.on('error', function (error) {
      sendJsonError(req, res, error);
    });
  }

function sendJsonError(req, res, error) {
    res.writeHead(500, {
      'Content-Type': 'text/html'
    });
    res.write('<!doctype html>\n');
    res.write('<title>Internal Server Error</title>\n');
    res.write('<h1>Internal Server Error</h1>');
    res.write('<pre>' + escapeHtml(util.inspect(error)) + '</pre>');
    util.puts('500 Internal Server Error');
    util.puts(util.inspect(error));
  };

};

//app.get(/\.json/, function(req,res){
//  res.send("HELLO WORLD" + req);
//});
