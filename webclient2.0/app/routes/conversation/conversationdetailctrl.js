'use strict';
angular.module('sf')
  .controller('ConversationDetailCtrl', function($scope, $rootScope, caseService, $routeParams, navigationService) {
    $scope.projectId = $routeParams.projectId;
    $scope.projectType = $routeParams.projectType;
    $scope.caseId = $routeParams.caseId;
    $scope.conversationId = $routeParams.conversationId;

    $scope.conversationMessages = caseService.getConversationMessages($routeParams.caseId, $routeParams.conversationId);
    $scope.conversationParticipants = caseService.getConversationParticipants($routeParams.caseId, $routeParams.conversationId);
    $scope.conversationMessageDraft = caseService.getMessageDraft($routeParams.caseId, $routeParams.conversationId);

    $scope.$watch("conversationMessageDraft[0]", function(){
      var toSend = $scope.conversationMessageDraft[0];
      caseService.updateMessageDraft($routeParams.caseId, $routeParams.conversationId, toSend);
    });

    $scope.removeParticipant = function(participant){
      caseService.deleteParticipantFromConversation($routeParams.caseId, $routeParams.conversationId, participant).then(function(){
        $rootScope.$broadcast('participant-removed');
        //alert("Deltagare borttagen!");
      });
    }

    $scope.submitMessage = function($event){
      $event.preventDefault();
      caseService.createMessage($routeParams.caseId, $routeParams.conversationId).then(function(){
        $scope.conversationMessages.invalidate();
        $scope.conversationMessages.resolve();
        $scope.conversationMessageDraft[0] = "";
        $rootScope.$broadcast('conversation-message-created');
      });
    }
  });