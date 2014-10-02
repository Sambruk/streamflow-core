'use strict';
angular.module('sf')
  .controller('NotesHistoryCtrl', function($scope, caseService, $routeParams) {
    $scope.caseId = $routeParams.caseId;
    $scope.notesHistory = caseService.getAllNotes($routeParams.caseId);

    $scope.showSpinner = {
    	notesHistory: true
    };

    $scope.notesHistory.promise.then(function(){
    	$scope.showSpinner.notesHistory = false;
    });

	});