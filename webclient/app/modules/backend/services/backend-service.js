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
(function () {
  'use strict';

  var sfServices = angular.module('sf.backend.services.backend', ['sf.backend.services.http']);

  sfServices.factory("backendService", ['$http', '$q', 'httpService', function ($http, $q, httpService) {
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
      var lastIndex = str.lastIndexOf("/");
      return str.substring(0, lastIndex + 1);
    }

    // The Instance API
    SfResource.prototype = {

      absApiUrl : function(href) {
        return httpService.absApiUrl(this.basehref + href);
      },

      createById:function (resourceData, id, urls) {
        var w = _.find(resourceData, function (item) {
          return item.id === id
        });

        if (!w) {
          var deferred = $q.defer();
          deferred.reject({msg: "Missing key in json " + id, data: resourceData});
          return deferred.promise;
        }
        var abshref = this.basehref + w.href;
        return httpService.getRequest(abshref).then(function (response) {
          urls && urls.push(abshref);
          return new SfResource(abshref, response);
        });
      },
      getNested:function (specs, urls) {
        if (specs.length === 0) {
          var deferred = $q.defer();
          deferred.resolve(this);
          return deferred.promise;
        }
        var spec = specs.splice(0, 1)[0];
        var key = Object.keys(spec)[0];
        var id = spec[key];
        if (typeof id === 'function') {
          id = id();
        }

        // select the data we want to find the id in:
        var keys = key.split('.');
        var data = keys.reduce(function(prev, curr) { return prev[curr]}, this.response);
        var resource = this.createById(data, id, urls);
        return resource.then(function (nextResource) {
          return nextResource.getNested(specs, urls);
        });
      },

      postNested: function(specs, data) {
        // remove last specs
        var postLink = specs.pop();
        var key = Object.keys(postLink)[0];
        var id = postLink[key];

        return this.getNested(specs).then(function(resource) {
          var w = _.find(resource.response[key], function(item){return item.id === id});
          return httpService.postRequest(resource.basehref +  w.href, data);
        });
      }
    };

    function clear(obj) {
      obj.length = 0;
      function clearable(i) { return obj.hasOwnProperty(i) && ['invalidate', 'resolve'].indexOf(i) == -1}

      for (var i in obj) {
        clearable(i) && delete obj[i];
      }
    }

    // The Static API
    var api = {
      get: function(dsl) {
        var urls = [];
        var result = [];

        result.invalidate = function() { httpService.invalidate(urls); };
        result.resolve =  function() {
          if (dsl.condition && !dsl.condition()) {
            var deferred = $q.defer();
            deferred.resolve(this);
            return deferred.promise;
          }
          clear(this); // clear the array
          urls.length = 0;

          return httpService.getRequest("").
            then(function (response) {
              var resource = new SfResource("", response);
              return resource.getNested(angular.copy(dsl.specs), urls);}).
            then(function(resource){
              dsl.onSuccess(resource, result, urls)
            });
        };
        result.resolve();
        return result;
      },

      postNested: function (specs, data, responseSelector) {

        function findInJson(spec, json) {
          var key = Object.keys(spec)[0];
          var id = spec[key];
          var searchIds = key.split('.');
          var arrayId = searchIds.pop();
          var jsonArray = searchIds.reduce(function(prev, curr) { return prev[curr]}, json);
          return _.find(jsonArray, function (item) { return item[arrayId] === id });
        }

//        var sleep = function (result) {return $timeout(function() {return result;}, 3000); };

        return httpService.getRequest("").then(function (response) {
          var resource = new SfResource("", response);
          return resource.postNested(specs, data);
        }).then(function(response) {
            return responseSelector ?  findInJson(responseSelector, response.data) : response;
        });
      }
    }

    return api;
  }]);

})();