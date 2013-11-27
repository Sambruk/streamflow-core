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

  var sfCaselog = angular.module('sf.controllers.caselog', ['sf.services.case', 'sf.services.navigation', 'sf.services.http']);

  sfCaselog.controller('CaselogListCtrl', ['$scope', 'caseService', '$routeParams','navigationService', 'httpService',
    function($scope, caseService, $params, navigationService, httpService) {

      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;
      $scope.caseId = $params.caseId;     

      var defaultFiltersUrl = 'workspacev2/cases/' + $params.caseId + '/caselog/defaultfilters';      
      httpService.getRequest(defaultFiltersUrl, false).then(function(result){
          
          var filterObj = result.data;
          var filterArray = [];
          for (var prop in filterObj) {
              filterArray.push({ "filterName": prop, "filterValue": filterObj[prop] });
          }
          $scope.caseLogFilters = filterArray;

        $scope.caseLogs = caseService.getSelectedCaseLog($params.projectId, $params.projectType, $params.caseId);
        //$scope.caseLogs = caseService.getSelectedCaseLog($params.projectId, $params.projectType, $params.caseId, defaultFilters);
        //console.log($scope.caseLogs);
      });
   
    }]);

  sfCaselog.controller('CaselogEntryCreateCtrl', ['$scope', '$rootScope', 'caseService', '$routeParams','navigationService',
    function($scope, $rootScope, caseService, $params, navigationService) {

      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;
      $scope.caseId = $params.caseId;

      $scope.submitCaseLogEntry = function($event){
        $event.preventDefault();

        var entry = $scope.caseLogEntryToCreate;
        caseService.createCaseLogEntry($params.projectId, $params.projectType, $params.caseId, entry).then(function(response){
          var href = navigationService.caseHref($params.caseId) + "/caselog";
          $scope.caseLogs.invalidate();
          $scope.caseLogs.resolve();
          $rootScope.$broadcast('caselog-message-created');
          window.location.assign(href);
        });
      }

    }]);
  

})();
