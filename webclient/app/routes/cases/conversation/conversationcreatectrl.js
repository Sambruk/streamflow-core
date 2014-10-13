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