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

  sfSidebar.controller('SidebarCtrl', ['$scope', 'projectService', '$routeParams', 'navigationService', 'caseService', 'httpService', '$q',
    function($scope, projectService, $params, navigationService, caseService, httpService, $q) {

      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;

      $scope.caze = caseService.getSelected($params.caseId);
      $scope.general = caseService.getSelectedGeneral($params.caseId);
      $scope.contacts = caseService.getSelectedContacts($params.caseId);
      $scope.conversations = caseService.getSelectedConversations($params.caseId);
      $scope.attachments = caseService.getSelectedAttachments($params.caseId);
      $scope.apiUrl = httpService.apiUrl + caseService.getWorkspace();
      $scope.possiblePriorities = caseService.getPossiblePriorities($params.caseId);
      $scope.possibleCaseTypes = caseService.getPossibleCaseTypes($params.caseId);

      $scope.showSpinner = {
        caseType: true,
        caseLabels: true,
        caseDueOn: true,
        casePriority: true,
        caseToolbar: false,
        casePermissions: true
      };
      
      var sortByText = function (x, y) {
        var xS = x.text && x.text.toUpperCase() || '',
            yS = y.text && y.text.toUpperCase() || '';
        if (xS > yS) {
          return 1;
        } else if (xS < yS) {
          return -1;
        } else {
          return 0;
        }
      };
      
      // Resolve
      $scope.showToolbar = function () {
        return !!$scope.possibleResolutions.length;
      };
      
      $scope.resolve = function() {
        $scope.possibleResolutions = caseService.getPossibleResolutions($params.caseId);
        $scope.possibleResolutions.promise.then(function (response) {
          $scope.resolution = response[0].id;
          $scope.commandView = 'resolve';
        });
      };

      $scope.onResolveButtonClicked = function(){
        var resolutionId = $scope.resolution;

        var callback = function () {
          var href = navigationService.caseListHrefFromCase($scope.caze);
          window.location.replace(href);
        };

        caseService.resolveCase($params.caseId, resolutionId, callback);
      };

      $scope.onCancelResolveButtonClicked = function () {
        $scope.commandView = '';
      };
      // End Resolve
      
      // Due on
      $scope.general.promise.then(function (result) {
        $scope.dueOnShortStartValue = result[0].dueOnShort;
        $scope.showSpinner.caseDueOn = false;
      });
      
      $scope.changeDueOn = function (date) {
        $scope.showSpinner.caseDueOn = true;
        
        // Must be in the future and time must be set (but is not used).
        var isoString = (new Date(date + 'T23:59:59.000Z')).toISOString();
        caseService.changeDueOn($params.caseId, isoString).then(function () {
          $scope.general.invalidate();
          $scope.general.resolve().then(function (result) {
            $scope.dueOnShortStartValue = result[0].dueOnShort;
            $scope.showSpinner.caseDueOn = false;
          });
        });
      };
      // End due on

      // Priority
      $scope.priority = '-1';
      $scope.priorityColor = {};
      $scope.activePriorityColor = {};
      
      $q.all([
        $scope.general.promise,
        $scope.possiblePriorities.promise
      ]).then(function (responses) {
        responses[1].forEach(function (item) {
          if (item.color !== null) {
            var intColor = parseInt(item.color, 10);

            if (intColor < 0) {
              intColor = 0xFFFFFF + intColor + 1;
            }

            $scope.priorityColor[item.id] = '#' + intColor.toString(16);
          }
        });
        
        $scope.priority = responses[0][0].priority && responses[0][0].priority.id;

        if ($scope.priorityColor[$scope.priority]) {
          $scope.activePriorityColor = {
            'background-color': $scope.priorityColor[$scope.priority]
          };
        }
        
        $scope.showSpinner.casePriority = false;
      });
      
      $scope.changePriorityLevel = function(priorityId) {
        $scope.showSpinner.casePriority = true;

        if (priorityId === '-1') {
          priorityId = '';
        }

        caseService.changePriorityLevel($params.caseId, priorityId).then(function () {
          if ($scope.priorityColor[priorityId]) {
            $scope.activePriorityColor = {
              'background-color': $scope.priorityColor[priorityId]
            };
          }
          
          $scope.showSpinner.casePriority = false;
        });
      };
      // Priority end
      
      // Case type
      $q.all([
        $scope.possibleCaseTypes.promise,
        $scope.caze.promise
      ]).then(function (results) {
        $scope.caseType = results[1][0].caseType && results[1][0].caseType.id;
        $scope.possibleCaseTypes = results[0].sort(sortByText);
        $scope.showSpinner.caseType = false;
        setTimeout(function () {
          jQuery('.chosen-case-type').chosen({ 'search_contains': true }).trigger('chosen:updated');
        }, 0);
      });
      
      $scope.changeCaseType = function(casetype) {
        $scope.showSpinner.caseToolbar = true;
        caseService.changeCaseType($params.caseId, casetype).then(function () {
          $scope.showSpinner.caseToolbar = false;
          updateCaseLabels();
          updateToolbar();
        });
      };
      // End case type

      // Case labels
      $scope.allCaseLabels = [];
      $scope.activeLabels = [];
      var previousActiveLabels = [];
      
      var updateCaseLabels = function () {
        $scope.showSpinner.caseLabels = true;
        
        if ($scope.caseLabel) {
          $scope.caseLabel.invalidate();
          $scope.caseLabel.resolve();
        } else {
          $scope.caseLabel = caseService.getCaseLabel($params.caseId);
        }
        
        if ($scope.possibleCaseLabels) {
          $scope.possibleCaseLabels.invalidate();
          $scope.possibleCaseLabels.resolve();
        } else {
          $scope.possibleCaseLabels = caseService.getPossibleCaseLabels($params.caseId);
        }
        
        $q.all([
          $scope.caseLabel.promise,
          $scope.possibleCaseLabels.promise
        ]).then(function (results) {
          $scope.activeLabels = results[0].map(function (i) {
            i.selected = true;
            return i;
          });
          
          $scope.allCaseLabels = $scope.activeLabels.concat(results[1].map(function (i) {
            i.selected = false;
            return i;
          })).sort(sortByText);
          
          previousActiveLabels = $scope.activeLabels;
          $scope.showSpinner.caseLabels = false;
          
          setTimeout(function () {
            jQuery('.chosen-case-label').chosen({ search_contains: true }).trigger("chosen:updated");
          }, 0);
        });
      };
      
      updateCaseLabels();
      
      $scope.changeCaseLabels = function (labels) {
        $scope.showSpinner.caseLabels = true;
        var removedLabels = previousActiveLabels.filter(function (item) {
          return !_.find(labels, function (j) {
              return item.id === j.id;
          });
        });
        
        if (removedLabels.length > 0) {
          var removePromises = removedLabels.map(function (label) {
            return caseService.deleteCaseLabel($params.caseId, label.id);
          });
          
          $q.all(removePromises).then(updateCaseLabels);
        } else {
          var addPromises = labels.map(function (label) {
            return caseService.addCaseLabel($params.caseId, label.id);
          });
          
          $q.all(addPromises).then(updateCaseLabels);
        }
      
        previousActiveLabels = labels;
      };
      // End case labels
      
      // Commands (toolbar)
      var updateToolbar = function () {
        $scope.showSpinner.caseToolbar = true;
        
        if ($scope.commands) {
          $scope.commands.invalidate();
          $scope.commands.resolve();
        } else {
          $scope.commands = caseService.getSelectedCommands($params.caseId);
        }
        
        $scope.commands.promise.then(function (response) {  
          var commandMap = {
            'sendto': 'canSendTo',
            'resolve': 'canResolve',
            'close': 'canClose',
            'delete': 'canDelete',
            'assign': 'canAssign',
            'unassign': 'canUnassign',
            'restrict': 'canRestrict',
            'unrestrict': 'canUnrestrict',
            'markunread': 'canMarkUnread',
            'markread': 'canMarkRead',
            'formonclose': 'formOnClose'
          };
          
          var hasCommand = function (commandName) {
            return !!_.find(response, function (command) {
              return command.rel === commandName;
            });
          };
          
          for (var commandName in commandMap) {
            if (commandMap.hasOwnProperty(commandName)) {
              $scope[commandMap[commandName]] = hasCommand(commandName);
            }
          }
          
          $scope.showSpinner.caseToolbar = false;
        });
      };
      updateToolbar();
      // End commands (toolbar)
      
      // Restrict / Unrestrict
      $scope.permissions = caseService.getPermissions($params.caseId);
      $scope.permissions.promise.then(function () {
        $scope.showSpinner.casePermissions = false;
      });
      
      $scope.unrestrict = function () {
        $scope.showSpinner.caseToolbar = true;
        $scope.showSpinner.casePermissions = true;
        caseService.unrestrictCase($params.caseId).then(function () {
          $scope.permissions.invalidate();
          $scope.permissions.resolve().then(function () {
            $scope.showSpinner.casePermissions = false;
            updateToolbar();
          });
        });
      };

      $scope.restrict = function () {
        $scope.showSpinner.caseToolbar = true;
        $scope.showSpinner.casePermissions = true;
        caseService.restrictCase($params.caseId).then(function () {
          $scope.permissions.invalidate();
          $scope.permissions.resolve().then(function () {
            $scope.showSpinner.casePermissions = false;
            updateToolbar();
          });
        });
      };
      // End Restrict / Unrestrict
      
      // Mark Read / Unread
      $scope.markReadUnread = function (read) {
        var markFunction = read ? caseService.markRead : caseService.markUnread;
        $scope.showSpinner.caseToolbar = true;
        
        markFunction($params.caseId).then(function () {
          $scope.commands.resolve().then(function () {
            updateToolbar();
          });
        });
      };
      // End Mark Read / Unread
      
      // Close
      $scope.close = function () {
        caseService.closeCase($params.caseId).then(function () {
          var href = navigationService.caseListHrefFromCase($scope.caze);
          window.location.replace(href);
        });
      };
      // End Close
      
      // Delete
      $scope.deleteCase = function () {
        caseService.deleteCase($params.caseId).then(function () {
          var href = navigationService.caseListHrefFromCase($scope.caze);
          window.location.replace(href);
        });
      };
      // End Delete
      
      // Send to (BROKEN!)
      $scope.sendTo = function () {
        $scope.possibleSendTo = caseService.getPossibleSendTo($params.caseId);
        $scope.possibleSendTo.promise.then(function (response) {
          if (response[0]) {
            $scope.sendToId = response[0] && response[0].id;
          }
          
          $scope.commandView = 'sendTo';
        });
      };

      $scope.sendToIdChanged = function () {
        $scope.sendToId = event;
      };

      $scope.onSendToButtonClicked = function () {
        var sendToId = $scope.sendToId;

        var callback = function(){

          var href = navigationService.caseListHrefFromCase($scope.caze);
          window.location.replace(href);
        };
        caseService.sendCaseTo($params.caseId, sendToId, callback);
      };
      // End Send to

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
        if (attr.command === 'casetype') {
          $scope.commands.invalidate();
          $scope.commands.resolve();
        } else if (attr.command === 'changedueon') {
          $scope.general.invalidate();
          $scope.general.resolve();
        }
      });

    $scope.$on('casedescription-changed', function(){
        $scope.caze = caseService.getSelected($params.caseId);
    });

      $scope.$on('participant-removed', function(){
     	$scope.conversations = caseService.getSelectedConversations($params.caseId);
      });

      $scope.assign = function($event){
        $event.preventDefault();

        var callback = function(){
          var href = navigationService.caseListHrefFromCase($scope.caze);
          window.location.replace(href);
        };
        caseService.assignCase($params.caseId, callback);
      };

      $scope.unassign = function($event){
        $event.preventDefault();

        var callback = function(){
          var href = navigationService.caseListHrefFromCase($scope.caze);
          window.location.replace(href);
        };
        caseService.unassignCase($params.caseId, callback);
      };

      $scope.downloadAttachment = function(attachmentId){
        var attachments = $scope.attachments;

        if(attachments[0].rel === 'conversation'){
          attachments[0].href  = $scope.apiUrl + '/cases/' + $params.caseId + '/' + attachments[0].href.substring(3) + 'download';
        }else if(attachments[0].rel === 'attachment'){
          attachments[0].href = $scope.apiUrl + '/cases/' + $params.caseId + '/attachments/' + attachments[0].id + '/download';
        }
      };

      $scope.deleteAttachment = function(attachmentId){
        var callback = function(){
          $scope.attachments.invalidate();
          $scope.attachments.resolve();
        };
        caseService.deleteAttachment($params.caseId, attachmentId, callback);
      };

      $scope.showContact = function(contactId){
        alert("Not supported - need UX for this.");
      };
      
    }]);
})();
