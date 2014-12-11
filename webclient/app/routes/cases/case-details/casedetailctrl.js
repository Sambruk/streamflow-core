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
.controller('CaseDetailCtrl', function($scope, $rootScope, $routeParams, caseService, navigationService){

  $scope.sidebardata = {};
  $scope.showSpinner = {
    caze: true
  };

  $scope.$watch('sidebardata.caze', function(newVal){
    if(!newVal){
      return;
    }
    if($scope.sidebardata){
      $scope.caze = $scope.sidebardata.caze;
    }
    $scope.notes = $scope.sidebardata.notes;
    $scope.caze.promise.then(function(){
      $scope.showSpinner.caze = false;
    });
  });


  $scope.$watch('sidebardata.notes', function(newVal){
    if(!newVal){
      return;
    }
    $scope.notes = $scope.sidebardata.notes;
  });

  $scope.$watch('sidebardata.notesHistory', function(newVal){
    if(!newVal){
      return;
    }
    $scope.notesHistory = $scope.sidebardata.notesHistory;

    $scope.notesHistory.promise.then(function(){
      if($scope.notesHistory.length == 0){
        return;
      }
      if($scope.notesHistory[$scope.notesHistory.length -1].note == ''){
        $scope.notesHistory.pop();
      }
    });
  });

  $scope.addNote = function($event){
    $event.preventDefault();
    if($scope.noteToAdd){
      $scope.notes[0].note = $scope.noteToAdd;
      caseService.addNote($routeParams.caseId, $scope.notes[0])
      .then(function(response){
        $scope.noteToAdd = '';
        $scope.notesHistory.invalidate();
        $scope.notesHistory.resolve();
      });
    }
  };

  $scope.changeCaseDescription = function($event, $success, $error){
    $event.preventDefault();

    if ($event.currentTarget.value === '')  {
      $error($($event.target));
    }else{
      caseService.changeCaseDescription($routeParams.caseId, $scope.caze[0].text)
      .then(function(response){
        $rootScope.$broadcast('casedescription-changed');
        $success($($event.target));
      }, function(error) {
        $error($error($event.target));
      });
    }
  }

});
