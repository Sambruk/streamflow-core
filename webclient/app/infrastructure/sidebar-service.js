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
.factory('sidebarService', function($routeParams, caseService, $q, $rootScope, $location, navigationService, tokenService, checkPermissionService){

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

  var _updateObject = function(itemToUpdate){
    itemToUpdate.invalidate();
    itemToUpdate.resolve();
  };

  var _changePriorityLevel = function(scope, priorityId) {
    if (priorityId === '-1') {
      priorityId = '';
    }

    caseService.changePriorityLevel($routeParams.caseId, priorityId).then(function () {
      if (scope.priorityColor[priorityId]) {
        scope.activePriorityColor = {
          'background-color': scope.priorityColor[priorityId]
        };
      }else{
        scope.activePriorityColor = {
          'background-color': '#ffffff'
        };
      }
    });
  };

  var _changeCaseType = function(scope, casetype) {
    caseService.changeCaseType($routeParams.caseId, casetype).then(function() {
      if(!scope.possibleForms){
        scope.possibleForms = caseService.getSelectedPossibleForms($routeParams.caseId);
      }else{
        _updateObject(scope.possibleForms);
      }

      if(!scope.possiblePriorities){
        scope.possiblePriorities = caseService.getPossiblePriorities($routeParams.caseId);
      }else{
        _updateObject(scope.possiblePriorities);
      }

      scope.general.invalidate();
      scope.general.resolve().then(function(){
        _priority(scope);
      });

      if(scope.possibleForms.length === 0){
        // Check if the current route contains formdraft to redirect to "case main page"
        var checkRoute = new RegExp('formdrafts').test($location.path());
        if(checkRoute === true){
          var href = navigationService.caseHrefSimple($routeParams.caseId);
          window.location.replace(href);
        }
      }
      _updateObject(scope.possibleResolutions);

      $rootScope.$broadcast('case-type-changed');
      _updateCaseLabels(scope);
      _updateToolbar(scope);
    });
  };

  var _updateCaseLabels = function(scope) {
    if (!scope.caseLabel) {
      scope.caseLabel = caseService.getCaseLabel($routeParams.caseId);

    } else {
      _updateObject(scope.caseLabel);
    }

    if (!scope.possibleCaseLabels) {
      scope.possibleCaseLabels = caseService.getPossibleCaseLabels($routeParams.caseId);
    } else {
      _updateObject(scope.possibleCaseLabels);
    }

    $q.all([
      scope.caseLabel.promise,
      scope.possibleCaseLabels.promise
    ]).then(function (results) {
      checkPermissionService.checkPermissions(scope, scope.caseLabel.commands, ['addlabel'], ['canAddLabel']);
      scope.activeLabels = results[0].map(function (i) {
        i.selected = true;
        return i;
      });

      scope.allCaseLabels = scope.activeLabels.concat(results[1].map(function (i) {
        i.selected = false;
        return i;
      })).sort(sortByText);

      scope.previousActiveLabels = scope.activeLabels;
      setTimeout(function () {
        jQuery('.chosen-case-label').chosen({ 'search_contains': true }).trigger('chosen:updated');
        $('.chosen-container').css({visibility: 'visible'});
      }, 0);
    });
  };

  var _updateToolbar = function(scope) {
    if (scope.commands) {
      _updateObject(scope.commands);
    } else {
      scope.commands = caseService.getSelectedCommands($routeParams.caseId);
    }

    scope.commands.promise.then(function (response) {
      var commandMap = {
        'sendto': 'canSendTo',
        'resolve': 'canResolve',
        'close': 'canClose',
        'reopen': 'canReopen',
        'delete': 'canDelete',
        'assign': 'canAssign',
        'unassign': 'canUnassign',
        'restrict': 'canRestrict',
        'requirecasetype': 'caseRequireCaseType',
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
          scope[commandMap[commandName]] = hasCommand(commandName);
        }
      }
    });
  };

  var _sendTo = function(scope) {
    scope.possibleSendTo.promise.then(function (response) {
      scope.sendToRecipients = response;
      if (response[0]) {
        scope.show = true;
        scope.sendToId = response[0] && response[0].id;
      }
      scope.commandView = 'sendTo';
    });
  };

  var _onSendToButtonClicked = function(scope) {
    var sendToId = scope.sendToId;

    caseService.sendCaseTo($routeParams.caseId, sendToId, function(){
      var href = navigationService.caseListHrefFromCase(scope.caze);
      var projectId = scope.caze[0].owner;
      var projectType = scope.caze[0].listType;

      scope.show = false;
      scope.caze.invalidate();
      scope.caze.resolve().then(function(response){
        $rootScope.$broadcast('case-changed');
        $rootScope.$broadcast('case-owner-changed');

        $rootScope.$broadcast('breadcrumb-updated',
          [{projectId: projectId},
          {projectType: projectType}]);

        window.location.replace(href);
      });
    });
  };

  var _unrestrict = function (scope) {
    caseService.unrestrictCase($routeParams.caseId).then(function () {
      scope.permissions.invalidate();
      scope.permissions.resolve().then(function () {
        $rootScope.$broadcast('case-unrestricted');
        _updateToolbar(scope);
      });
    });
  };

  var _restrict = function (scope) {
    caseService.restrictCase($routeParams.caseId).then(function () {
      scope.permissions.invalidate();
      scope.permissions.resolve().then(function () {
        $rootScope.$broadcast('case-restricted');
        _updateToolbar(scope);
      });
    });
  };
  // End Restrict / Unrestrict

  // Mark Read / Unread
  var _markReadUnread = function (scope, read) {
    var markFunction = read ? caseService.markRead : caseService.markUnread;

    markFunction($routeParams.caseId).then(function () {
      scope.commands.resolve().then(function () {
        _updateToolbar(scope);
      });
    });
  };
  // End Mark Read / Unread

  // Close
  var _close = function (scope) {
    if(scope.caseType === null){
      scope.commandView = 'requiredCaseType';
    }else{
      caseService.closeCase($routeParams.caseId).then(function () {
        $rootScope.$broadcast('case-closed');
        var href = navigationService.caseListHrefFromCase(scope.caze);
        window.location.replace(href);
      });
    }
  };
  // End Close

  // Delete
  var _deleteCase = function (scope) {
    caseService.deleteCase($routeParams.caseId).then(function () {
      $rootScope.$broadcast('case-deleted');
      var href = navigationService.caseListHrefFromCase(scope.caze);
      window.location.replace(href);
    });
  };
  // End Delete

  // Assign / Unassign
  var _assign = function (scope) {
    caseService.assignCase($routeParams.caseId).then(function () {
      $rootScope.$broadcast('case-assigned');
      _updateToolbar(scope);

      var href = navigationService.caseListHrefFromCase(scope.caze);
      window.location.replace(href);
    });
  };

  var _unassign = function (scope) {
    caseService.unassignCase($routeParams.caseId).then(function () {
      $rootScope.$broadcast('case-unassigned');

      _updateToolbar(scope);
      var href = navigationService.caseListHrefFromCase(scope.caze);
      window.location.replace(href);
    });
  };

  var _reopen = function(scope){
    caseService.reopenCase($routeParams.caseId).then(function(){
      _updateToolbar(scope);
    });
  };

  var _downloadAttachment = function (scope, attachment) {
    // Hack to replace dummy user and pass with authentication from token.
    // This is normally sent by httpF headers in ajax but not possible here.
    var apiUrl = scope.apiUrl.replace(/https:\/\/(.*)@/, function () {
      var userPass = window.atob(tokenService.getToken());
      return 'https://' + userPass + '@' ;
    });

    var url = apiUrl + '/cases/' + $routeParams.caseId + '/attachments/' + attachment.href + 'download';
    window.location.replace(url);
  };

  var _deleteAttachment = function(scope, attachment){
    caseService.deleteAttachment($routeParams.caseId, attachment.id).then(function () {
       _updateObject(scope.attachments);
    });
  };
  // End Attachments

  var _changeCaseLabels = function (scope, labels) {
    var removedLabels = scope.previousActiveLabels.filter(function (item) {
      return !_.find(labels, function (j) {
        return item.id === j.id;
      });
    });

    if (removedLabels.length > 0) {
      var removePromises = removedLabels.map(function (label) {
        return caseService.deleteCaseLabel($routeParams.caseId, label.id);
      });

      $q.all(removePromises).then(function(){
        _updateCaseLabels(scope);
      });
    } else {
      var addPromises = labels.map(function (label) {
        return caseService.addCaseLabel($routeParams.caseId, label.id);
      });

      $q.all(addPromises).then(function(){
        _updateCaseLabels(scope);
      });
    }

    scope.previousActiveLabels = labels;
  };

  var _changeDueOn = function (scope, date) {
    // Must be in the future and time must be set (but is not used).
    var isoString = (new Date(date + 'T23:59:59.000Z')).toISOString();
    caseService.changeDueOn($routeParams.caseId, isoString).then(function () {
      scope.general.invalidate();
      scope.general.resolve().then(function (result) {
        scope.dueOnShortStartValue = result[0].dueOnShort;
      });
    });
  };

  var _onResolveButtonClicked = function(scope){
    var resolutionId = scope.resolution;

    caseService.resolveCase($routeParams.caseId, resolutionId, function(){
      $rootScope.$broadcast('case-resolved');
      var href = navigationService.caseListHrefFromCase(scope.caze);
      window.location.replace(href);
    });
  };

  var _resolveCase = function(scope) {
    scope.possibleResolutions.promise.then(function (response) {
      scope.resolution = response[0].id;
      scope.commandView = 'resolve';
    });
  };

  var _caseType = function(scope){
    $q.all([
      scope.possibleCaseTypes.promise,
      scope.caze.promise,
    ]).then(function (results) {
      scope.caseType = results[1][0].caseType && results[1][0].caseType.id;
      scope.possibleCaseTypes = results[0].sort(sortByText);

      setTimeout(function () {
        jQuery('.chosen-case-type').chosen({ 'search_contains': true }).trigger('chosen:updated');
        // $('#type-select').css({visibility: 'visible'});
        // $('#type_select_chosen').css({visibility: 'visible'});
      }, 0);
    });
  };

  var _priority = function(scope){
    $q.all([
      scope.general.promise,
      scope.possiblePriorities.promise
    ]).then(function (responses) {
      responses[1].forEach(function (item) {
        if (item.color !== null) {
          var intColor = parseInt(item.color, 10);

          if (intColor < 0) {
            intColor = 0xFFFFFFFF + intColor + 1;
          }
          scope.priorityColor[item.id] = '#' + intColor.toString(16).slice(2,8);
        }else{
          scope.priorityColor[item.id] = '#ffffff';
        }
      });

      scope.priority = responses[0][0].priority && responses[0][0].priority.id;
      if (scope.priorityColor[scope.priority]) {
        scope.activePriorityColor = {
          'background-color': scope.priorityColor[scope.priority]
        };
      }
    });
  };

  return {
    changePriorityLevel: _changePriorityLevel,
    changeCaseType: _changeCaseType,
    updateCaseLabels: _updateCaseLabels,
    updateToolbar: _updateToolbar,
    sendTo: _sendTo,
    onSendToButtonClicked: _onSendToButtonClicked,
    unrestrict: _unrestrict,
    restrict: _restrict,
    markReadUnread: _markReadUnread,
    close: _close,
    reopen: _reopen,
    deleteCase: _deleteCase,
    assign: _assign,
    unassign: _unassign,
    downloadAttachment: _downloadAttachment,
    deleteAttachment: _deleteAttachment,
    changeCaseLabels: _changeCaseLabels,
    changeDueOn: _changeDueOn,
    onResolveButtonClicked: _onResolveButtonClicked,
    resolveCase: _resolveCase,
    caseType: _caseType,
    priority: _priority
  };
});
