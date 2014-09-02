'use strict';
angular.module('sf')
  .controller('PrintCtrl', function(growl, $scope, $routeParams, caseService, $q){

    $scope.caze = caseService.getSelected($routeParams.caseId);
    $scope.general = caseService.getSelectedGeneral($routeParams.caseId);
    $scope.notes = caseService.getSelectedNote($routeParams.caseId);
    var dfds = [];
    dfds.push($scope.caze.promise, $scope.general.promise, $scope.notes.promise);

    $q.all(dfds)
    .then(function () {
      // Page has to be rendered first.
      setTimeout(function () {
        window.print();
      });
    });
  });