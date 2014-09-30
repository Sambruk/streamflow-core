'use strict';

angular.module('sf')
.factory('sidebarService', function($routeParams, caseService, $q, $rootScope, navigationService, tokenService){

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

  var _changePriorityLevel = function(scope, priorityId) {
    scope.showSpinner.casePriority = true;

    if (priorityId === '-1') {
      priorityId = '';
    }

    caseService.changePriorityLevel($routeParams.caseId, priorityId).then(function () {
      if (scope.priorityColor[priorityId]) {
        scope.activePriorityColor = {
          'background-color': scope.priorityColor[priorityId]
        };
      }

      scope.showSpinner.casePriority = false;
    });
  };

  var _changeCaseType = function(scope, casetype) {
    scope.showSpinner.caseToolbar = true;
    scope.showSpinner.casePossibleForms = true;
    caseService.changeCaseType($routeParams.caseId, casetype).then(function() {
      scope.showSpinner.caseToolbar = false;
      if(!scope.possibleForms){
        scope.possibleForms = caseService.getSelectedPossibleForms($routeParams.caseId);
      }else{
        scope.possibleForms.invalidate();
        scope.possibleForms.resolve();
      }
      scope.showSpinner.casePossibleForms = false;
      _updateCaseLabels(scope);
      _updateToolbar(scope);
    });
  };

  var _updateCaseLabels = function(scope) {
    //console.log(scope);
    scope.showSpinner.caseLabels = true;
    
    if (!scope.caseLabel) {
      scope.caseLabel = caseService.getCaseLabel($routeParams.caseId);
    } else {
      scope.caseLabel.invalidate();
      scope.caseLabel.resolve();
    }
    
    if (!scope.possibleCaseLabels) {
      scope.possibleCaseLabels = caseService.getPossibleCaseLabels($routeParams.caseId);
    } else {
      scope.possibleCaseLabels.invalidate();
      scope.possibleCaseLabels.resolve();
    }
    
    $q.all([
      scope.caseLabel.promise,
      scope.possibleCaseLabels.promise
    ]).then(function (results) {
      scope.activeLabels = results[0].map(function (i) {
        i.selected = true;
        return i;
      });
      
      scope.allCaseLabels = scope.activeLabels.concat(results[1].map(function (i) {
        i.selected = false;
        return i;
      })).sort(sortByText);
      
      scope.previousActiveLabels = scope.activeLabels;
      scope.showSpinner.caseLabels = false;
      
      setTimeout(function () {
        jQuery('.chosen-case-label').chosen({ 'search_contains': true }).trigger('chosen:updated');
      }, 0);
    });
  };

  var _updateToolbar = function(scope) {
    scope.showSpinner.caseToolbar = true;
    
    if (scope.commands) {
      scope.commands.invalidate();
      scope.commands.resolve();
    } else {
      scope.commands = caseService.getSelectedCommands($routeParams.caseId);
    }
    
    scope.commands.promise.then(function (response) {  
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
          scope[commandMap[commandName]] = hasCommand(commandName);
        }
      }

      scope.showSpinner.caseToolbar = false;
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
    scope.showSpinner.caseGeneralInfo= true;

    caseService.sendCaseTo($routeParams.caseId, sendToId, function(){
      scope.show = false;
      scope.caze.invalidate();
      scope.caze.resolve().then(function(response){
        // Is this needed?
        //$rootScope.$broadcast('case-changed');
        $rootScope.$broadcast('case-changed-update-context-and-caselist');
        $rootScope.$broadcast('breadcrumb-updated', 
          [{projectId: scope.caze[0].owner}, 
          {projectType: scope.caze[0].listType},
          {caseId: scope.caze[0].caseId}]);

        scope.showSpinner.caseGeneralInfo = false;
      });
    });
  };

  var _unrestrict = function (scope) {
    scope.showSpinner.caseToolbar = true;
    scope.showSpinner.casePermissions = true;
    caseService.unrestrictCase($routeParams.caseId).then(function () {
      scope.permissions.invalidate();
      scope.permissions.resolve().then(function () {
        scope.showSpinner.casePermissions = false;
        _updateToolbar(scope);
      });
    });
  };

  var _restrict = function (scope) {
    scope.showSpinner.caseToolbar = true;
    scope.showSpinner.casePermissions = true;
    caseService.restrictCase($routeParams.caseId).then(function () {
      scope.permissions.invalidate();
      scope.permissions.resolve().then(function () {
        scope.showSpinner.casePermissions = false;
        _updateToolbar(scope);
      });
    });
  };
  // End Restrict / Unrestrict
  
  // Mark Read / Unread
  var _markReadUnread = function (scope, read) {
    var markFunction = read ? caseService.markRead : caseService.markUnread;
    scope.showSpinner.caseToolbar = true;
    
    markFunction($routeParams.caseId).then(function () {
      scope.commands.resolve().then(function () {
        _updateToolbar(scope);
      });
    });
  };
  // End Mark Read / Unread
  
  // Close
  var _close = function (scope) {
    caseService.closeCase($routeParams.caseId).then(function () {
      var href = navigationService.caseListHrefFromCase(scope.caze);
      window.location.replace(href);
    });
  };
  // End Close
  
  // Delete
  var _deleteCase = function (scope) {
    caseService.deleteCase($routeParams.caseId).then(function () {
      var href = navigationService.caseListHrefFromCase(scope.caze);
      window.location.replace(href);
    });
  };
  // End Delete
  
  // Assign / Unassign
  var _assign = function (scope) {
    caseService.assignCase($routeParams.caseId).then(function () {
      var href = navigationService.caseListHrefFromCase(scope.caze);
      window.location.replace(href);
    });
  };

  var _unassign = function (scope) {
    caseService.unassignCase($routeParams.caseId).then(function () {
      var href = navigationService.caseListHrefFromCase(scope.caze);
      window.location.replace(href);
    });
  };

  var _downloadAttachment = function (scope, attachment) {
    // Hack to replace dummy user and pass with authentication from token.
    // This is normally sent by http headers in ajax but not possible here.
    var apiUrl = scope.apiUrl.replace(/https:\/\/(.*)@/, function () {
      var userPass = window.atob(tokenService.getToken());
      return 'https://' + userPass + '@' ;
    });
    
    var url = apiUrl + '/cases/' + $routeParams.caseId + '/attachments/' + attachment.href + 'download';
    window.location.replace(url);
  };

  var _deleteAttachment = function(scope, attachmentId){
    caseService.deleteAttachment($routeParams.caseId, attachmentId).then(function () {
      scope.showSpinner.caseAttachment = true;
      scope.attachments.invalidate();
      scope.attachments.resolve().then(function () {
        scope.showSpinner.caseAttachment = false;
      });
    });
  };
  // End Attachments

  var _changeCaseLabels = function (scope, labels) {
    scope.showSpinner.caseLabels = true;
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
    scope.showSpinner.caseDueOn = true;
    
    // Must be in the future and time must be set (but is not used).
    var isoString = (new Date(date + 'T23:59:59.000Z')).toISOString();
    caseService.changeDueOn($routeParams.caseId, isoString).then(function () {
      scope.general.invalidate();
      scope.general.resolve().then(function (result) {
        scope.dueOnShortStartValue = result[0].dueOnShort;
        scope.showSpinner.caseDueOn = false;
      });
    });
  };

  var _onResolveButtonClicked = function(scope){
    var resolutionId = scope.resolution;

    var callback = function () {
      var href = navigationService.caseListHrefFromCase(scope.caze);
      window.location.replace(href);
    };

    caseService.resolveCase($routeParams.caseId, resolutionId, callback);
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
      scope.caze.promise
    ]).then(function (results) {
      scope.caseType = results[1][0].caseType && results[1][0].caseType.id;
      scope.possibleCaseTypes = results[0].sort(sortByText);
      scope.showSpinner.caseType = false;
      setTimeout(function () {
        jQuery('.chosen-case-type').chosen({ 'search_contains': true }).trigger('chosen:updated');
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
            intColor = 0xFFFFFF + intColor + 1;
          }

          scope.priorityColor[item.id] = '#' + intColor.toString(16);
        }
      });
      
      scope.priority = responses[0][0].priority && responses[0][0].priority.id;

      if (scope.priorityColor[scope.priority]) {
        scope.activePriorityColor = {
          'background-color': scope.priorityColor[scope.priority]
        };
      }
      
      scope.showSpinner.casePriority = false;
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