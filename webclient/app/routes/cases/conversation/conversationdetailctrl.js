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
      conversation: true
    };

    $scope.conversationParticipants.promise.then(function(response){
      $scope.showSpinner.participants = false;
    });

    $scope.conversationMessages.promise.then(function(){
      $scope.showSpinner.conversation = false;
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
      $rootScope.$broadcast('conversation-changed-set-spinner', 'true');

      caseService.createMessage($routeParams.caseId, $routeParams.conversationId).then(function(){
        $scope.conversationMessages.invalidate();
        $scope.conversationMessages.resolve();

        $scope.conversationMessageDraft[0] = "";
        $rootScope.$broadcast('conversation-message-created');

        $scope.showSpinner.conversation = false;
        $rootScope.$broadcast('conversation-changed-set-spinner', 'false');
      });
    }
  });
