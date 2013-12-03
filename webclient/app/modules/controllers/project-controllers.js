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

  var sfProject = angular.module('sf.controllers.project', ['sf.services.navigation', 'sf.services.project']);

  function toggleToolbar($event) {
    $event.preventDefault();
    $('.functions-menu').toggleClass('open');
  }

  sfProject.controller('ProjectListCtrl', ['$scope', 'projectService', '$routeParams', 'navigationService', '$rootScope',
    function($scope, projectService, $params, navigationService, $rootScope) {
      $scope.projects = projectService.getAll();
      $scope.toggleToolbar = toggleToolbar;

      function canCreateCase() {

        if ($params.projectType === 'inbox') {
          return false;
        }
        if (!$params.projectType) {
          return false;
        }

        return true;
      } 

      $scope.canCreateCase = canCreateCase;

      $scope.createCase = function(){

        if (!canCreateCase())
          return;

        $rootScope.$broadcast('case-created');

        projectService.createCase($params.projectId, $params.projectType).then(function(response){
          var caseId = response.data.events[1].entity;
          var href = navigationService.caseHrefSimple(caseId);

          window.location.replace(href + "/edit");
        });
      }

    }]);

})();
