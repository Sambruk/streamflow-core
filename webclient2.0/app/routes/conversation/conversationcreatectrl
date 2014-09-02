'use strict';
angular.module('sf')
    .controller('ConversationCreateCtrl',
        function($scope, caseService, $params, navigationService) {

            $scope.projectId = $params.projectId;
            $scope.projectType = $params.projectType;
            $scope.caseId = $params.caseId;
            // Necessary to get to be able to invalidate and resolve the conversation list after a create
            $scope.conversations = caseService.getSelectedConversations($params.caseId);

            $scope.submitConversation = function($event){
                $event.preventDefault();
                $('#createContact').attr('disabled', 'disabled');

                var topic = $scope.conversationTopicToCreate;
                caseService.createConversation($params.caseId, topic).then(function(response){
                    var conversationId = JSON.parse(response.data.events[0].parameters).param1;
                    var href = navigationService.caseHrefSimple($params.caseId + "/conversation/" + conversationId);
                    $scope.conversations.invalidate();
                    $scope.conversations.resolve();
                    window.location.assign(href);
                });
            }

        });