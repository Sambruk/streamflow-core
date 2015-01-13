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
  .controller('ConversationDetailCtrl', function($scope, $q, $rootScope, caseService, $routeParams, navigationService, tokenService, httpService, fileService, checkPermissionService) {

    $scope.apiUrl = httpService.apiUrl + caseService.getWorkspace();
    $scope.sidebardata = {};
    $scope.caseId = $routeParams.caseId;
    $scope.conversationId = $routeParams.conversationId;

    $scope.conversationMessages = caseService.getConversationMessages($routeParams.caseId, $routeParams.conversationId);
    $scope.conversationParticipants = caseService.getConversationParticipants($routeParams.caseId, $routeParams.conversationId);
    $scope.conversationMessageDraft = caseService.getMessageDraft($routeParams.caseId, $routeParams.conversationId);
    $scope.conversationMessageDraftAttachments = caseService.getMessageDraftAttachments($routeParams.caseId, $routeParams.conversationId);

    $scope.showSpinner = {
      conversation: true,
      conversationMessageDraft: true
    };

    $scope.conversationParticipants.promise.then(function(response){
      // console.log($scope.conversationParticipants)
      checkPermissionService.checkPermissions($scope, $scope.conversationParticipants.commands, ['addparticipant', 'addexternalparticipant'], ['canAddParticipant', 'canAddExternalParticipant']);
    });

    $scope.conversationMessages.promise.then(function(){
      $scope.showSpinner.conversation = false;
      checkPermissionService.checkPermission($scope, $scope.conversationMessages.commands, 'createmessagefromdraft', 'canCreateMessageFromDraft');
    });

    $scope.conversationMessageDraft.promise.then(function(){
      $scope.showSpinner.conversationMessageDraft = false;
    });

    $scope.$watch('sidebardata.conversations', function(newVal){
      if(!newVal){
        return;
      }
      $scope.conversations = $scope.sidebardata.conversations;
    });

    var updateObject = function(itemToUpdate){
      itemToUpdate.invalidate();
      itemToUpdate.resolve();
    };

    var getApiUrl = function(){
      // Hack to replace dummy user and pass with authentication from token.
      // This is normally sent by httpF headers in ajax but not possible here.
      var apiUrl = $scope.apiUrl.replace(/https:\/\/(.*)@/, function () {
        var userPass = window.atob(tokenService.getToken());
        return 'https://' + userPass + '@' ;
      });
      return apiUrl;
    };


    $scope.changeMessageDraft = function($event){
      var message = $event.currentTarget.value;
      caseService.updateMessageDraft($scope.caseId, $scope.conversationId, message).then(function(){
        updateObject($scope.conversationMessageDraft);
      });
    };

    $scope.onMessageFileSelect = function($files){
      var url = httpService.apiUrl + 'workspacev2/cases/'+$routeParams.caseId+'/conversations/'+$routeParams.conversationId+'/messages/messagedraft/attachments/createattachment';
      fileService.uploadFiles($files, url);
      updateObject($scope.conversationMessageDraftAttachments);
    }

    $scope.removeParticipant = function(participant){
      $rootScope.$broadcast('conversation-changed-set-spinner', 'true');
      caseService.deleteParticipantFromConversation($routeParams.caseId, $routeParams.conversationId, participant).then(function(){
        $rootScope.$broadcast('participant-removed');
        $rootScope.$broadcast('conversation-changed-set-spinner', 'false');
      });
    }

    $scope.submitMessage = function($event){
      $event.preventDefault();
      $scope.showSpinner.conversation = true;

      caseService.createMessage($routeParams.caseId, $routeParams.conversationId).then(function(){
        updateObject($scope.conversationMessages);
        updateObject($scope.conversationMessageDraftAttachments);

        $scope.conversationMessageDraft[0] = "";
        $rootScope.$broadcast('conversation-message-created');

        $scope.showSpinner.conversation = false;
      });
    }

    $scope.deleteDraftAttachment = function(attachment){
      caseService.deleteDraftAttachment($routeParams.caseId, $routeParams.conversationId, attachment.id).then(function(){
        updateObject($scope.conversationMessageDraftAttachments);
      });
    }

    $scope.downloadDraftAttachment = function(attachment){
      var apiUrl = getApiUrl();

      var url = apiUrl + '/cases/' + $routeParams.caseId + '/conversations/' + $routeParams.conversationId + '/messages/messagedraft/attachments/' + attachment.href + 'download';
      window.location.replace(url);
    }

    $scope.downloadMessageAttachment = function (message, attachment) {
      var apiUrl = getApiUrl();

      var url = apiUrl + '/cases/' + $routeParams.caseId + '/conversations/' + $scope.conversationId + '/messages/' + message.id + '/attachments/' + attachment.href + 'download';
      window.location.replace(url);
    };

  });
