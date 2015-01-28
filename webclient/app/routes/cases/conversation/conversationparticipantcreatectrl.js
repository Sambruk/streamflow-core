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
  .controller('ConversationParticipantCreateCtrl', function($scope, caseService, $routeParams, navigationService) {
    $scope.caseId = $routeParams.caseId;
    $scope.conversationId = $routeParams.conversationId;
    $scope.possibleParticipants = caseService.getPossibleConversationParticipants($routeParams.caseId, $routeParams.conversationId);


    $scope.possibleParticipants.promise.then(function(){
      setTimeout(function () {
        jQuery('.chosen-conversation-participant').chosen({ 'search_contains': true }).trigger('chosen:updated');
      }, 0);
    });

    // cache the select element as we'll be using it a few times
    var select = $('.chosen-select');
    select.chosen();
    var chosen = select.data('chosen');

    chosen.dropdown.find('input').on('keydown', function(keyEvent){
      if ((keyEvent.which === 9 || keyEvent.which === 13) && chosen.dropdown.find('li.no-results').length > 0){
        if(!this.value.match(/^$|^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/)){
          return;
        }

        var option = $('<option>').val(this.value).text(this.value);
        select.prepend(option);
        select.find(option).prop('selected', true);
        select.trigger('chosen:updated');
        $scope.externalParticipant = option[0].text;
      }
    });

    var updateParticipant = function(){
      var href = navigationService.caseHrefSimple($routeParams.caseId) + '/conversation/' + $routeParams.conversationId;
      $scope.possibleParticipants.invalidate();
      $scope.possibleParticipants.resolve();
      window.location.assign(href);
    };

    $scope.addParticipant = function($event){
      $event.preventDefault();
      var participant = $scope.participant;

      if($scope.participant){
        caseService.addParticipantToConversation($routeParams.caseId, $routeParams.conversationId, participant)
        .then(function(){
          updateParticipant();
        });
      }else{
        caseService.addExternalParticipantToConversation($routeParams.caseId, $routeParams.conversationId, $scope.externalParticipant)
        .then(function(){
          updateParticipant();
        });
      }
    };
  });
