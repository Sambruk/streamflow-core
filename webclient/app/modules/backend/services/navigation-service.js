(function () {
  'use strict';


  var sfServices = angular.module('sf.backend.services.navigation', []);

  sfServices.factory('navigationService', ['$location', '$routeParams', function ($location, $routeParams) {

    return {
      projectId: function() {
        return $routeParams.projectId;
      },
      caseType: function() {
        return $routeParams.caseType;
      }
    };
  }]);


})();
