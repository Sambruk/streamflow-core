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
  .controller('CaseEditCtrl', function($scope, $rootScope, $routeParams, caseService ) {
    $scope.sidebardata = {};

    $scope.$watch('sidebardata.caze', function(newVal){
      if(!newVal){
        return;
      }
      $scope.caze = $scope.sidebardata.caze;
      $scope.caze.promise.then(function(){
        $scope.caseDescription = $scope.caze[0].text;
      });
    });

    $scope.$watch('sidebardata.notes', function(newVal){
      if(!newVal){
        return;
      }
      $scope.notes = $scope.sidebardata.notes;

      $scope.notes.promise.then(function(){
        $scope.caseNote = $scope.notes[0].note;
      });
    });

    $scope.$watch('caseDescription', function(newVal){
      if(!newVal){
        return;
      }
      $scope.caseDescription = newVal;
    });

    $scope.$watch('caseNote', function(newVal){
      if(!newVal){
        return;
      }
      $scope.caseNote = newVal;
    });


    $scope.addNote = function($event, $success, $error){
      $event.preventDefault();

      $scope.notes[0].note = $scope.caseNote;
      if($scope.notes[0].note === $event.target.value){
        caseService.addNote($routeParams.caseId, $scope.notes[0])
        .then(function(response){
          $rootScope.$broadcast('note-changed');
          $success($($event.target));
        }, function (error){
          $error($error($event.target));
        });
      }
    };

    $scope.changeCaseDescription = function($event, $success, $error){
      $event.preventDefault();

      $scope.caze[0].text = $scope.caseDescription;
      if($event.currentTarget.value.length > 50){
        $error($($event.target));
      }else{
        caseService.changeCaseDescription($routeParams.caseId, $scope.caze[0].text)
        .then(function(){
          $rootScope.$broadcast('casedescription-changed');
          $success($($event.target));
        }, function(error) {
          $error($error($event.target));
        });
      }
    };

  });
