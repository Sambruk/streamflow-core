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

  var sfServices = angular.module('sf.backend.services.http', ['sf.backend.services.error-handler']);

  sfServices.factory("httpService", ['$q', '$cacheFactory', '$location', '$http', 'errorHandlerService', function ($q, $cacheFactory, $location, $http, errorHandlerService) {
    var url = $location.absUrl();
    var li = url.lastIndexOf($location.path());
    var index = url.substring(0, li);
    var baseUrl = index.substring(0, index.lastIndexOf("/"));
    var apiUrl = baseUrl + "/api/";

    var cache = $cacheFactory('sfHttpCache');

    // TODO Remove this, not needed ?
    function makeBaseAuth(user, password) {
      var tok = user + ':' + password;
      var hash = Base64.encode(tok);
      return "Basic " + hash;
    }

    return {

      baseUrl: baseUrl,

      // When using the real streamflow server in the test_folder:
      //"apiUrl": 'http://localhost:8082/streamflow/surface/customers/197606030001/',
      apiUrl:apiUrl,

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
        hrefs.forEach(function(href) { cache.remove(href)});
      },

      getRequest: function (href, skipCache) {
        var headers = {'Authorization':makeBaseAuth('administrator', 'administrator')};

        // handle absolute URLs
        var url = (href[0] === '/') ? href : this.absApiUrl(href);

        var result = cache.get(href);
        if (!result || skipCache) {
          return $http({method:'GET', url:url, cache:false, headers:headers}).then(function(response) {
            cache.put(href, response);
            return response;
          }, errorHandlerService)
        } else {
          var deferred = $q.defer();
          deferred.resolve(result);
          return deferred.promise;
        }
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


    }

  }])
})();