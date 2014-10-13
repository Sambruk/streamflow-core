'use strict';
angular.module('sf')
  .controller('ConversationParticipantCreateCtrl', function($scope, caseService, $routeParams, navigationService) {
    $scope.caseId = $routeParams.caseId;
    $scope.conversationId = $routeParams.conversationId;
    $scope.possibleParticipants = caseService.getPossibleConversationParticipants($routeParams.caseId, $routeParams.conversationId);

    $scope.addParticipant = function($event){
      $event.preventDefault();
      var participant = $scope.participant;

      caseService.addParticipantToConversation($routeParams.caseId, $routeParams.conversationId, participant)
      .then(function(){
        var href = navigationService.caseHrefSimple($routeParams.caseId) + "/conversation/" + $routeParams.conversationId;
        $scope.possibleParticipants.invalidate();
        $scope.possibleParticipants.resolve();
        window.location.assign(href);
      });
    }
  });