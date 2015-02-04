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

angular.module('sf').controller('SearchCtrl', function ($scope, $routeParams, $rootScope, searchService, groupByService, paginationService, caseService, casePropertiesService) {
  $scope.currentCases = [];

  var query = $routeParams.query;
  var originalCurrentCases = [];
  var pagesShown = 1;

  $scope.showSpinner = {
    currentCases: true
  };

  $scope.getHeader = function () {
    return 'Sökresultat';
  };

  $scope.groupingOptions = groupByService.getGroupingOptions();

  $scope.groupBy = function(selectedGroupItem) {
    $scope.currentCases = groupByService.groupBy($scope.currentCases, originalCurrentCases, selectedGroupItem);
    $scope.specificGroupByDefaultSortExpression = groupByService.getSpecificGroupByDefault(selectedGroupItem);
  };

  searchService.getCases(query).promise.then(function (result) {
    $scope.currentCases = result;
    $scope.showSpinner.currentCases = false;

    $scope.currentCases.promise.then(function(){
      originalCurrentCases = $scope.currentCases;
      $rootScope.$broadcast('breadcrumb-updated',[]);

      $scope.currentCases = casePropertiesService.checkCaseProperties($scope.currentCases);

      // 'Pagination'
      $scope.itemsLimit = paginationService.itemsLimit(pagesShown);
      $scope.hasMoreItemsToShow = paginationService.hasMoreItemsToShow($scope.currentCases, pagesShown);

      $scope.showMoreItems = function() {
        pagesShown = paginationService.showMoreItems(pagesShown);
        $scope.itemsLimit = paginationService.itemsLimit(pagesShown);
        $scope.hasMoreItemsToShow = paginationService.hasMoreItemsToShow($scope.currentCases, pagesShown);
      };
    });
  });

});
