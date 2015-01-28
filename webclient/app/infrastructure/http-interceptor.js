'use strict';

angular.module('sf').factory('authHttpResponseInterceptor', function ($q, $location, $window) {
  return {
    responseError: function (rejection) {
      if (rejection.status === 401) {
        console.error(rejection);
        // Redirect to index, which should ask for authentication again.
        $location.path('/#');
        $window.location.reload();
      }
      return $q.reject(rejection);
    }
  };
}).config(function ($httpProvider) {
  $httpProvider.interceptors.push('authHttpResponseInterceptor');
});

