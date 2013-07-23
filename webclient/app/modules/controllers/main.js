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

  var main = angular.module('sf.controllers', ['sf.services.project', 'sf.services.case', 'sf.services.navigation']);

  function toggleToolbar($event) {
    $event.preventDefault();
    $('.functions-menu').toggleClass('open');
  }

  main.controller('ProjectListCtrl', ['$scope', 'projectService', '$routeParams', 'navigationService', '$rootScope', function($scope, projectService, $params, navigationService, $rootScope) {
    $scope.projects = projectService.getAll();
    $scope.toggleToolbar = toggleToolbar;

    $scope.createCase = function(){
      $rootScope.$broadcast('case-created');

      projectService.createCase($params.projectId, $params.projectType).then(function(response){
        var caseId = response.data.events[1].entity;
        var href = navigationService.caseHref(caseId);

        window.location.replace(href + "/edit");
      });
    }

  }]);

  main.controller('CaseListCtrl', ['$scope', 'projectService', '$routeParams',
                  function($scope, projectService, $params) {
    $scope.cases = projectService.getSelected($params.projectId, $params.projectType);

    $scope.$on('case-created', function() {
        $scope.cases.invalidate();
    });
  }]);

  main.controller('SidebarCtrl', ['$scope', 'projectService', '$routeParams', 'navigationService', 'caseService',
                  function($scope, projectService, $params, navigationService, caseService) {

    $scope.projectId = $params.projectId;
    $scope.projectType = $params.projectType;

    $scope.case = caseService.getSelected($params.projectId, $params.projectType, $params.caseId);
    $scope.general = caseService.getSelectedGeneral($params.projectId, $params.projectType, $params.caseId);
    $scope.contacts = caseService.getSelectedContacts($params.projectId, $params.projectType, $params.caseId);
    $scope.notes = caseService.getSelectedNotes($params.projectId, $params.projectType, $params.caseId);
    $scope.conversations = caseService.getSelectedConversations($params.projectId, $params.projectType, $params.caseId);
    $scope.attachments = caseService.getSelectedAttachments($params.projectId, $params.projectType, $params.caseId);
    $scope.caseLog = caseService.getSelectedCaseLog($params.projectId, $params.projectType, $params.caseId);

    $scope.closeCase = function(){
      alert("TODO");
    }

    $scope.changeOwner = function(){
      alert("TODO");
    }

    $scope.assign = function(){
      alert("TODO");
    }

    $scope.moreCommands = function(){
      alert("TODO");
    }

    $scope.deleteCase = function(){

      var callback = function(){

        alert("Ärendet är nu borttaget. Var vänlig ladda om sidan.");

        // TODO Find a way to invalidate the case list
        var href = navigationService.caseListHref();
        window.location.replace(href);
      }

      caseService.deleteCase($params.projectId, $params.projectType, $params.caseId, callback);
    }

    $scope.downloadAttachment = function(attachmentId){
      alert("Not supported - need absolute url in API.");
    }

    $scope.deleteAttachment = function(attachmentId){

      var callback = function(){
        $scope.attachments.invalidate();
        $scope.attachments.resolve();
      }
      caseService.deleteAttachment($params.projectId, $params.projectType, $params.caseId, attachmentId, callback);
    }

    $scope.showContact = function(contactId){
      alert("Not supported - need UX for this.");
    }
  }]);

  main.controller('CaseDetailCtrl', ['$scope', 'caseService', '$routeParams', 'navigationService',
                  function($scope, caseService, $params, navigationService){

    $scope.projectId = $params.projectId;
    $scope.projectType = $params.projectType;

    $scope.case = caseService.getSelected($params.projectId, $params.projectType, $params.caseId);
    $scope.general = caseService.getSelectedGeneral($params.projectId, $params.projectType, $params.caseId);

    $scope.$on('case-created', function() {
        $scope.case.invalidate();
    });

    $scope.$on('case-changed', function() {
        $scope.case.invalidate();
        $scope.case.resolve();
    });
  }]);

  main.controller('CaseEditCtrl', ['$scope', 'caseService', '$routeParams', 'navigationService',
                  function($scope, caseService, $params, navigationService) {

    $scope.projectId = $params.projectId;
    $scope.projectType = $params.projectType;

    $scope.case = caseService.getSelected($params.projectId, $params.projectType, $params.caseId);
    $scope.general = caseService.getSelectedGeneral($params.projectId, $params.projectType, $params.caseId);

    $scope.possibleCaseTypes = caseService.getPossibleCaseTypes($params.projectId, $params.projectType, $params.caseId);

  }]);

  main.controller('ConversationDetailCtrl', ['$scope', 'caseService', '$routeParams','navigationService',
                  function($scope, caseService, $params, navigationService) {

    $scope.conversationMessages = caseService.getConversationMessages($params.projectId, $params.projectType, $params.caseId, $params.conversationId);
    $scope.conversationParticipants = caseService.getConversationParticipants($params.projectId, $params.projectType, $params.caseId, $params.conversationId);
    $scope.conversationMessageDraft = caseService.getMessageDraft($params.projectId, $params.projectType, $params.caseId, $params.conversationId);

    $scope.$watch("conversationMessageDraft[0]", function(){
      var toSend = $scope.conversationMessageDraft[0];
      caseService.updateMessageDraft($params.projectId, $params.projectType, $params.caseId, $params.conversationId, toSend);
    })

    $scope.submitMessage = function($event){
      var toSend = $scope.conversationMessageDraft[0];
      $event.preventDefault();
      caseService.updateMessageDraft($params.projectId, $params.projectType, $params.caseId, $params.conversationId, toSend);
    }
  }]);

  main.controller('FormCtrl', ['$scope', 'caseService', '$routeParams',
                  function($scope, caseService, $params) {

    $scope.possibleForms = caseService.getSelectedPossibleForms($params.projectId, $params.projectType, $params.caseId);

    $scope.selectForm = function(formId){

      // TODO Is there a better way than this?
      $scope.$watch("form", function(){
        setTimeout(function(){
          $scope.$apply(function () {
              if ($scope.form && $scope.form[0]) {
                $scope.currentFormPage = $scope.form[0].enhancedPages[0];
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
      return $scope.currentFormPage && $scope.form[0].enhancedPages.indexOf($scope.currentFormPage) === ($scope.form[0].enhancedPages.length - 1);
    }

    $scope.isFirstPage = function(){
      return $scope.currentFormPage && $scope.form[0].enhancedPages.indexOf($scope.currentFormPage) === 0;
    }

    $scope.nextFormPage = function(){
      var index = $scope.form[0].enhancedPages.indexOf($scope.currentFormPage);
      index += 1;
      $scope.currentFormPage = $scope.form[0].enhancedPages[index];
    }

    $scope.previousFormPage = function(){
      var index = $scope.form[0].enhancedPages.indexOf($scope.currentFormPage);
      index -= 1;
      $scope.currentFormPage = $scope.form[0].enhancedPages[index];
    }
  }]);

})();
