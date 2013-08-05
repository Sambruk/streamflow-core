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

  var sfConversation = angular.module('sf.controllers.conversation', ['sf.services.case', 'sf.services.navigation']);

  sfConversation.controller('ConversationCreateCtrl', ['$scope', 'caseService', '$routeParams','navigationService',
    function($scope, caseService, $params, navigationService) {

      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;
      $scope.caseId = $params.caseId;
      // Necessary to get to be able to invalidate and resolve the conversation list after a create
      $scope.conversations = caseService.getSelectedConversations($params.projectId, $params.projectType, $params.caseId);

      $scope.submitConversation = function($event){
        $event.preventDefault();

        var topic = $scope.conversationTopicToCreate;
        caseService.createConversation($params.projectId, $params.projectType, $params.caseId, topic).then(function(response){
          var conversationId = JSON.parse(response.data.events[0].parameters).param1;
          var href = navigationService.caseHref($params.caseId) + "/conversation/" + conversationId;
          $scope.conversations.invalidate();
          $scope.conversations.resolve();
          window.location.assign(href);
        });
      }

    }]);

  sfConversation.controller('ConversationDetailCtrl', ['$scope', 'caseService', '$routeParams','navigationService',
    function($scope, caseService, $params, navigationService) {

      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;
      $scope.caseId = $params.caseId;
      $scope.conversationId = $params.conversationId;

      $scope.conversationMessages = caseService.getConversationMessages($params.projectId, $params.projectType, $params.caseId, $params.conversationId);
      $scope.conversationParticipants = caseService.getConversationParticipants($params.projectId, $params.projectType, $params.caseId, $params.conversationId);
      $scope.conversationMessageDraft = caseService.getMessageDraft($params.projectId, $params.projectType, $params.caseId, $params.conversationId);

      $scope.$watch("conversationMessageDraft[0]", function(){
        var toSend = $scope.conversationMessageDraft[0];
        caseService.updateMessageDraft($params.projectId, $params.projectType, $params.caseId, $params.conversationId, toSend);
      })

      $scope.removeParticipant = function(participant){
        caseService.deleteParticipantFromConversation($params.projectId, $params.projectType, $params.caseId, $params.conversationId, participant).then(function(){
          $scope.conversationParticipants.invalidate();
          $scope.conversationParticipants.resolve();
        });
      }

      $scope.submitMessage = function($event){
        $event.preventDefault();
        caseService.createMessage($params.projectId, $params.projectType, $params.caseId, $params.conversationId).then(function(){
          $scope.conversationMessages.invalidate();
          $scope.conversationMessages.resolve();
          $scope.conversationMessageDraft = "";
        });
      }
    }]);

  sfConversation.controller('ConversationParticipantCreateCtrl', ['$scope', 'caseService', '$routeParams','navigationService',
    function($scope, caseService, $params, navigationService) {

      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;
      $scope.caseId = $params.caseId;
      $scope.conversationId = $params.conversationId;
      $scope.conversationParticipants = caseService.getConversationParticipants($params.projectId, $params.projectType, $params.caseId, $params.conversationId);

      $scope.possibleParticipants = caseService.getPossibleConversationParticipants($params.projectId, $params.projectType, $params.caseId, $params.conversationId);

      $scope.addParticipant = function($event){
        $event.preventDefault();

        var participant = $scope.participant;

        caseService.addParticipantToConversation($params.projectId, $params.projectType, $params.caseId, $params.conversationId, participant).then(function(){
          var href = navigationService.caseHref($params.caseId) + "/conversation/" + $params.conversationId;
          $scope.conversationParticipants.invalidate();
          $scope.conversationParticipants.resolve();
          window.location.assign(href);
        });
      }

    }]);

})();
