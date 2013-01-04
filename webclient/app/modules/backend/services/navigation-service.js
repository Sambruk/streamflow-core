(function () {
  'use strict';


  var sfServices = angular.module('sf.backend.services.navigation', []);

  sfServices.factory('navigationService', ['$location', '$routeParams', function ($location, $routeParams) {

    return {
      caseHref: function(caseId) {
        return "#" + this.projectId() + '/' + this.caseType() + '/' + caseId;
      },
      projectId: function() {
        return $routeParams.projectId;
      },
      caseType: function() {
        return $routeParams.caseType;
      },
      caseId: function() {
        return $routeParams.caseId;
      }
    };
  }]);


})();
