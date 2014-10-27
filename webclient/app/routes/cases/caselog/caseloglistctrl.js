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
  .controller('CaselogListCtrl', function($scope, $rootScope, caseService, $routeParams, navigationService, httpService) {
    $scope.sidebardata = {};
    $scope.caseId = $routeParams.caseId;
    var defaultFiltersUrl = caseService.getWorkspace() + '/cases/' + $scope.caseId + '/caselog/defaultfilters';

    $scope.showSpinner = {
      caseLogs: true
    };
    
    httpService.getRequest(defaultFiltersUrl, false)
    .then(function(result){
      var filterObj = result.data;
      var filterArray = [];
      for (var prop in filterObj) {
        filterArray.push({ "filterName": prop, "filterValue": filterObj[prop] });
      }
      $scope.caseLogFilters = filterArray;
      $scope.caseLogs = caseService.getSelectedCaseLog($routeParams.caseId);

      $scope.caseLogs.promise.then(function(){
        $scope.showSpinner.caseLogs = false;
      });
    });

    $rootScope.$on('update-caseLogs', function(){
      $scope.showSpinner.caseLogs = true;
      updateObject($scope.caseLogs);

      $scope.caseLogs.promise.then(function(){
        $scope.showSpinner.caseLogs = false;
      });
    });
    
    $scope.$on('caselog-message-created', function(){
      $scope.showSpinner.caseLogs = true;
      updateObject($scope.caseLogs);

      $scope.caseLogs.promise.then(function(){
        $scope.showSpinner.caseLogs = false;
      });
    });

    var updateObject = function(itemToUpdate){
      itemToUpdate.invalidate();
      itemToUpdate.resolve();
    };
  });