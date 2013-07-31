/*
 *
 * Copyright 2009-2012 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

(function() {
  'use strict';

  var sfCase = angular.module('sf.controllers.case', ['sf.services.case', 'sf.services.navigation', 'sf.services.project']);

  sfCase.controller('CaseListCtrl', ['$scope', 'projectService', '$routeParams',
    function($scope, projectService, $params) {
    $scope.cases = projectService.getSelected($params.projectId, $params.projectType);

    $scope.$on('case-created', function() {
      $scope.cases.invalidate();
    });
  }]);

  sfCase.controller('CaseDetailCtrl', ['$scope', 'caseService', '$routeParams', 'navigationService',
    function($scope, caseService, $params, navigationService){
    $scope.projectId = $params.projectId;
    $scope.projectType = $params.projectType;

    $scope.case = caseService.getSelected($params.projectId, $params.projectType, $params.caseId);
    $scope.general = caseService.getSelectedGeneral($params.projectId, $params.projectType, $params.caseId);
    $scope.notes = caseService.getSelectedNote($params.projectId, $params.projectType, $params.caseId);

    $scope.$on('case-created', function() {
        $scope.case.invalidate();
    });

    $scope.$on('case-changed', function() {
      $scope.case.invalidate();
      $scope.case.resolve();
    });
  }]);

  sfCase.controller('CaseEditCtrl', ['$scope', 'caseService', '$routeParams', 'navigationService',
    function($scope, caseService, $params, navigationService) {
      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;

      $scope.case = caseService.getSelected($params.projectId, $params.projectType, $params.caseId);
      $scope.general = caseService.getSelectedGeneral($params.projectId, $params.projectType, $params.caseId);

      $scope.notes = caseService.getSelectedNote($params.projectId, $params.projectType, $params.caseId);
      $scope.cachedNote = caseService.getSelectedNote($params.projectId, $params.projectType, $params.caseId);

      $scope.possibleCaseTypes = caseService.getPossibleCaseTypes($params.projectId, $params.projectType, $params.caseId);

      $scope.addNote = function($event){
        $event.preventDefault();
        if ($scope.notes[0].note !== $scope.cachedNote[0].note)
          caseService.addNote($params.projectId, $params.projectType, $params.caseId, $scope.notes[0])
      }

    }]);

})();
