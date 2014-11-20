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
.directive('caselogentry', function($rootScope, $location, $routeParams, $q, caseService, navigationService){
  return {
    restrict: 'E',
    templateUrl: 'components/caselogentry/caselogentry.html',
    scope: {
      caseid: '=?'
    },
    link: function(scope){
      scope.caseLogEntryToCreate = '';
      scope.caseId = $routeParams.caseId;
      scope.caseLogs;

      scope.$watch('caseLogs', function(newVal){
        if(!newVal){
          return;
        }
        scope.caseLogs = newVal;
      });

      scope.submitCaseLogEntry = function($event){
        $event.preventDefault();
        caseService.createCaseLogEntry(scope.caseId, scope.caseLogEntryToCreate)
        .then(function(response){
          $rootScope.$broadcast('caselog-message-created');
          scope.caseLogEntryToCreate = '';
        });
      }
    }
  };
});