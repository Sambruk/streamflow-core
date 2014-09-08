'use strict';

angular.module('sf')
.directive('caselogentry', function($rootScope, $location, $routeParams, $q, caseService, navigationService){
  return {
    restrict: 'E',
    templateUrl: 'components/caselogentry/caselogentry.html',
    scope: {
      caseid: '=?'
    },
    link: function(scope){
      scope.caseLogEntryToCreate = '';
      //scope.caseLogs;
      scope.caseId = $routeParams.caseId;
      scope.caseLogs;

      scope.$watch('caseLogs', function(newVal){
        if(!newVal){
          return;
        }
        scope.caseLogs = newVal;
      });

      scope.submitCaseLogEntry = function($event){
        $event.preventDefault();
        console.log(scope.caseLogEntryToCreate);
        
        scope.caseLogs = caseService.getSelectedCaseLog(scope.caseId);
        caseService.createCaseLogEntry(scope.caseId, scope.caseLogEntryToCreate)
        .then(function(response){
          console.log(response);
          var href = navigationService.caseHrefSimple(scope.caseId) + '/caselog';
          scope.caseLogs.invalidate();
          scope.caseLogs.resolve();
          $rootScope.$broadcast('caselog-message-created');
          console.log(href);
          $location.path(href);
          //window.location.assign(href);
        });
      }
    }
  };
});