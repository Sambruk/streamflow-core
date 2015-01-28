/*
 *
 * Copyright 2009-2014 Jayway Products AB
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
'use strict';

 angular.module('sf').factory('backendService', function ($window, $http, $q, httpService) {
    function SfResource(href, response) {
      if (response) {
        this.response = response.data;
      }
      this.basehref = getBaseUrl(href);
    }

    function getBaseUrl(str) {
      if (str[str.length - 1] === '/') {
        return str;
      }
      // remove last part of the url to get the base
      // this is needed since all URLs returned by the server are relative
      var lastIndex = str.lastIndexOf('/');
      return str.substring(0, lastIndex + 1);
    }

    var isId = function(href){
      return href.split('-').length - 1 === 5;
    };

    // The Instance API
    SfResource.prototype = {

      absApiUrl : function(href) {
        return httpService.absApiUrl(this.basehref + href);
      },

      createById:function (resourceData, id, urls, skipCache) {
        var values = id.split('?');
        var trimmedId = values[0];
        var query = values[1];

        var w = _.find(resourceData, function (item) {
          return item.id === trimmedId;
        });

        // Fix for broken API links to entities
        if (w && isId(w.href) && _.last(w.href) !== '/') {
          w.href = w.href + '/';
        }

        if (!w) {
          var deferred = $q.defer();
          deferred.reject({msg: 'Missing key in json ' + id, data: resourceData});
          return deferred.promise;
        }
        // handle absolute urls
        var abshref = (w.href[0] === '/') ? w.href : this.basehref + w.href;
        if (query){
          abshref += '?' + query;
        }

        return httpService.getRequest(abshref, skipCache).then(function (response) {
          //QUICK FIX:
          // workspacev2 id sends undefined urls once which breaks the loop
          // therefore, until we find out why this happens, we check that urls
          // is not undefined.
          if(urls){
            urls.push(abshref);
          }
          return new SfResource(abshref, response);
        });
      },
      getNested:function (specs, urls) {
        //console.log(urls);
        if (specs.length === 0) {
          var deferred = $q.defer();
          deferred.resolve(this);
          return deferred.promise;
        }
        var spec = specs.splice(0, 1)[0];
        var key = Object.keys(spec)[0];

        // Override API browsability if needed
        if (spec.unsafe) {
          var valueToPush = spec[key];

          this.response[key].push({
            href: valueToPush + '/',
            id: valueToPush,
            rel: valueToPush
          });
        }

        var id = spec[key];
        if (typeof id === 'function') {
          id = id();
        }

        // select the data we want to find the id in:
        var keys = key.split('.');
        var data = keys.reduce(
          function(prev, curr) {
            return prev[curr];
        }, this.response);

        var skipCache = specs.length === 0;

        var resource = this.createById(data, id, urls, skipCache);
        return resource.then(function (nextResource) {
          //console.log(nextResource);
          return nextResource.getNested(specs, urls);
        });
      },

      postNested: function(specs, data) {
        // remove last specs
        var postLink = specs.pop();
        var key = Object.keys(postLink)[0];
        var id = postLink[key];

        return this.getNested(specs).then(function(resource) {
          var w = _.find(resource.response[key], function(item){
            return item.id === id;
          });
          return httpService.postRequest(resource.basehref +  w.href, data);
        });
      }
    };

    function clear(obj) {
      obj.length = 0;
      function clearable(i) {
        return obj.hasOwnProperty(i) && ['invalidate', 'resolve'].indexOf(i) === -1;
      }

      for (var i in obj) {
        if (clearable(i)) {
          delete obj[i];
        }
      }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // The Public API
    //
    var api = {

      // Returns an Array which can be used as a Hash object as well
      // The Array object has two extra methods, invalidate and resolve which allow you to
      // perform the request again. This could be useful for example after a post request when we know that
      // the server has changed. Example in a controller
      //    $scope.foo = aService.getFoo(); // returns an array
      //    $scope.onClick = function() {
      //      $scope.foo.invalidate();
      //      $scope.foo.resolve(); // will update the foo result again.
      //    }

      //GET
      //skipCache, a bool that decides whether to skip cache (in an update event) or not.
      // needs to be passed.
      get: function(dsl, skipCache) {
        var urls = []; // stores which URL the given DSL will use, so that we can invalidate it later if necessarily
        var result = []; // the return value, which can be used as a normal JavaScript object and has two extra methods
        // invalidates the HTTP Cache
        result.urls = urls;
        $window.result = result.urls;
        result.invalidate = function() {

          httpService.invalidate(urls);
        };

        result.status = null;

        // calls the Server (again)
        result.resolve =  function() {
          // The dsl can provide a 'guard' condition preventing the calls to the server
          if (dsl.condition && !dsl.condition()) {
            var deferred = $q.defer();
            deferred.resolve(this);
            return deferred.promise;
          }

          clear(this); // clear the array
          urls.length = 0;

          // always start from the root resource and then drill down by using the provided dsl object
          result.promise = httpService.getRequest('', skipCache)
          .then(function (response) {
            var resource = new SfResource('', response);
            result.status = response.status;
            return resource.getNested(angular.copy(dsl.specs), urls);
          })
          .then(function(resource){
            dsl.onSuccess(resource, result, urls);
            return result;
          });
          return result.promise;
        };
        result.resolve();
        return result;
      },


      // Works in a similar way to get, but does a post instead
      postNested: function (specs, data, responseSelector) {

        function findInJson(spec, json) {
          var key = Object.keys(spec)[0];
          var id = spec[key];
          var searchIds = key.split('.');
          var arrayId = searchIds.pop();
          var jsonArray = searchIds.reduce(function(prev, curr) {
            return prev[curr];
          }, json);
          return _.find(jsonArray, function (item) {
            return item[arrayId] === id;
          });
        }

        return httpService.getRequest('')
        .then(function (response) {
          var resource = new SfResource('', response);
          return resource.postNested(specs, data);
        })
        .then(function(response) {
          return responseSelector ?  findInJson(responseSelector, response.data) : response;
        });
      }
    };

    return api;
  });
