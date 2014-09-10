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
.directive('sidebar', function($location, $rootScope, $routeParams, projectService, caseService, httpService, navigationService, $q, tokenService){
  return {
    restrict: 'E',
    templateUrl: 'components/sidebar/sidebar.html',
    scope: {
      sidebardata: '='
    },
    link: function(scope){
      scope.projectId = $routeParams.projectId;
      scope.projectType = $routeParams.projectType;

      scope.caze = caseService.getSelected($routeParams.caseId);
      scope.general = caseService.getSelectedGeneral($routeParams.caseId);
      scope.contacts = caseService.getSelectedContacts($routeParams.caseId);
      scope.conversations = caseService.getSelectedConversations($routeParams.caseId);
      scope.attachments = caseService.getSelectedAttachments($routeParams.caseId);
      scope.apiUrl = httpService.apiUrl + caseService.getWorkspace();
      scope.possiblePriorities = caseService.getPossiblePriorities($routeParams.caseId);
      scope.possibleCaseTypes = caseService.getPossibleCaseTypes($routeParams.caseId);
      scope.possibleResolutions = caseService.getPossibleResolutions($routeParams.caseId);
      scope.possibleForms = caseService.getSelectedPossibleForms($routeParams.caseId);
      scope.submittedFormList = caseService.getSubmittedFormList($routeParams.caseId);
      scope.notes = caseService.getSelectedNote($routeParams.caseId);

      if($routeParams.formId && $routeParams.caseId){
        var formId = scope.formdata.formId;
        scope.submittedForms = caseService.getSubmittedForms($routeParams.caseId, $routeParams.formId);
      }

      scope.showSpinner = {
        caseType: true,
        caseLabels: true,
        caseDueOn: true,
        casePriority: true,
        caseToolbar: false,
        casePermissions: true,
        caseAttachment: true
      };

      scope.contact = {
        name: '',
        contactId: '',
        note: '',
        addresses: [{ address: '', zipCode: '', city: '', region: '', country: '', contactType: 'HOME' }],
        emailAddresses: [{ emailAddress: '', contactType: 'HOME'}],
        phoneNumbers: [{ phoneNumber: '', contactType: 'HOME' }],
        contactPreference: 'email'
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
      scope.showToolbar = function () {
        return !!scope.possibleResolutions.length;
      };
      
      scope.resolve = function() {
        scope.possibleResolutions.promise.then(function (response) {
          scope.resolution = response[0].id;
          scope.commandView = 'resolve';
        });
      };

      scope.onResolveButtonClicked = function(){
        var resolutionId = scope.resolution;

        var callback = function () {
          var href = navigationService.caseListHrefFromCase(scope.caze);
          window.location.replace(href);
        };

        caseService.resolveCase($routeParams.caseId, resolutionId, callback);
      };

      scope.onCancelResolveButtonClicked = function () {
        scope.commandView = '';
      };
      // End Resolve
      
      // Due on
      scope.general.promise.then(function (result) {
        scope.dueOnShortStartValue = result[0].dueOnShort;
        scope.showSpinner.caseDueOn = false;
      });
      
      scope.changeDueOn = function (date) {
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
      // End due on

      // Priority
      scope.priority = '-1';
      scope.priorityColor = {};
      scope.activePriorityColor = {};
      
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
      
      scope.changePriorityLevel = function(priorityId) {
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
      // Priority end
      
      // Case type
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
      
      scope.changeCaseType = function(casetype) {
        scope.showSpinner.caseToolbar = true;
        caseService.changeCaseType($routeParams.caseId, casetype).then(function () {
          scope.showSpinner.caseToolbar = false;
          updateCaseLabels();
          updateToolbar();
        });
      };
      // End case type

      // Case labels
      scope.allCaseLabels = [];
      scope.activeLabels = [];
      var previousActiveLabels = [];
      
      var updateCaseLabels = function () {
        scope.showSpinner.caseLabels = true;
        
        if (scope.caseLabel) {
          scope.caseLabel.invalidate();
          scope.caseLabel.resolve();
        } else {
          scope.caseLabel = caseService.getCaseLabel($routeParams.caseId);
        }
        
        if (scope.possibleCaseLabels) {
          scope.possibleCaseLabels.invalidate();
          scope.possibleCaseLabels.resolve();
        } else {
          scope.possibleCaseLabels = caseService.getPossibleCaseLabels($routeParams.caseId);
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
          
          previousActiveLabels = scope.activeLabels;
          scope.showSpinner.caseLabels = false;
          
          setTimeout(function () {
            jQuery('.chosen-case-label').chosen({ 'search_contains': true }).trigger('chosen:updated');
          }, 0);
        });
      };
      
      updateCaseLabels();
      
      scope.changeCaseLabels = function (labels) {
        scope.showSpinner.caseLabels = true;
        var removedLabels = previousActiveLabels.filter(function (item) {
          return !_.find(labels, function (j) {
              return item.id === j.id;
          });
        });
        
        if (removedLabels.length > 0) {
          var removePromises = removedLabels.map(function (label) {
            return caseService.deleteCaseLabel($routeParams.caseId, label.id);
          });
          
          $q.all(removePromises).then(updateCaseLabels);
        } else {
          var addPromises = labels.map(function (label) {
            return caseService.addCaseLabel($routeParams.caseId, label.id);
          });
          
          $q.all(addPromises).then(updateCaseLabels);
        }
      
        previousActiveLabels = labels;
      };
      // End case labels
      
      // Commands (toolbar)
      var updateToolbar = function () {
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
      updateToolbar();
      // End commands (toolbar)
      
      // Restrict / Unrestrict
      scope.permissions = caseService.getPermissions($routeParams.caseId);
      scope.permissions.promise.then(function () {
        scope.showSpinner.casePermissions = false;
      });
      
      scope.unrestrict = function () {
        scope.showSpinner.caseToolbar = true;
        scope.showSpinner.casePermissions = true;
        caseService.unrestrictCase($routeParams.caseId).then(function () {
          scope.permissions.invalidate();
          scope.permissions.resolve().then(function () {
            scope.showSpinner.casePermissions = false;
            updateToolbar();
          });
        });
      };

      scope.restrict = function () {
        scope.showSpinner.caseToolbar = true;
        scope.showSpinner.casePermissions = true;
        caseService.restrictCase($routeParams.caseId).then(function () {
          scope.permissions.invalidate();
          scope.permissions.resolve().then(function () {
            scope.showSpinner.casePermissions = false;
            updateToolbar();
          });
        });
      };
      // End Restrict / Unrestrict
      
      // Mark Read / Unread
      scope.markReadUnread = function (read) {
        var markFunction = read ? caseService.markRead : caseService.markUnread;
        scope.showSpinner.caseToolbar = true;
        
        markFunction($routeParams.caseId).then(function () {
          scope.commands.resolve().then(function () {
            updateToolbar();
          });
        });
      };
      // End Mark Read / Unread
      
      // Close
      scope.close = function () {
        caseService.closeCase($routeParams.caseId).then(function () {
          var href = navigationService.caseListHrefFromCase(scope.caze);
          window.location.replace(href);
        });
      };
      // End Close
      
      // Delete
      scope.deleteCase = function () {
        caseService.deleteCase($routeParams.caseId).then(function () {
          var href = navigationService.caseListHrefFromCase(scope.caze);
          window.location.replace(href);
        });
      };
      // End Delete
      
      // Send to (BROKEN)
      // NOTE: Why does this use response[0]? the response is a large array of possible
      // recipients. Why always the first in the list?
      scope.sendTo = function () {
        //debugger;
        scope.possibleSendTo = caseService.getPossibleSendTo($routeParams.caseId);
        scope.possibleSendTo.promise.then(function (response) {
          scope.sendToRecipients = response;
          console.log('send to ');
          console.log(response);
          if (response[0]) {
            scope.show = true;
            scope.sendToId = response[0] && response[0].id;
          }
          
          scope.commandView = 'sendTo';
        });
      };

      scope.sendToIdChanged = function (id) {
        scope.sendToId = id;
      };

      scope.onSendToButtonClicked = function () {
        var sendToId = scope.sendToId;

        caseService.sendCaseTo($routeParams.caseId, sendToId, function(){
          scope.show = false;
        });
        /*.then(function () {
          var href = navigationService.caseListHrefFromCase(scope.caze);
          window.location.replace(href);
        });*/
      };
      // End Send to
      
      // Assign / Unassign
      scope.assign = function () {
        caseService.assignCase($routeParams.caseId).then(function () {
          var href = navigationService.caseListHrefFromCase(scope.caze);
          window.location.replace(href);
        });
      };

      scope.unassign = function () {
        caseService.unassignCase($routeParams.caseId).then(function () {
          var href = navigationService.caseListHrefFromCase(scope.caze);
          window.location.replace(href);
        });
      };
      // End Assign / Unassign
      
      // Attachments
      scope.attachments.promise.then(function () {
        scope.showSpinner.caseAttachment = false;
      });
      
      scope.downloadAttachment = function (attachment) {
        // Hack to replace dummy user and pass with authentication from token.
        // This is normally sent by http headers in ajax but not possible here.
        var apiUrl = scope.apiUrl.replace(/https:\/\/(.*)@/, function () {
          var userPass = window.atob(tokenService.getToken());
          return 'https://' + userPass + '@' ;
        });
        
        var url = apiUrl + '/cases/' + $routeParams.caseId + '/attachments/' + attachment.href + 'download';
        window.location.replace(url);
      };

      scope.deleteAttachment = function(attachmentId){
        caseService.deleteAttachment($routeParams.caseId, attachmentId).then(function () {
          scope.showSpinner.caseAttachment = true;
          scope.attachments.invalidate();
          scope.attachments.resolve().then(function () {
            scope.showSpinner.caseAttachment = false;
          });
        });
      };
      // End Attachments

     var defaultFiltersUrl =  caseService.getWorkspace() + '/cases/' + $routeParams.caseId + '/caselog/defaultfilters';
      httpService.getRequest(defaultFiltersUrl, false).then(function(result){
        var defaultFilters = result.data;
        scope.sideBarCaseLogs = caseService.getSelectedFilteredCaseLog($routeParams.caseId, defaultFilters);
      });

      scope.$on('caselog-message-created', function(){
        scope.sideBarCaseLogs.invalidate();
        scope.sideBarCaseLogs.resolve();
      });

      scope.$on('conversation-message-created', function(){
        scope.conversations = caseService.getSelectedConversations($routeParams.caseId);
        scope.conversations.invalidate();
        scope.conversations.resolve();
      });

      scope.$on('contact-name-updated', function(){
        scope.contacts.invalidate();
        scope.contacts.resolve();
      });

      scope.$on('case-changed', function(e, attr) {
        if (attr.command === 'casetype') {
          scope.commands.invalidate();
          scope.commands.resolve();
        } else if (attr.command === 'changedueon') {
          scope.general.invalidate();
          scope.general.resolve();
        }
      });

      scope.$on('casedescription-changed', function(){
          scope.caze = caseService.getSelected($routeParams.caseId);
      });

      scope.$on('participant-removed', function(){
        scope.conversations = caseService.getSelectedConversations($routeParams.caseId);
      });

      scope.showContact = function(contactId){
        alert("Not supported - need UX for this.");
      };

      scope.submitContact = function() {
        caseService.addContact($routeParams.caseId, scope.contact).then(function(){
          var href = navigationService.caseHrefSimple($routeParams.caseId);
          window.location.assign(href + "/contact/" + scope.contacts.length + "/");
        });
      };

      /*scope.$watch('sidebardata', function(newVal){
        if(newVal){
          scope.sidebardata = newVal;
          initSidebar();
        }   
      });

      console.log($routeParams);

      var initSidebar = function(){


        ///////////////////////////////////////////////
        /////// Datepicker angular-bootstrap /////////
        /////////////////////////////////////////////
        

          scope.today = function() {
            scope.dt = new Date();
          };
          //scope.today();

          scope.clear = function () {
            scope.dt = null;
          };

          scope.toggleDatepicker = function($event){
            $event.preventDefault();
            $event.stopPropagation();
            scope.opened = !scope.opened;
          };

          scope.dateOptions = {
            selectYears: true,
            selectMonths: true,
            format: 'yyyy-mm-dd',
            min: new Date(),
            close: false,
            clear: false,
            onClose: function () {
              var value = $element.val();

              if (dateRegex.test(value)) {
                // Set fancy date without triggering ng-change.
                $element.val(fancyDateService.format(value)).blur();
              }
            }

          }

          ///////////////////////////////////////////
          //////////// End datepicker //////////////
          /////////////////////////////////////////

        console.log(scope.sidebardata);
        var caseId = scope.sidebardata.caseData.caseId;
        scope.caze = scope.sidebardata.caseData.caze;
        console.log(scope.caze);
        scope.general = scope.sidebardata.general;
        scope.notes = scope.sidebardata.notes;

        //Datepicker specific
        scope.opened = false;
        scope.dt = new Date();

        //TODO: Cannot find usage of below commented variables. Investigate
       //scope.projectId = scope.sidebardata.caseData[0].owner;
       //scope.projectType = scope.projectdata.projectType;

        if($routeParams.formId && $routeParams.caseId){
          var formId = scope.formdata.formId;
          scope.possibleForms = caseService.getSelectedPossibleForms(caseId);
          scope.submittedForms = caseService.getSubmittedForms(caseId, formId);
          scope.submittedFormList = caseService.getSubmittedFormList(caseId);
        }
        
        

        scope.contacts = caseService.getSelectedContacts(caseId);
        scope.conversations = caseService.getSelectedConversations(caseId);
        scope.attachments = caseService.getSelectedAttachments(caseId);
        scope.apiUrl = httpService.apiUrl + caseService.getWorkspace();
        scope.possiblePriorities = caseService.getPossiblePriorities(caseId);
        scope.possibleCaseTypes = caseService.getPossibleCaseTypes(caseId);
        
        



        //Event listeners if things are updated by user or other user
        scope.$on('case-created', function() {
          scope.caze.invalidate();
        });

        scope.$on('case-changed', function() {
          scope.caze.invalidate();
          scope.caze.resolve();
        });

        scope.$on('note-changed', function() {
          scope.notes = caseService.getSelectedNote(caseId);
        });

        scope.$on('noteDescription-changed', function() {
          scope.caze = caseService.getSelected(caseId);
        })


        scope.showSpinner = {
          caseType: true,
          caseLabels: true,
          caseDueOn: true,
          casePriority: true,
          caseToolbar: false,
          casePermissions: true,
          caseAttachment: true
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
        
        //TODO: CONTINUE HERE

        // Resolve
        scope.showToolbar = function () {
          if(scope.possibleResolutions){
            return !!scope.possibleResolutions.length;
          } else {
            scope.resolve();
          }
        };
        
        scope.resolve = function() {
          scope.possibleResolutions = caseService.getPossibleResolutions(caseId);
          scope.possibleResolutions.promise.then(function (response) {
            scope.resolution = response[0].id;
            scope.commandView = 'resolve';
          });
        };

        scope.onResolveButtonClicked = function(){
          var resolutionId = scope.resolution;

          var callback = function () {
            var href = navigationService.caseListHrefFromCase(scope.caze);
            window.location.replace(href);
          };

          caseService.resolveCase(caseId, resolutionId, callback);
        };

        scope.onCancelResolveButtonClicked = function () {
          scope.commandView = '';
        };
        // End Resolve
        
        // Due on
        scope.general.promise.then(function (result) {
          scope.dueOnShortStartValue = result[0].dueOnShort;
          scope.showSpinner.caseDueOn = false;
        });
        
        scope.changeDueOn = function (date) {
          scope.showSpinner.caseDueOn = true;
          
          // Must be in the future and time must be set (but is not used).
          var isoString = (new Date(date + 'T23:59:59.000Z')).toISOString();
          caseService.changeDueOn(caseId, isoString).then(function () {
            scope.general.invalidate();
            scope.general.resolve().then(function (result) {
              scope.dueOnShortStartValue = result[0].dueOnShort;
              scope.showSpinner.caseDueOn = false;
            });
          });
        };
        // End due on

        // Priority
        scope.priority = '-1';
        scope.priorityColor = {};
        scope.activePriorityColor = {};

        var prioDfds = [];
        prioDfds.push(scope.general.promise, scope.possiblePriorities.promise);
        $q.all(prioDfds).then(function(response){
          debugger;
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
        
        scope.changePriorityLevel = function(priorityId) {
          scope.showSpinner.casePriority = true;

          if (priorityId === '-1') {
            priorityId = '';
          }

          caseService.changePriorityLevel(caseId, priorityId).then(function () {
            if (scope.priorityColor[priorityId]) {
              scope.activePriorityColor = {
                'background-color': scope.priorityColor[priorityId]
              };
            }
            
            scope.showSpinner.casePriority = false;
          });
        };
        // Priority end
        
        // Case type
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
        
        scope.changeCaseType = function(casetype) {
          scope.showSpinner.caseToolbar = true;
          caseService.changeCaseType(caseId, casetype).then(function () {
            scope.showSpinner.caseToolbar = false;
            updateCaseLabels();
            updateToolbar();
          });
        };
        // End case type

        // Case labels
        scope.allCaseLabels = [];
        scope.activeLabels = [];
        var previousActiveLabels = [];
        
        var updateCaseLabels = function () {
          scope.showSpinner.caseLabels = true;
          
          if (scope.caseLabel) {
            scope.caseLabel.invalidate();
            scope.caseLabel.resolve();
          } else {
            scope.caseLabel = caseService.getCaseLabel(caseId);
          }
          
          if (scope.possibleCaseLabels) {
            scope.possibleCaseLabels.invalidate();
            scope.possibleCaseLabels.resolve();
          } else {
            scope.possibleCaseLabels = caseService.getPossibleCaseLabels(caseId);
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
            
            previousActiveLabels = scope.activeLabels;
            scope.showSpinner.caseLabels = false;
            
            setTimeout(function () {
              jQuery('.chosen-case-label').chosen({ 'search_contains': true }).trigger('chosen:updated');
            }, 0);
          });
        };
        
        updateCaseLabels();
        
        scope.changeCaseLabels = function (labels) {
          //debugger;
          //scope.showSpinner.caseLabels = true;
          var removedLabels = previousActiveLabels.filter(function (item) {
            return !_.find(labels, function (j) {
                return item.id === j.id;
            });
          });
          
          if (removedLabels.length > 0) {
            var removePromises = removedLabels.map(function (label) {
              return caseService.deleteCaseLabel(caseId, label.id);
            });
            
            $q.all(removePromises).then(updateCaseLabels);
          } else {
            var addPromises = labels.map(function (label) {
              return caseService.addCaseLabel(caseId, label.id);
            });
            
            $q.all(addPromises).then(updateCaseLabels);
          }
        
          previousActiveLabels = labels;
        };
        // End case labels
        
        // Commands (toolbar)
        var updateToolbar = function () {
          scope.showSpinner.caseToolbar = true;
          
          if (scope.commands) {
            scope.commands.invalidate();
            scope.commands.resolve();
          } else {
            scope.commands = caseService.getSelectedCommands(caseId);
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
        updateToolbar();
        // End commands (toolbar)
        
        // Restrict / Unrestrict
        scope.permissions = caseService.getPermissions(caseId);
        scope.permissions.promise.then(function () {
          scope.showSpinner.casePermissions = false;
        });
        
        scope.unrestrict = function () {
          scope.showSpinner.caseToolbar = true;
          scope.showSpinner.casePermissions = true;
          caseService.unrestrictCase(caseId).then(function () {
            scope.permissions.invalidate();
            scope.permissions.resolve().then(function () {
              scope.showSpinner.casePermissions = false;
              updateToolbar();
            });
          });
        };

        scope.restrict = function () {
          scope.showSpinner.caseToolbar = true;
          scope.showSpinner.casePermissions = true;
          caseService.restrictCase(caseId).then(function () {
            scope.permissions.invalidate();
            scope.permissions.resolve().then(function () {
              scope.showSpinner.casePermissions = false;
              updateToolbar();
            });
          });
        };
        // End Restrict / Unrestrict
        
        // Mark Read / Unread
        scope.markReadUnread = function (read) {
          var markFunction = read ? caseService.markRead : caseService.markUnread;
          scope.showSpinner.caseToolbar = true;
          
          markFunction(caseId).then(function () {
            scope.commands.resolve().then(function () {
              updateToolbar();
            });
          });
        };
        // End Mark Read / Unread
        
        // Close
        scope.close = function () {
          caseService.closeCase(caseId).then(function () {
            var href = navigationService.caseListHrefFromCase(scope.caze);
            window.location.replace(href);
          });
        };
        // End Close
        
        // Delete
        scope.deleteCase = function () {
          caseService.deleteCase(caseId).then(function () {
            var href = navigationService.caseListHrefFromCase(scope.caze);
            window.location.replace(href);
          });
        };
        // End Delete
        
        // Send to (BROKEN!)
        scope.sendTo = function () {
          scope.possibleSendTo = caseService.getPossibleSendTo(caseId);
          scope.possibleSendTo.promise.then(function (response) {
            if (response[0]) {
              scope.sendToId = response[0] && response[0].id;
            }
            
            scope.commandView = 'sendTo';
          });
        };

        scope.sendToIdChanged = function () {
          scope.sendToId = event;
        };

        scope.onSendToButtonClicked = function () {
          var sendToId = scope.sendToId;

          caseService.sendCaseTo(caseId, sendToId).then(function () {
            var href = navigationService.caseListHrefFromCase(scope.caze);
            window.location.replace(href);
          });
        };
        // End Send to
        
        // Assign / Unassign
        scope.assign = function () {
          caseService.assignCase(caseId).then(function () {
            var href = navigationService.caseListHrefFromCase(scope.caze);
            window.location.replace(href);
          });
        };

        scope.unassign = function () {
          caseService.unassignCase(caseId).then(function () {
            var href = navigationService.caseListHrefFromCase(scope.caze);
            window.location.replace(href);
          });
        };
        // End Assign / Unassign
        
        // Attachments
        scope.attachments.promise.then(function () {
          scope.showSpinner.caseAttachment = false;
        });
        
        scope.downloadAttachment = function (attachment) {
          // Hack to replace dummy user and pass with authentication from token.
          // This is normally sent by http headers in ajax but not possible here.
          var apiUrl = scope.apiUrl.replace(/https:\/\/(.*)@/, function () {
            var userPass = window.atob(tokenService.getToken());
            return 'https://' + userPass + '@' ;
          });
          
          var url = apiUrl + '/cases/' + caseId + '/attachments/' + attachment.href + 'download';
          window.location.replace(url);
        };

        scope.deleteAttachment = function(attachmentId){
          caseService.deleteAttachment(caseId, attachmentId).then(function () {
            scope.showSpinner.caseAttachment = true;
            scope.attachments.invalidate();
            scope.attachments.resolve().then(function () {
              scope.showSpinner.caseAttachment = false;
            });
          });
        };
        // End Attachments

        var defaultFiltersUrl =  caseService.getWorkspace() + '/cases/' + caseId + '/caselog/defaultfilters';
        httpService.getRequest(defaultFiltersUrl, false).then(function(result){
          var defaultFilters = result.data;
          scope.sideBarCaseLogs = caseService.getSelectedFilteredCaseLog(caseId, defaultFilters);
        });

        scope.$on('caselog-message-created', function(){
          scope.sideBarCaseLogs.invalidate();
          scope.sideBarCaseLogs.resolve();
        });

        scope.$on('conversation-message-created', function(){
          scope.conversations = caseService.getSelectedConversations(caseId);
          scope.conversations.invalidate();
          scope.conversations.resolve();
        });

        scope.$on('contact-name-updated', function(){
          scope.contacts.invalidate();
          scope.contacts.resolve();
        });

        scope.$on('case-changed', function(e, attr) {
          if (attr.command === 'casetype') {
            scope.commands.invalidate();
            scope.commands.resolve();
          } else if (attr.command === 'changedueon') {
            scope.general.invalidate();
            scope.general.resolve();
          }
        });

        scope.$on('casedescription-changed', function(){
            scope.caze = caseService.getSelected(caseId);
        });

        scope.$on('participant-removed', function(){
        scope.conversations = caseService.getSelectedConversations(caseId);
        });

        scope.showContact = function(contactId){
          alert('Not supported - need UX for this.');
        };
      };*/

    }
  };
});