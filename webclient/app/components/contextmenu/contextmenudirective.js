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
.directive('contextmenu', function(projectService, navigationService, $rootScope, $timeout){
  return {
    restrict: 'E',
    templateUrl: 'components/contextmenu/contextmenu.html',
    scope: {
      params: '=?'
    },
    link: function(scope){
      scope.projects = projectService.getAll();

      scope.displayToolbar = false;

      /*scope.toggleToolbar = function(){
        scope.displayToolbar = !scope.displayToolbar;
      }*/

      scope.navigateTo = function(href, $event){
        $event.preventDefault();
        scope.toggleToolbar($event);
        navigationService.linkTo(href);
      };
      

      scope.toggleToolbar = function($event) {
        $event.preventDefault();
        $('.functions-menu').toggleClass('open');
        if ( $('.functions-menu').hasClass('open') ) {
          $('.sub-category').show();
        } else {
          $('.sub-category').hide();
        }
      };

      scope.canCreateCase = function() {
        if(!scope.params){
          return false;
        }
        if (scope.params.projectType === 'inbox') {
          return false;
        }
        if (!scope.params.projectType) {
          return false;
        }
        return true;
      };

      scope.createCase = function(){
        if(!scope.canCreateCase()){
          return;
        }

        projectService.createCase(scope.params.projectId, scope.params.projectType).then(function(response){
          //NOTE: Why is caseId defined here?
          var caseId = response.data.events[1].entity;
          var href = navigationService.caseHrefSimple(caseId);

          $rootScope.$broadcast('case-created');

          window.location.replace(href + "/edit");
        });
      }
      
      var updateObject = function(itemToUpdate){
        itemToUpdate.invalidate();
        itemToUpdate.resolve();
      };

      // Event listeners
      $rootScope.$on('case-created', function(){
        updateObject(scope.projects);
      });
      $rootScope.$on('case-closed', function(){
        updateObject(scope.projects);
      });
      $rootScope.$on('case-assigned', function(){
        updateObject(scope.projects);
      });
      $rootScope.$on('case-unassigned', function(){
        updateObject(scope.projects);
      });
      $rootScope.$on('case-resolved', function(){
        updateObject(scope.projects);
      });
      $rootScope.$on('case-deleted', function(){
        updateObject(scope.projects);
      });
      $rootScope.$on('case-owner-changed', function(){
        updateObject(scope.projects);
      });
      // End Event listeners
    }
  };
});