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
  .controller('ConversationDetailCtrl', function($scope, $q, $rootScope, caseService, $routeParams, navigationService) {
    $scope.sidebardata = {};
    $scope.caseId = $routeParams.caseId;
    $scope.conversationId = $routeParams.conversationId;

    $scope.conversationMessages = caseService.getConversationMessages($routeParams.caseId, $routeParams.conversationId);
    $scope.conversationParticipants = caseService.getConversationParticipants($routeParams.caseId, $routeParams.conversationId);
    $scope.conversationMessageDraft = caseService.getMessageDraft($routeParams.caseId, $routeParams.conversationId);

    $scope.showSpinner = {
      participants: true,
      conversation: true,
      conversationMessageDraft: true
    };

    $scope.conversationParticipants.promise.then(function(response){
      $scope.showSpinner.participants = false;
    });

    $scope.conversationMessages.promise.then(function(){
      $scope.showSpinner.conversation = false;
    });

    $scope.conversationMessageDraft.promise.then(function(){
      $scope.showSpinner.conversationMessageDraft = false;
    });

    $scope.$watch("conversationMessageDraft[0]", function(){
      var toSend = $scope.conversationMessageDraft[0];
      caseService.updateMessageDraft($routeParams.caseId, $routeParams.conversationId, toSend);
    });

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
      $rootScope.$broadcast('conversation-changed', 'true');

      caseService.createMessage($routeParams.caseId, $routeParams.conversationId).then(function(){
        $scope.conversationMessages.invalidate();
        $scope.conversationMessages.resolve();

        $scope.conversationMessageDraft[0] = "";
        $rootScope.$broadcast('conversation-message-created');

        $scope.showSpinner.conversation = false;
        $rootScope.$broadcast('conversation-changed', 'false');
      });
    }
  });
