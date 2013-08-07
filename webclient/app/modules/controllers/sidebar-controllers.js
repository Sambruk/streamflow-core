/*
 *
 * Copyright 2009-2012 Jayway Products AB
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

  var sfSidebar = angular.module('sf.controllers.sidebar', ['sf.services.case', 'sf.services.navigation', 'sf.services.project']);

  sfSidebar.controller('SidebarCtrl', ['$scope', 'projectService', '$routeParams', 'navigationService', 'caseService',
    function($scope, projectService, $params, navigationService, caseService) {

      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;

      $scope.caze = caseService.getSelected($params.projectId, $params.projectType, $params.caseId);
      $scope.commands = caseService.getSelectedCommands($params.projectId, $params.projectType, $params.caseId);
      $scope.general = caseService.getSelectedGeneral($params.projectId, $params.projectType, $params.caseId);
      $scope.contacts = caseService.getSelectedContacts($params.projectId, $params.projectType, $params.caseId);
      $scope.conversations = caseService.getSelectedConversations($params.projectId, $params.projectType, $params.caseId);
      $scope.attachments = caseService.getSelectedAttachments($params.projectId, $params.projectType, $params.caseId);
      $scope.caseLog = caseService.getSelectedCaseLog($params.projectId, $params.projectType, $params.caseId);

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

        $scope.canResolve = _.any(commands, function(command){
          return command.rel === "resolve";
        });
        $scope.canClose = _.any(commands, function(command){
          return command.rel === "close";
        });
        $scope.canDelete = _.any(commands, function(command){
          return command.rel === "delete";
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

      });

      $scope.resolve = function(){

        $scope.possibleResolutions = caseService.getPossibleResolutions($params.projectId, $params.projectType, $params.caseId);
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
          alert("Ärendet avslutades. Var vänlig ladda om sidan.");

          // TODO Find a way to invalidate the case list
          var href = navigationService.caseListHref();
          window.location.replace(href);
        };
        caseService.resolveCase($params.projectId, $params.projectType, $params.caseId, resolutionId, callback)
      }

      $scope.sendTo = function(){
        $scope.commandView = "sendTo"
      }

      $scope.close = function(){
        $scope.commandView = "close";
      }

      $scope.onCloseButtonClicked = function($event){
        $event.preventDefault();

        var callback = function(){
          alert("Ärendet stängdes. Var vänlig ladda om sidan.");

          // TODO Find a way to invalidate the case list
          var href = navigationService.caseListHref();
          window.location.replace(href);
        };
        caseService.closeCase($params.projectId, $params.projectType, $params.caseId, callback)
      }

      $scope.assign = function(){
        $scope.commandView = "todo";
      }

      $scope.markUnread = function(){
        $scope.commandView = "todo";
      }

      $scope.deleteCase = function(){
        $scope.commandView = undefined;

        var callback = function(){

          alert("Ärendet är nu borttaget. Var vänlig ladda om sidan.");

          // TODO Find a way to invalidate the case list
          var href = navigationService.caseListHref();
          window.location.replace(href);
        }

        caseService.deleteCase($params.projectId, $params.projectType, $params.caseId, callback);
      }

      $scope.downloadAttachment = function(attachmentId){
        alert("Not supported - need absolute url in API.");
      }

      $scope.deleteAttachment = function(attachmentId){

        var callback = function(){
          $scope.attachments.invalidate();
          $scope.attachments.resolve();
        }
        caseService.deleteAttachment($params.projectId, $params.projectType, $params.caseId, attachmentId, callback);
      }

      $scope.showContact = function(contactId){
        alert("Not supported - need UX for this.");
      }
    }]);

})();
