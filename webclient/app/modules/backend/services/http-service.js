(function() {

  var sfServices = angular.module('sf.backend.services.http', ['sf.backend.services.error-handler']);

  sfServices.factory("httpService", ['$q', '$cacheFactory', '$location', '$http', 'errorHandlerService', function ($q, $cacheFactory, $location, $http, errorHandlerService) {
    var url = $location.absUrl();
    var li = url.lastIndexOf($location.path());
    var index = url.substring(0, li);
    var baseUrl = index.substring(0, index.lastIndexOf("/"));
    var apiUrl = baseUrl + "/api/";

    var cache = $cacheFactory('sfHttpCache');

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

      timeout: 120000,

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
        var url = this.absApiUrl(href);

        var result = cache.get(href);
        if (!result || skipCache) {
          return $http({method:'GET', url:url, cache:false, headers:headers, timeout: this.timeout}).then(function(response) {
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