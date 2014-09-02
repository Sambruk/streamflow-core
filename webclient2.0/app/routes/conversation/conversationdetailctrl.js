'use strict';
angular.module('sf')
    .controller('ConversationDetailCtrl',
        function($scope, $rootScope, caseService, $params, navigationService) {

            $scope.projectId = $params.projectId;
            $scope.projectType = $params.projectType;
            $scope.caseId = $params.caseId;
            $scope.conversationId = $params.conversationId;

            $scope.conversationMessages = caseService.getConversationMessages($params.caseId, $params.conversationId);
            $scope.conversationParticipants = caseService.getConversationParticipants($params.caseId, $params.conversationId);
            $scope.conversationMessageDraft = caseService.getMessageDraft($params.caseId, $params.conversationId);

            $scope.$watch("conversationMessageDraft[0]", function(){
                var toSend = $scope.conversationMessageDraft[0];
                caseService.updateMessageDraft($params.caseId, $params.conversationId, toSend);
            })

            $scope.removeParticipant = function(participant){
                caseService.deleteParticipantFromConversation($params.caseId, $params.conversationId, participant).then(function(){
                    $rootScope.$broadcast('participant-removed');
//          alert("Deltagare borttagen!");
                });
            }

            $scope.submitMessage = function($event){
                $event.preventDefault();
                caseService.createMessage($params.caseId, $params.conversationId).then(function(){
                    $scope.conversationMessages.invalidate();
                    $scope.conversationMessages.resolve();
                    $scope.conversationMessageDraft[0] = "";
                    $rootScope.$broadcast('conversation-message-created');
                });
            }
        });
