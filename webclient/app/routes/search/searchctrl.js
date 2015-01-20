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

angular.module('sf').controller('SearchCtrl', function ($scope, $routeParams, searchService) {

  var query = $routeParams.query;

  $scope.currentPage = 1;
  $scope.pageSize = 10;
  $scope.currentCases = [];
  $scope.showSpinner = {
    currentCases: true
  };

  $scope.getHeader = function () {
    return 'Sökresultat';
  };

  searchService.getCases(query).promise.then(function (result) {
    $scope.currentCases = result;
    $scope.showSpinner.currentCases = false;
  });

});
