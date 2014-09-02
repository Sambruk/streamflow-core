'use strict';
angular.module('sf')
  .controller('ConversationCreateCtrl', function($scope, caseService, $routeParams, navigationService) {
    $scope.projectId = $routeParams.projectId;
    $scope.projectType = $routeParams.projectType;
    $scope.caseId = $routeParams.caseId;
    // Necessary to get to be able to invalidate and resolve the conversation list after a create
    $scope.conversations = caseService.getSelectedConversations($routeParams.caseId);

    $scope.submitConversation = function($event){
      $event.preventDefault();
      $('#createContact').attr('disabled', 'disabled');

      var topic = $scope.conversationTopicToCreate;
      caseService.createConversation($routeParams.caseId, topic).then(function(response){
        var conversationId = JSON.parse(response.data.events[0].parameters).param1;
        var href = navigationService.caseHrefSimple($routeParams.caseId + "/conversation/" + conversationId);
        $scope.conversations.invalidate();
        $scope.conversations.resolve();
        window.location.assign(href);
      });
    }
  });