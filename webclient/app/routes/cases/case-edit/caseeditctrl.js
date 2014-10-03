'use strict';
angular.module('sf')
  .controller('CaseEditCtrl', function($scope, $rootScope, $routeParams, caseService) { 
    $scope.sidebardata = {};

    $scope.$watch('sidebardata.caze', function(newVal){
      if(!newVal){
        return;
      }
      $scope.caze = $scope.sidebardata.caze;
    });

    $scope.$watch('sidebardata.notes', function(newVal){
      if(!newVal){
        return;
      }
      $scope.notes = $scope.sidebardata.notes;
    });

    $scope.addNote = function($event, $success, $error){
      $event.preventDefault();
      if($scope.notes[0].note === $event.target.value){
        caseService.addNote($routeParams.caseId, $scope.notes[0])
        .then(function(response){
          $rootScope.$broadcast('note-changed');
          $success($($event.target));
        }, function (error){
          $error($error($event.target));
        });
      }   
    }

    $scope.changeCaseDescription = function($event, $success, $error){
      $event.preventDefault();
      caseService.changeCaseDescription($routeParams.caseId, $scope.caze[0].text)
      .then(function(response){
        $rootScope.$broadcast('casedescription-changed');
        $success($($event.target));
      }, function(error) {
        $error($error($event.target));
      });
    }
  });