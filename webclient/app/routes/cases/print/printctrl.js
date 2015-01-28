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
  .controller('PrintCtrl', function(growl, $scope, $routeParams, caseService, $q){

    $scope.caze = caseService.getSelected($routeParams.caseId);
    $scope.general = caseService.getSelectedGeneral($routeParams.caseId);
    $scope.notes = caseService.getSelectedNote($routeParams.caseId);
    var dfds = [];
    dfds.push($scope.caze.promise, $scope.general.promise, $scope.notes.promise);

    $q.all(dfds)
    .then(function () {
      // Page has to be rendered first.
      setTimeout(function () {
        window.print();
      });
    });
  });
