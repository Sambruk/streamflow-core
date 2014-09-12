'use strict';
angular.module('sf')
  .controller('NotesHistoryCtrl', function($scope, caseService, $routeParams) {
    $scope.projectId = $routeParams.projectId;
    $scope.projectType = $routeParams.projectType;
    $scope.caseId = $routeParams.caseId;
    $scope.notesHistory = caseService.getAllNotes($routeParams.caseId);
  });
