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
(function() {
  'use strict';

  var sfSidebar = angular.module('sf.controllers.sidebar', ['sf.services.case', 'sf.services.navigation', 'sf.services.project','sf.services.http', 'sf.services.fancy-date']);

  sfSidebar.controller('SidebarCtrl', ['$scope', 'projectService', '$routeParams', 'navigationService', 'caseService', 'httpService', 'fancyDateService',
    function($scope, projectService, $params, navigationService, caseService, httpService, fancyDateService) {

      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;

      $scope.caze = caseService.getSelected($params.caseId);
      $scope.commands = caseService.getSelectedCommands($params.caseId);
      $scope.general = caseService.getSelectedGeneral($params.caseId);
      $scope.contacts = caseService.getSelectedContacts($params.caseId);
      $scope.conversations = caseService.getSelectedConversations($params.caseId);
      $scope.attachments = caseService.getSelectedAttachments($params.caseId);
      $scope.apiUrl = httpService.apiUrl + caseService.getWorkspace();
      $scope.permissions = caseService.getPermissions($params.caseId);

      $scope.general.possibleCaseTypes = caseService.getPossibleCaseTypes($params.caseId);
      $scope.caseLabel = caseService.getCaseLabel($params.caseId);
      $scope.possibleCaseLabels = caseService.getPossibleCaseLabels($params.caseId);

      $scope.allCaseLabels = [];
      $scope.activeLabels = [];
      var previousActiveLabels = [];

      $scope.$watch('general[0].dueOnShort', function (newVal) {
        if (!!newVal) {
          $scope.dueOnShort = fancyDateService.format(newVal);
        }
      });

      $scope.priority = "-1";
      $scope.priorityColor = {};
      $scope.activePriorityColor = {};

      $scope.$watch('general[0].priority', function (newVal) {
        if (!!newVal) {
          $scope.priority = newVal.id;

          if ($scope.priorityColor[newVal.id]) {
            $scope.activePriorityColor = {
              'background-color': $scope.priorityColor[newVal.id]
            };
          }
        }
      });

      $scope.$watch('general.possiblePriorities', function (newVal) {
        if (!!newVal) {
          for (var prop in newVal) {
            if (newVal.hasOwnProperty(prop) && !isNaN(prop)) {
              var item = newVal[prop];

              if (item.color !== null) {
                var intColor = parseInt(item.color, 10);

                if (intColor < 0) {
                  intColor = 0xFFFFFF + intColor + 1;
                }

                $scope.priorityColor[item.id] = '#' + intColor.toString(16);
              }
            }
          }

          if ($scope.priority && $scope.priorityColor[$scope.priority]) {
            $scope.activePriorityColor = {
              'background-color': $scope.priorityColor[$scope.priority]
            };
          }
        }
      }, true);

      $scope.$watch('caze[0].caseType', function (newVal) {
        if (!!newVal) {
          $scope.caseType = newVal.id;
        }
      });

      var sortByText = function (x, y) {
        var xS = x.text && x.text.toUpperCase() || "",
            yS = y.text && y.text.toUpperCase() || "";
        if (xS > yS) {
          return 1;
        } else if (xS < yS) {
          return -1;
        } else {
          return 0;
        }
      };

      $scope.$watch('general.possibleCaseTypes', function () {
        $scope.general.possibleCaseTypes.sort(sortByText);

        setTimeout(function () {
          $('.chosen-case-type').chosen({ search_contains: true }).trigger("chosen:updated");
        }, 0);
      }, true);

      var hasDuplicateLabel = function (labels, item) {
        return !!_.find(labels, function (j) {
          return item.id === j.id;
        });
      };

      var updateAllCaseLabels = function (labels) {
        $scope.allCaseLabels = $scope.allCaseLabels.filter(function (item) {
          return !hasDuplicateLabel(labels, item);
        }).concat(labels).sort(sortByText);

        $scope.activeLabels = $scope.activeLabels.filter(function (item) {
          return !hasDuplicateLabel(labels, item);
        }).concat(labels).filter(function (i) {
          return i.selected;
        });

        previousActiveLabels = $scope.activeLabels;

        setTimeout(function () {
          $('.chosen-case-label').chosen({ search_contains: true }).trigger("chosen:updated");
        }, 0);
      };

      var uniqueLabels = function (labels) {
        return labels.reduce(function (mem, item) {
          if (!hasDuplicateLabel(mem, item)) {
            mem.push(item);
          }

          return mem;
        }, []);
      };

      $scope.$watch('caseLabel', function (newVal) {
        var labels = uniqueLabels(newVal).map(function (i) {
          i.selected = true;
          return i;
        });

        updateAllCaseLabels(labels);
      }, true);

      $scope.$watch('possibleCaseLabels', function (newVal) {
        var labels = uniqueLabels(newVal).map(function (i) {
          i.selected = false;
          return i;
        });

        updateAllCaseLabels(labels);
      }, true);

      $scope.general.possiblePriorities = caseService.getPossiblePriorities($params.caseId);

     var defaultFiltersUrl =  caseService.getWorkspace() + '/cases/' + $params.caseId + '/caselog/defaultfilters';
      httpService.getRequest(defaultFiltersUrl, false).then(function(result){
        var defaultFilters = result.data;
        $scope.sideBarCaseLogs = caseService.getSelectedFilteredCaseLog($params.caseId, defaultFilters);
      });

      $scope.$on('caselog-message-created', function(){
        $scope.sideBarCaseLogs.invalidate();
        $scope.sideBarCaseLogs.resolve();
      });

      $scope.$on('conversation-message-created', function(){
        $scope.conversations = caseService.getSelectedConversations($params.caseId);
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

      $scope.$on('participant-removed', function(){
     	$scope.conversations = caseService.getSelectedConversations($params.caseId);
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
        caseService.sendCaseTo($params.caseId, sendToId, callback);
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
        var attachments = $scope.attachments;

        if(attachments[0].rel === 'conversation'){
          attachments[0].href  = $scope.apiUrl + '/cases/' + $params.caseId + "/" + attachments[0].href.substring(3) + 'download';
        }else if(attachments[0].rel === 'attachment'){
          attachments[0].href = $scope.apiUrl + '/cases/' + $params.caseId + '/attachments/' + attachments[0].id + '/download';
        }
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

      $scope.changeCaseLabels = function (labels) {
        var removedLabels = previousActiveLabels.filter(function (item) {
          return !hasDuplicateLabel(labels, item);
        });

        var updateLabels = function () {
          $scope.possibleCaseLabels.resolve();
          $scope.caseLabel.resolve();
        };

        if (removedLabels.length > 0) {
          removedLabels.forEach(function (label) {
            caseService.deleteCaseLabel($params.caseId, label.id).then(updateLabels);
          });
        } else {
          labels = labels.filter(function (item) {
            return !hasDuplicateLabel(previousActiveLabels, item);
          });

          labels.forEach(function (label) {
            caseService.addCaseLabel($params.caseId, label.id).then(updateLabels);
          });
        }

        previousActiveLabels = labels;
      };

      $scope.changePriorityLevel = function(priorityId){
        $scope.activePriorityColor = {
          'background-color': $scope.priorityColor[priorityId]
        };

        if (priorityId === "-1") {
          priorityId = "";
        }

        caseService.changePriorityLevel($params.caseId, priorityId);
      };

      $scope.changeCaseType = function(casetype) {
        caseService.changeCaseType($params.caseId, casetype).then(function () {
          $scope.allCaseLabels = [];
          $scope.activeLabels = [];
          $scope.possibleCaseLabels.resolve();
          $scope.caseLabel.resolve();
        });
      };

      $scope.changeDueOn = function (isoDate) {
        caseService.changeDueOn($params.caseId, isoDate);
      };

    }]);

})();
