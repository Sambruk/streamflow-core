'use strict';
angular.module('sf')
    .controller('ConversationParticipantCreateCtrl',
        function($scope, caseService, $params, navigationService) {

            $scope.projectId = $params.projectId;
            $scope.projectType = $params.projectType;
            $scope.caseId = $params.caseId;
            $scope.conversationId = $params.conversationId;

            $scope.possibleParticipants = caseService.getPossibleConversationParticipants($params.caseId, $params.conversationId);

            $scope.addParticipant = function($event){
                $event.preventDefault();

                var participant = $scope.participant;

                caseService.addParticipantToConversation($params.caseId, $params.conversationId, participant).then(function(){
                    var href = navigationService.caseHrefSimple($params.caseId) + "/conversation/" + $params.conversationId;
                    $scope.possibleParticipants.invalidate();
                    $scope.possibleParticipants.resolve();
                    window.location.assign(href);
                });
            }

        });