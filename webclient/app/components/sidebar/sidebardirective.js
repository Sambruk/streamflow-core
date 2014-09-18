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
.directive('sidebar', function($location, growl, contactService, sidebarService, $cacheFactory, $rootScope, $routeParams, projectService, caseService, httpService, navigationService, $q, tokenService){
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
      scope.caze = caseService.getSelected($routeParams.caseId);
      scope.possibleSendTo = caseService.getPossibleSendTo($routeParams.caseId);
      if($routeParams.formId && $routeParams.caseId){
        scope.submittedForms = caseService.getSubmittedForms($routeParams.caseId, $routeParams.formId);
      }
      scope.showSpinner = {
        caseType: true,
        caseLabels: true,
        caseDueOn: true,
        casePriority: true,
        caseToolbar: false,
        casePermissions: true,
        caseAttachment: true,
        caseGeneralInfo: true
      }; //End declare scope objects

      scope.caze.promise.then(function(){
        scope.showSpinner.caseGeneralInfo = false;
      });

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
          scope.sidebardata['caze'] = scope.caze;
        }
      });

      scope.$watch('notes', function(newVal){
        if(!newVal){
          return;
        }
        if(scope.sidebardata){
          scope.sidebardata['notes'] = scope.notes;
        }
      });

      scope.$watch('caze', function(newVal){
        if(!newVal){
          return;
        }
        scope.caze = newVal;
      });

      scope.$watch('contacts', function(newVal){
        //console.log('updated contacts');
        //console.log(newVal);
      }); //End Watch

      /* HTTP NOTIFICATIONS */
      scope.errorHandler = function(){
        var bcMessage = caseService.getMessage();
        if(bcMessage === 200)  {
          //alert('success');
            //growl.addSuccessMessage('successMessage');
        }else {
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
        scope.showSpinner.caseDueOn = false;
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
        //console.log(scope);
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
      };
      // End Send to

      // Restrict / Unrestrict
      scope.permissions = caseService.getPermissions($routeParams.caseId);
      scope.permissions.promise.then(function () {
        scope.showSpinner.casePermissions = false;
      });     
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
      
      // Close
      scope.close = function () {
        $rootScope.$broadcast('case-closed');
        sidebarService.close(scope);
      }; // End Close     
      
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
      scope.attachments.promise.then(function () {
        scope.showSpinner.caseAttachment = false;
      });    
      scope.downloadAttachment = function (attachment) {
        sidebarService.downloadAttachment(scope, attachment);
      };
      scope.deleteAttachment = function(attachmentId){
        sidebarService.deleteAttachment(scope, attachmentId);
      }; // End Attachments
      
     var defaultFiltersUrl =  caseService.getWorkspace() + '/cases/' + $routeParams.caseId + '/caselog/defaultfilters';
      httpService.getRequest(defaultFiltersUrl, false).then(function(result){
        var defaultFilters = result.data;
        scope.sideBarCaseLogs = caseService.getSelectedFilteredCaseLog($routeParams.caseId, defaultFilters);
      });

      var updateObject = function(itemToUpdate){
        console.log("ItemToUpdate");
        console.log(itemToUpdate);
        itemToUpdate.invalidate();
        itemToUpdate.resolve();
        console.log(itemToUpdate);

      };

      //Event-listeners
      scope.$on('caselog-message-created', function(){
        updateObject(scope.sideBarCaseLogs);
      });
      scope.$on('conversation-created', function(){
        updateObject(scope.conversations);
      });
      scope.$on('conversation-message-created', function(){
        updateObject(scope.conversations);
      });
      scope.$on('note-changed', function(event, data){
        updateObject(scope.notes);
      });
      scope.$on('contact-name-updated', function(){
        updateObject(scope.contacts);
      });
      scope.$on('case-changed', function(e, attr) {
        if (attr.command === 'casetype') {
          updateObject(scope.commands);
        } else if (attr.command === 'changedueon') {
          updateObject(scope.general);
        }
      });
      scope.$on('casedescription-changed', function(){
        updateObject(scope.caze);
      });
      scope.$on('participant-removed', function(){
        updateObject(scope.conversations);
      });
      scope.$on('form-submitted', function(){
        updateObject(scope.submittedFormList);
      }); //End Event-listeners
    }
  };
});