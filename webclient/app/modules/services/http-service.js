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
(function() {
  'use strict';

  var sfServices = angular.module('sf.services.http', ['sf.services.error-handler']);

  sfServices.factory("httpService", ['$q', '$cacheFactory', '$location', '$http', 'errorHandlerService', function ($q, $cacheFactory, $location, $http, errorHandlerService) {

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
          //return baseUrl + "/../streamflow"/;
          return baseUrl + "/api/";
      }
    }

    // TODO Remove this, not needed ?
    function makeBaseAuth(user, password) {
      var tok = user + ':' + password;
      var hash = Base64.encode(tok);
      return "Basic " + hash;
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

      headers: function() {
        var headers = {'Authorization':makeBaseAuth('administrator', 'administrator')};
      },

      getRequest: function (href, skipCache) {
        var result = cache.get(href);
        if (result && !skipCache)
          return this.wrapInPromise(result);
        return this.makeRequest(href);
      },

      wrapInPromise: function(result) {
        var deferred = $q.defer();
        deferred.resolve(result);
        return deferred.promise;
      },

      makeRequest: function(href) {
        var url = this.prepareUrl(href);
        var request = $http({ method:'GET', url: url, cache: false });
        // Bind href parameter to cacheResponse.
        var cacheResponse = _.bind(this.cacheResponse, null, href);
        return request.then(cacheResponse, errorHandlerService);
      },

      prepareUrl: function(href) {
        if (href[0] === '/')
          return (/\/streamflow/).test(href) ?  this.absApiUrl(href.substring(11)) : href;
        else
          return this.absApiUrl(href);
      },

      cacheResponse: function(href, response) {
        cache.put(href, response);
        return response;
      },

      postRequest: function (href, data) {
        var params = $.param(data);
        return $http({
          method: 'POST',
          url: this.absApiUrl(href),
          timeout: this.timeout,
          data: params,
          headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        });
      }


    };

  }]);
})();
