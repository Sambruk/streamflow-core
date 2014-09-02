'use strict';
angular.module('sf')
  .controller('CaseEditCtrl', function(growl, $scope, $rootScope, $routeParams, caseService, navigationService) {
    $scope.caze = caseService.getSelected($routeParams.caseId);
    $scope.general = caseService.getSelectedGeneral($routeParams.caseId);

    $scope.notes = caseService.getSelectedNote($routeParams.caseId);
    $scope.cachedNote = caseService.getSelectedNote($routeParams.caseId);


    $scope.addNote = function($event, $success, $error){
      $event.preventDefault();
      if($scope.notes[0].note !== $scope.cachedNote[0].note || $event.target.value == $scope.caze[0].text){
        caseService.addNote($routeParams.caseId, $scope.notes[0])
        .then(function(){
          $success($($event.target));
          $rootScope.$broadcast('note-changed');
        }, function (error){
          $error($error($event.target));
        });
      }   
    }

    $scope.changeCaseDescription = function($event, $success, $error){
      $event.preventDefault();
      caseService.changeCaseDescription($routeParams.caseId, $scope.caze[0].text)
      .then(function(){
        $success($($event.target));
        $rootScope.$broadcast('casedescription-changed');
      }, function(error) {
        $error($error($event.target));
      });
    }
  });