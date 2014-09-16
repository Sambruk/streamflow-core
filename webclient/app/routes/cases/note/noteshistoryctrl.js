'use strict';
angular.module('sf')
  .controller('NotesHistoryCtrl', function($scope, caseService, $routeParams) {
    $scope.caseId = $routeParams.caseId;
    $scope.notesHistory = caseService.getAllNotes($routeParams.caseId);
  });
