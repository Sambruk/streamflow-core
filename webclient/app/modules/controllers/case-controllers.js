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

  var sfCase = angular.module('sf.controllers.case', ['sf.services.case', 'sf.services.navigation', 'sf.services.perspective', 'sf.services.project']);

  sfCase.controller('CaseOverviewCtrl', ['$scope', '$routeParams', 'perspectiveService', 'navigationService',
    function($scope, $params, perspectiveService, navigationService) {
      $scope.perspectives = perspectiveService.getPerspectives();
      /*$scope.myCases = perspectiveService.getMyCases();
      $scope.myLatestCases = perspectiveService.getMyLatestCases();
      $scope.myTodaysCases = perspectiveService.getTodaysCases();*/
    }]);

  sfCase.controller('CaseSearchCtrl', ['$scope', '$routeParams', 'caseService', 'navigationService',
    function($scope, $params, caseService, navigationService) {

    }]);

  sfCase.controller('CaseListCtrl', ['$scope', '$routeParams', 'projectService',
    function($scope, $params, projectService) {

      // TODO This is a bit nasty, could it be done in a better way?
      $scope.cases = projectService.getSelected($params.projectId, $params.projectType, function(){
        $scope.cases.invalidate();
        $scope.cases.resolve();

        $scope.cases = projectService.getSelected($params.projectId, $params.projectType);
    });
    
    $scope.$on('case-created', function() {
      $scope.cases.invalidate();
    });
  }]);

  sfCase.controller('CaseDetailCtrl', ['$scope', '$routeParams', 'caseService', 'navigationService',
    function($scope, $params, caseService, navigationService){
    $scope.projectId = $params.projectId;
    $scope.projectType = $params.projectType;

    $scope.caze = caseService.getSelected($params.projectId, $params.projectType, $params.caseId);
    $scope.general = caseService.getSelectedGeneral($params.projectId, $params.projectType, $params.caseId);
    $scope.notes = caseService.getSelectedNote($params.projectId, $params.projectType, $params.caseId);

    $scope.$on('case-created', function() {
        $scope.caze.invalidate();
    });

    $scope.$on('case-changed', function() {
      $scope.caze.invalidate();
      $scope.caze.resolve();
    });   
  }]);

  sfCase.controller('PrintCtrl', ['$scope', '$routeParams', 'caseService', 'navigationService',
    function($scope, $params, caseService, navigationService){
    $scope.projectId = $params.projectId;
    $scope.projectType = $params.projectType;

    $scope.caze = caseService.getSelected($params.projectId, $params.projectType, $params.caseId);
    $scope.general = caseService.getSelectedGeneral($params.projectId, $params.projectType, $params.caseId);
    $scope.notes = caseService.getSelectedNote($params.projectId, $params.projectType, $params.caseId);

    $scope.$on('case-created', function() {
        $scope.caze.invalidate();
    });

    $scope.$on('case-changed', function() {
      $scope.caze.invalidate();
      $scope.caze.resolve();
    });

    $scope.$watch('caze + general + notes', function() {
      setTimeout(function(){
         window.print();
      }, 500);
    })
  }]);

  sfCase.controller('CaseEditCtrl', ['$scope', '$routeParams', 'caseService', 'navigationService',
    function($scope, $params, caseService, navigationService) {
      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;

      $scope.caze = caseService.getSelected($params.projectId, $params.projectType, $params.caseId);
      $scope.general = caseService.getSelectedGeneral($params.projectId, $params.projectType, $params.caseId);

      $scope.notes = caseService.getSelectedNote($params.projectId, $params.projectType, $params.caseId);
      $scope.cachedNote = caseService.getSelectedNote($params.projectId, $params.projectType, $params.caseId);

      $scope.possibleCaseTypes = caseService.getPossibleCaseTypes($params.projectId, $params.projectType, $params.caseId);

      $scope.addNote = function($event){
        $event.preventDefault();
        if ($scope.notes[0].note !== $scope.cachedNote[0].note)
          caseService.addNote($params.projectId, $params.projectType, $params.caseId, $scope.notes[0]).then(function(){
            var href = navigationService.caseHref($params.caseId);
            $scope.notes.invalidate();
            $scope.notes.resolve();
            window.location.assign(href);
          });
      }

    }]);

})();
