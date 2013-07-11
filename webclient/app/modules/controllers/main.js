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

  var main = angular.module('sf.controllers', ['sf.services.project', 'sf.services.case']);

  function toggleToolbar($event) {
    $event.preventDefault();
    $('.functions-menu').toggleClass('open');
  }

  main.controller('ProjectListCtrl', ['$scope', 'projectService', function($scope, projectService) {
    $scope.projects = projectService.getAll();
    $scope.toggleToolbar = toggleToolbar;
  }]);

  main.controller('CaseListCtrl', ['$scope', 'projectService', '$routeParams',
                  function($scope, projectService, $params) {
    $scope.cases = projectService.getSelected($params.projectId, $params.projectType);
  }]);

  main.controller('CaseDetailCtrl', ['$scope', 'caseService', '$routeParams',
                  function($scope, caseService, $params){
    console.log('params', $params);
    $scope.case = caseService.getSelected($params.projectId, $params.projectType, $params.caseId);
    $scope.general = caseService.getSelectedGeneral($params.projectId, $params.projectType, $params.caseId);
    $scope.contacts = caseService.getSelectedContacts($params.projectId, $params.projectType, $params.caseId);
    $scope.notes = caseService.getSelectedNotes($params.projectId, $params.projectType, $params.caseId);
    $scope.conversations = caseService.getSelectedConversations($params.projectId, $params.projectType, $params.caseId);
    $scope.possibleForms = caseService.getSelectedPossibleForms($params.projectId, $params.projectType, $params.caseId);

    $scope.selectForm = function(formId){

      caseService.createSelectedForm($params.projectId, $params.projectType, $params.caseId, formId).then(function(response){
        if (response.data.events.length === 0) {
          $scope.form = caseService.getFormDraftFromForm($params.projectId, $params.projectType, $params.caseId, formId)
        }
        else {
          var draftId = JSON.parse(response.data.events[0].parameters).param1;
          $scope.form = caseService.getFormDraft($params.projectId, $params.projectType, $params.caseId, draftId);
        }
      });
    }

    $scope.selectFormPage = function(page){
      $scope.currentFormPage = page;
    }

    $scope.saveForm = function(fieldId, $event){
      var value = $($event.target).val();
      caseService.updateField($params.projectId, $params.projectType, $params.caseId, $scope.form[0].draftId, fieldId, value);
    }

    $scope.submitForm = function(){
      caseService.submitForm($params.projectId, $params.projectType, $params.caseId, $scope.form[0].draftId);
      $scope.formMessage = "Skickat!";

      $scope.form = [];
      $scope.currentFormPage = null;
    }

    $scope.isLastPage = function(page){
      if (page)
        return $scope.form[0].pages.indexOf(page) === ($scope.form[0].pages.length - 1);

      return false;
    }
  }]);


})();
