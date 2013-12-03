/*
 *
 * Copyright 2009-2013 Jayway Products AB
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
(function() {
  'use strict';

  var sfSidebar = angular.module('sf.controllers.sidebar', ['sf.services.case', 'sf.services.navigation', 'sf.services.project','sf.services.http']);

  sfSidebar.controller('SidebarCtrl', ['$scope', 'projectService', '$routeParams', 'navigationService', 'caseService', 'httpService', 
    function($scope, projectService, $params, navigationService, caseService, httpService) {

      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;

      $scope.caze = caseService.getSelected($params.caseId);
      $scope.commands = caseService.getSelectedCommands($params.caseId);
      $scope.general = caseService.getSelectedGeneral($params.caseId);
      $scope.contacts = caseService.getSelectedContacts($params.caseId);
      $scope.conversations = caseService.getSelectedConversations($params.caseId);
      $scope.attachments = caseService.getSelectedAttachments($params.caseId);

     var defaultFiltersUrl = 'workspacev2/cases/' + $params.caseId + '/caselog/defaultfilters';      
      httpService.getRequest(defaultFiltersUrl, false).then(function(result){
        var defaultFilters = result.data;
        $scope.sideBarCaseLogs = caseService.getSelectedFilteredCaseLog($params.caseId, defaultFilters);
      });

      $scope.$on('caselog-message-created', function(){
        $scope.sideBarCaseLogs.invalidate();
        $scope.sideBarCaseLogs.resolve();
      });
 
      $scope.$on('conversation-message-created', function(){
        $scope.conversations.invalidate();
        $scope.conversations.resolve();
      });

      $scope.$on('contact-name-updated', function(){
        $scope.contacts.invalidate();
        $scope.contacts.resolve();
      });

      $scope.$on('case-changed', function(e, attr) {
        if (attr.command === "casetype") {
          $scope.commands.invalidate();
          $scope.commands.resolve();
        } else if (attr.command === "changedueon") {
          $scope.general.invalidate();
          $scope.general.resolve();
        }
      });

      $scope.$watch("commands[0]", function(){

        var commands = $scope.commands;

        $scope.canSendTo = _.any(commands, function(command){
          return command.rel === "sendto";
        });
        $scope.canResolve = _.any(commands, function(command){
          return command.rel === "resolve";
        });
        $scope.canClose = _.any(commands, function(command){
          return command.rel === "close";
        });
        $scope.canDelete = _.any(commands, function(command){
          return command.rel === "delete";
        });
        $scope.canAssign = _.any(commands, function(command){
          return command.rel === "assign";
        });
        $scope.canUnassign = _.any(commands, function(command){
          return command.rel === "unassign";
        });
        $scope.canRestrict = _.any(commands, function(command){
          return command.rel === "restrict";
        });
        $scope.canMarkUnread = _.any(commands, function(command){
          return command.rel === "markunread";
        });
        $scope.canMarkRead = _.any(commands, function(command){
          return command.rel === "markread";
        });
      });

      $scope.resolve = function(){

        $scope.possibleResolutions = caseService.getPossibleResolutions($params.caseId);
        $scope.$watch("possibleResolutions[0]", function(){
          if ($scope.possibleResolutions[0]) {
            $scope.resolution = $scope.possibleResolutions[0].id;
          }
        });

        $scope.commandView = "resolve"
      }

      $scope.onResolveButtonClicked = function($event){
        $event.preventDefault();

        var resolutionId = $scope.resolution;

        var callback = function(){

          var href = navigationService.caseListHrefFromCase($scope.caze);
          window.location.replace(href);
        };
        caseService.resolveCase($params.caseId, resolutionId, callback)
      }

      $scope.sendTo = function(){
        $scope.possibleSendTo = caseService.getPossibleSendTo($params.caseId);
        $scope.$watch("possibleSendTo[0]", function(){
          if ($scope.possibleSendTo[0]) {
            $scope.sendToId = $scope.possibleSendTo[0].id;
          }
        });

        $scope.commandView = "sendTo"
      }

      $scope.sendToIdChanged = function(event) {
        $scope.sendToId = event;
      }

      $scope.onSendToButtonClicked = function($event){
        $event.preventDefault();

        var sendToId = $scope.sendToId;

        var callback = function(){

          var href = navigationService.caseListHrefFromCase($scope.caze);
          window.location.replace(href);
        };
        caseService.sendCaseTo($params.caseId, sendToId, callback)
      }
/*
      $scope.close = function(){
        $scope.commandView = "close";
      }
*/
      $scope.close = function($event){
        $event.preventDefault();

        var callback = function(){

          var href = navigationService.caseListHrefFromCase($scope.caze);
          window.location.replace(href);
        };
        caseService.closeCase($params.caseId, callback)
      }

      $scope.assign = function($event){
        $event.preventDefault();

        var callback = function(){
          var href = navigationService.caseListHrefFromCase($scope.caze);
          window.location.replace(href);
        };
        caseService.assignCase($params.caseId, callback);
      }

      $scope.unassign = function($event){
        $event.preventDefault();
        
        var callback = function(){
          var href = navigationService.caseListHrefFromCase($scope.caze);
          window.location.replace(href);
        };
        caseService.unassignCase($params.caseId, callback);
      }

      $scope.markUnread = function($event){
        $event.preventDefault();

        var callback = function(){
          $scope.commands = caseService.getSelectedCommands($params.caseId);
        };
        caseService.markUnread($params.caseId, callback);
      }

      $scope.markRead = function($event){
        $event.preventDefault();
        var callback = function(){
          $scope.commands = caseService.getSelectedCommands($params.caseId);
        };
        caseService.markRead($params.caseId, callback);
      }

      $scope.deleteCase = function(){
        $scope.commandView = undefined;

        var callback = function(){

          var href = navigationService.caseListHrefFromCase($scope.caze);
          window.location.replace(href);
        }

        caseService.deleteCase($params.caseId, callback);
      }

      $scope.downloadAttachment = function(attachmentId){
        alert("Not supported - need absolute url in API.");
      }

      $scope.deleteAttachment = function(attachmentId){

        var callback = function(){
          $scope.attachments.invalidate();
          $scope.attachments.resolve();
        }
        caseService.deleteAttachment($params.caseId, attachmentId, callback);
      }

      $scope.showContact = function(contactId){
        alert("Not supported - need UX for this.");
      }
    }]);

})();
