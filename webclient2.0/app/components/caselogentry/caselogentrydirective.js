'use strict';

angular.module('sf')
.directive('caselogentry', function($rootScope, caseService, navigationService){
  return {
    restrict: 'E',
    templateUrl: 'components/caselogentry/caselogentry.html',
    scope: {
      caseid: '=?'
    },
    link: function(scope){
      $scope.submitCaseLogEntry = function($event){
        $event.preventDefault();
        scope.caseLogEntryToCreate;

        caseService.createCaseLogEntry(scope.caseId, scope.caseLogEntryToCreate)
        .then(function(response){
          var href = navigationService.caseHref(scope.caseId) + '/caselog';
          //NOTE: Where is scope.caseLogs declared and set?
          scope.caseLogs.invalidate();
          scope.caseLogs.resolve();
          $rootScope.$broadcast('caselog-message-created');
          window.location.assign(href);
        });
      }
    }
  };
});