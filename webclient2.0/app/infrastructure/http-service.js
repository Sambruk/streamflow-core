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

var sf = angular.module('sf');

angular.module('sf')
.factory('httpService', function ($q, $cacheFactory, $location, $http, $window, errorHandlerService, tokenService) {
    var token = tokenService.getToken();

    if (token) {
      $http.defaults.headers.common.Authorization = 'Basic ' + token;
    }

    function prepareBaseUrl() {
      var url = $location.absUrl();
      var li = url.lastIndexOf($location.path());
      var index = url.substring(0, li);
      var baseUrl = index.substring(0, index.lastIndexOf("/"));
      return baseUrl;
    }

    function prepareApiUrl(baseUrl) {
      switch (sf.env) {
        case 'production':
          return 'http://localhost:8082/streamflow/';
        default:
          return 'https://dummyuser:dummypass@test-sf.jayway.com/streamflow/';
          /*return baseUrl.replace(/(https?:\/\/)/, function (protocol) {
            return protocol + 'dummyuser:dummypass@';
          }) + '/api/';*/
      }
    }

    var baseUrl = prepareBaseUrl();
    var apiUrl = prepareApiUrl(baseUrl);
    var cache = $cacheFactory('sfHttpCache');
    
  
    return {

      baseUrl: baseUrl,
      apiUrl: apiUrl,

      info: function() {
        return cache.info();
      },

      absApiUrl: function(href) {
        return this.apiUrl + href;
      },

      isCached: function(href) {
        return !!cache.get(href);
      },

      invalidate: function(hrefs) {
        hrefs.forEach(function(href) { cache.remove(href);});
      },

      getRequest: function (href, skipCache) {
        var result = cache.get(href);
        if (!result) {
          var url = this.prepareUrl(href);
          var q = $q.defer();
          var promise = q.promise;
          cache.put(href, promise);

          q.resolve($http({ method:'GET', url: url, cache: false}));

          promise.then(function () {
            setTimeout(function () {
              cache.remove(href);
            }, 3000);
          }, function (arg) {
            errorHandlerService(arg);
            cache.remove(href);
          });

          return q.promise;
        }

        return result;
      },

      prepareUrl: function(href) {
        if (href[0] === '/')
          return (/\/streamflow/).test(href) ?  this.absApiUrl(href.substring(11)) : href;
        else
          return this.absApiUrl(href);
      },

      postRequest: function (href, data) {
        console.log(data);
        var params = $.param(data);
        var url = this.prepareUrl(href);
        return $http({
          method: 'POST',
          url: url,
          timeout: this.timeout,
          data: params,
          headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        });
      }


    };

  });
