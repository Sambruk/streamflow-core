(function () {
  'use strict';

  var sfServices = angular.module('sf.backend.services.error-handler', []);

  sfServices.factory('errorHandlerService', ['$window', '$q', function ($window, $q) {
    return function(error) {
      console.log("ERROR HANDLER ", error);
      if (error.status == 403) {
        $window.location.reload();
      }
      return $q.reject(error);
    }
  }]);
})();