'use strict';
angular.module('sf')
  .controller('ConversationCreateCtrl', function($scope, $rootScope, caseService, $routeParams, navigationService) {
    $scope.sidebardata = {};
    $scope.caseId = $routeParams.caseId;

    $scope.submitConversation = function($event){
      $event.preventDefault();
      $('#createContact').attr('disabled', 'disabled');

      var topic = $scope.conversationTopicToCreate;
      caseService.createConversation($routeParams.caseId, topic).then(function(response){
        var conversationId = JSON.parse(response.data.events[0].parameters).param1;
        var href = navigationService.caseHrefSimple($routeParams.caseId + "/conversation/" + conversationId);
        $rootScope.$broadcast('conversation-created');
        window.location.assign(href);
      });
    }
  });