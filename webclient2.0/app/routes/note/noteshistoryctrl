'use strict';
angular.module('sf')
    .controller('NotesHistoryCtrl',
        function($scope, caseService, $params) {

            $scope.projectId = $params.projectId;
            $scope.projectType = $params.projectType;
            $scope.caseId = $params.caseId;

            $scope.notesHistory = caseService.getAllNotes($params.caseId);
        });
