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
  .controller('CaseEditCtrl', function($scope, $rootScope, $routeParams, caseService, navigationService ) { 
    $scope.sidebardata = {};

    $scope.$watch('sidebardata.caze', function(newVal){
      if(!newVal){
        return;
      }
      $scope.caze = $scope.sidebardata.caze;
    });

    $scope.$watch('caseDescription', function(newVal){
      if(!newVal){
        return;
      }
      $scope.caseDescription = newVal;
    });

    $scope.$watch('sidebardata.notes', function(newVal){
      if(!newVal){
        return;
      }
      $scope.notes = $scope.sidebardata.notes;
    });

    $scope.$watch('caseNote', function(newVal){
      if(!newVal){
        return
      }
      $scope.caseNote = newVal;
    });


    $scope.addCaseDescriptionAndNote = function($event){
      $event.preventDefault();

      if($scope.caseDescription && $scope.caseNote){
        $scope.caze[0].text = $scope.caseDescription;
        $scope.notes[0].note = $scope.caseNote;

        caseService.changeCaseDescription($routeParams.caseId, $scope.caseDescription)
        .then(function(){
          $rootScope.$broadcast('casedescription-changed');
        });
        caseService.addNote($routeParams.caseId, $scope.notes[0])
        .then(function(){
          $rootScope.$broadcast('note-changed');
        });

        var href = navigationService.caseHrefSimple($routeParams.caseId);
        window.location.assign(href);
      }
    }

  });