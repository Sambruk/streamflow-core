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

  var loadSidebarData = function($scope, caseService, $params){

    $scope.projectId = $params.projectId;
    $scope.projectType = $params.projectType

    $scope.case = caseService.getSelected($params.projectId, $params.projectType, $params.caseId);
    $scope.general = caseService.getSelectedGeneral($params.projectId, $params.projectType, $params.caseId);
    $scope.contacts = caseService.getSelectedContacts($params.projectId, $params.projectType, $params.caseId);
    $scope.notes = caseService.getSelectedNotes($params.projectId, $params.projectType, $params.caseId);
    $scope.conversations = caseService.getSelectedConversations($params.projectId, $params.projectType, $params.caseId);
  }

  main.controller('CaseDetailCtrl', ['$scope', 'caseService', '$routeParams',
                  function($scope, caseService, $params){

    loadSidebarData($scope, caseService, $params);

    // Forms
    $scope.possibleForms = caseService.getSelectedPossibleForms($params.projectId, $params.projectType, $params.caseId);

    $scope.selectForm = function(formId){

      // TODO Is there a better way than this?
      $scope.$watch("form", function(){
        setTimeout(function(){
          $scope.$apply(function () {
              if ($scope.form && $scope.form[0]) {
                $scope.currentFormPage = $scope.form[0].pages[0];
              };
          });
        }, 1000);

      })

      $scope.formMessage = "";

      caseService.createSelectedForm($params.projectId, $params.projectType, $params.caseId, formId).then(function(response){
        if (response.data.events.length === 0) {
          $scope.form = caseService.getFormDraftFromForm($params.projectId, $params.projectType, $params.caseId, formId)
        }
        else {
          var draftId = JSON.parse(response.data.events[0].parameters).param1;
          $scope.form = caseService.getFormDraft($params.projectId, $params.projectType, $params.caseId, draftId);
        }

        $scope.currentFormPage = null;
      });
    }

    $scope.selectFormPage = function(page){
      $scope.currentFormPage = page;
    }

    $scope.submitForm = function(){
      caseService.submitForm($params.projectId, $params.projectType, $params.caseId, $scope.form[0].draftId);
      $scope.formMessage = "Skickat!";

      $scope.form = [];
      $scope.currentFormPage = null;
    }

    $scope.isLastPage = function(){
      return $scope.currentFormPage && $scope.form[0].pages.indexOf($scope.currentFormPage) === ($scope.form[0].pages.length - 1);
    }

    $scope.isFirstPage = function(){
      return $scope.currentFormPage && $scope.form[0].pages.indexOf($scope.currentFormPage) === 0;
    }

    $scope.nextFormPage = function(){
      var index = $scope.form[0].pages.indexOf($scope.currentFormPage);
      index += 1;
      $scope.currentFormPage = $scope.form[0].pages[index];
    }

    $scope.previousFormPage = function(){
      var index = $scope.form[0].pages.indexOf($scope.currentFormPage);
      index -= 1;
      $scope.currentFormPage = $scope.form[0].pages[index];
    }
  }]);

  main.controller('CaseEditCtrl', ['$scope', 'caseService', '$routeParams',
                  function($scope, caseService, $params) {

    loadSidebarData($scope, caseService, $params);

    $scope.possibleCaseTypes = caseService.getPossibleCaseTypes($params.projectId, $params.projectType, $params.caseId);


  }]);

  main.controller('ConversationDetailCtrl', ['$scope', 'caseService', '$routeParams',
                  function($scope, caseService, $params) {

    loadSidebarData($scope, caseService, $params);

    $scope.conversationMessages = caseService.getConversationMessages($params.projectId, $params.projectType, $params.caseId, $params.conversationId);
    $scope.conversationParticipants = caseService.getConversationParticipants($params.projectId, $params.projectType, $params.caseId, $params.conversationId);
  }]);


  // https://github.com/angular/angular.js/issues/2690
  main.factory('debounce', ['$timeout', function ($timeout) {
      return function(fn, timeout, apply){ // debounce fn
          timeout = angular.isUndefined(timeout) ? 0 : timeout;
          apply = angular.isUndefined(apply) ? true : apply; // !!default is true! most suitable to my experience
          var nthCall = 0;
          return function(){ // intercepting fn
              var that = this;
              var argz = arguments;
              nthCall++;
              var later = (function(version){
                  return function(){
                      if (version === nthCall){
                          return fn.apply(that, argz);
                      }
                  };
              })(nthCall);
              return $timeout(later, timeout, apply);
          };
      };
  }]);

})();
