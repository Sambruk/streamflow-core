/*
 *
 * Copyright 2009-2014 Jayway Products AB
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

'use strict';

angular.module('sf')
.directive('sidebar', function($location, growl, contactService, sidebarService, fileService, $cacheFactory, $rootScope, $routeParams, projectService, caseService, httpService, navigationService, $q, tokenService, checkPermissionService){
  return {
    restrict: 'E',
    templateUrl: 'components/sidebar/sidebar.html',
    scope: {
      sidebardata: '='
    },
    link: function(scope){
      //Declare scope objects
      scope.projectId = $routeParams.projectId;
      scope.projectType = $routeParams.projectType;
      scope.general = caseService.getSelectedGeneral($routeParams.caseId);
      scope.contacts = caseService.getSelectedContacts($routeParams.caseId);
      scope.conversations = caseService.getSelectedConversations($routeParams.caseId);
      scope.attachments = caseService.getSelectedAttachments($routeParams.caseId);
      scope.apiUrl = httpService.apiUrl + caseService.getWorkspace();
      scope.possiblePriorities = caseService.getPossiblePriorities($routeParams.caseId);
      scope.possibleCaseTypes = caseService.getPossibleCaseTypes($routeParams.caseId);
      scope.possibleResolutions = caseService.getPossibleResolutions($routeParams.caseId);
      scope.possibleForms = caseService.getSelectedPossibleForms($routeParams.caseId);
      scope.submittedFormList = caseService.getSubmittedFormList($routeParams.caseId);
      scope.notes = caseService.getSelectedNote($routeParams.caseId);
      // scope.notesHistory = caseService.getAllNotes($routeParams.caseId);
      scope.caze = caseService.getSelected($routeParams.caseId);
      scope.possibleSendTo = caseService.getPossibleSendTo($routeParams.caseId);
      scope.uploadProgress = 0;
      scope.showExport = false;
      scope.exportSubmittedForms = false;
      scope.exportAttachments = false;
      scope.exportConversations = false;
      scope.exportContacts = false;
      scope.exportCaseLog = false;

      scope.caze.promise.then(function(){
        checkPermissionService.checkPermissions(scope, scope.caze.queries, ['exportpdf'], ['canExportCase']);
      });
      scope.general.promise.then(function(){
        checkPermissionService.checkPermissions(scope, scope.general.commands, ['casetype', 'changedueon', 'changedescription', 'changepriority'], ['canChangeCaseType', 'canChangeDueOn', 'canChangeDescription', 'canChangePriority']);
        if (!scope.canChangeCaseType) {
          $('.case-type-selected').addClass('cursor-default');
        }

        if (scope.sidebardata && scope.canChangeDescription) {
          scope.sidebardata.canChangeDescription = true;
        }
      });
      scope.notes.promise.then(function(){

        checkPermissionService.checkPermissions(scope, scope.notes.commands, ['addnote'], ['canAddNote']);

        if(scope.sidebardata && scope.canAddNote){
          scope.sidebardata.canAddNote = true;
        }
      });
      scope.contacts.promise.then(function(){
        checkPermissionService.checkPermissions(scope, scope.contacts.commands, ['add'], ['canAddContact']);
      });
      scope.conversations.promise.then(function(){
        checkPermissionService.checkPermissions(scope, scope.conversations.commands, ['create'], ['canCreateConversation']);
      });
      scope.attachments.promise.then(function(){
        checkPermissionService.checkPermissions(scope, scope.attachments.queries, ['createattachment'], ['canCreateAttachment']);
      });
      scope.possibleForms.promise.then(function(){
        sidebarService.checkPossibleForms(scope, scope.possibleForms);
      });

      if($routeParams.formId && $routeParams.caseId){
        scope.submittedForms = caseService.getSubmittedForms($routeParams.caseId, $routeParams.formId);
      }
      //End declare scope objects

      //Watch
      scope.$watch('caze[0]', function(newVal){
        if(!newVal){
          return;
        }
        $rootScope.$broadcast('breadcrumb-updated',
          [{projectId: scope.caze[0].owner},
          {projectType: scope.caze[0].listType},
          {caseId: scope.caze[0].caseId}]);

        if(scope.sidebardata){
          scope.sidebardata.caze = scope.caze;
        }
      });

      scope.$watch('notes', function(newVal){
        if(!newVal){
          return;
        }
        if(scope.sidebardata){
          scope.sidebardata.notes = scope.notes;
        }
      });

      scope.$watch('conversations', function(newVal){
        if(!newVal){
          return;
        }
        if(scope.sidebardata){
          scope.sidebardata.conversations = scope.conversations;
        }
      });

      scope.$watch('caze', function(newVal){
        if(!newVal){
          return;
        }
        scope.caze = newVal;
      });

      /* HTTP NOTIFICATIONS */
      scope.errorHandler = function(){
        var bcMessage = caseService.getMessage();
        if(bcMessage !== 200)  {
          growl.warning('errorMessage');
        }
      };
      //error-handler
      scope.$on('httpRequestInitiated', scope.errorHandler);
      // End HTTP NOTIFICATIONS

      //Contact
      scope.submitContact = contactService.submitContact; //End Contact

      //Resolve
      scope.resolveCase = function() {
        sidebarService.resolveCase(scope);
      };
      scope.onResolveButtonClicked = function(){
        sidebarService.onResolveButtonClicked(scope);
      };
      scope.onCancelResolveButtonClicked = function () {
        scope.commandView = '';
      }; //End Resolve

      // Due on
      scope.general.promise.then(function (result) {
        scope.dueOnShortStartValue = result[0].dueOnShort;

      });
      scope.changeDueOn = function (date) {
        sidebarService.changeDueOn(scope, date);
      }; // End due on

      // Priority
      scope.priority = '-1';
      scope.priorityColor = {};
      scope.activePriorityColor = {};
      sidebarService.priority(scope);
      scope.changePriorityLevel = function(priorityId){
        sidebarService.changePriorityLevel(scope, priorityId);
      }; //End Priority

      // Case type
      sidebarService.caseType(scope);
      scope.changeCaseType = function(caseType){
        sidebarService.changeCaseType(scope, caseType);
      }; // End case type

      // Case labels
      scope.allCaseLabels = [];
      scope.activeLabels = [];
      scope.previousActiveLabels = [];
      var updateCaseLabels = function() {
        sidebarService.updateCaseLabels(scope);
      };
      updateCaseLabels();
      scope.changeCaseLabels = function (labels) {
        sidebarService.changeCaseLabels(scope, labels);
      }; // End case labels

      // Commands (toolbar)
      var updateToolbar = function () {
        sidebarService.updateToolbar(scope);
      };
      updateToolbar(); // End commands (toolbar)

      // Send to
      scope.sendTo = function () {
        sidebarService.sendTo(scope);
      };
      scope.sendToIdChanged = function (id) {
        scope.sendToId = id;
      };
      scope.onSendToButtonClicked = function () {
        sidebarService.onSendToButtonClicked(scope);
      };// End Send to

      // Restrict / Unrestrict
      scope.permissions = caseService.getPermissions($routeParams.caseId);

      scope.unrestrict = function () {
        sidebarService.unrestrict(scope);
      };
      scope.restrict = function () {
        sidebarService.restrict(scope);
      }; // End Restrict / Unrestrict

      // Mark Read / Unread
      scope.markReadUnread = function (read) {
        sidebarService.markReadUnread(scope, read);
      }; // End Mark Read / Unread

      // Show Export Pdf
      scope.showExportPopUp = function () {
        scope.showExport =! scope.showExport;
        scope.commandView = true;
      }; // End Show Export Pdf

      scope.onExportButtonClicked = function () {
        caseService.getCasePdf($routeParams.caseId, scope.exportSubmittedForms, scope.exportAttachments, scope.exportConversations, scope.exportContacts, scope.exportCaseLog);
        scope.showExportPopUp();
      };// End Send to

      // Close
      scope.close = function () {
        sidebarService.close(scope);
      };
      scope.onCancelRequiredCaseTypeButtonClicked = function () {
        scope.commandView = '';
      };// End Close

      // FormOnClose
      scope.closeWithForm = function(){
        scope.commandView = 'formonclose';
        scope.show = true;
      }; // End FormOnClose

      // Reopen
      scope.reopen = function(){
        sidebarService.reopen(scope);
      };
      // End Reopen

      // Delete
      scope.deleteCase = function () {
        sidebarService.deleteCase(scope);
      }; // End Delete

      // Assign / Unassign
      scope.assign = function () {
        sidebarService.assign(scope);
      };
      scope.unassign = function () {
        sidebarService.unassign(scope);
      }; // End Assign / Unassign

      // Attachments
      scope.downloadAttachment = function (attachment) {
        sidebarService.downloadAttachment(scope, attachment);
      };
      scope.deleteAttachment = function(attachmentId){
        sidebarService.deleteAttachment(scope, attachmentId);
      }; // End Attachments

      scope.exportCaseInfo = function(){
        scope.caseExportInfo = caseService.getCaseExportInfo($routeParams.caseId);
      };
      scope.onFileSelect = function($files){
        var url = httpService.apiUrl + 'workspacev2/cases/' + $routeParams.caseId + '/attachments/createattachment';
        fileService.uploadFiles($files, url);
        updateObject(scope.attachments);
      };


      // Show / Close pop up
      scope.showExportCaseInfoPopUp = function(){
        scope.showExportInfo = true;
      };
      scope.showCaseInfoPopUp = function(){
        scope.showCaseInfo = true;
      };
      scope.closePopUp = function(){
        scope.showCaseInfo = false;
        scope.showExportInfo = false;
        scope.commandView = '';
      };

      // Filter for caselog
      var defaultFiltersUrl =  caseService.getWorkspace() + '/cases/' + $routeParams.caseId + '/caselog/defaultfilters';
      httpService.getRequest(defaultFiltersUrl, false).then(function(result){
        scope.defaultFilters = result.data;
        scope.sideBarCaseLogs = caseService.getSelectedFilteredCaseLog($routeParams.caseId, scope.defaultFilters);
      }); // End Filter for caselog

      var updateObject = function(itemToUpdate){
        itemToUpdate.invalidate();
        itemToUpdate.resolve();
      };

      //Event-listeners
      scope.$on('case-changed', function() {
        updateObject(scope.possibleCaseTypes);
        updateObject(scope.sendToRecipients);
        sidebarService.caseType(scope);
      });
      scope.$on('case-unassigned', function(){
        checkFilterCaseLog('system');
      });
      scope.$on('case-assigned', function(){
        checkFilterCaseLog('system');
      });
      scope.$on('case-restricted', function(){
        checkFilterCaseLog('system');
      });
      scope.$on('case-unrestricted', function(){
        checkFilterCaseLog('system');
      });
      scope.$on('case-type-changed', function(){
        checkFilterCaseLog('system');
      });
      scope.$on('casedescription-changed', function(){
        updateObject(scope.caze);
      });
      // scope.$on('note-changed', function(event){
      //   updateObject(scope.notes);
      // });
      scope.$on('form-submitted', function(){
        updateObject(scope.submittedFormList);
        checkFilterCaseLog('form');
      });
      scope.$on('conversation-created', function(){
        updateObject(scope.conversations);
        checkFilterCaseLog('conversation');
      });
      scope.$on('conversation-message-created', function(){
        // updateObject(scope.conversations);
        checkFilterCaseLog('conversation');
      });
      scope.$on('participant-removed', function(){
        updateObject(scope.conversations);
      });
      scope.$on('contact-created', function(){
        updateObject(scope.contacts);
        checkFilterCaseLog('contact');
      });
      scope.$on('contact-name-updated', function(){
        updateObject(scope.contacts);
        checkFilterCaseLog('contact');
      });
      scope.$on('caselog-message-created', function(){
        // updateObject(scope.sideBarCaseLogs);
        checkFilterCaseLog('custom');
      });
      //End Event-listeners

      var checkFilterCaseLog = function(filter){
        if(scope.defaultFilters[filter] === false){
          return;
        }
        updateObject(scope.sideBarCaseLogs);
      };
    }
  };
});
