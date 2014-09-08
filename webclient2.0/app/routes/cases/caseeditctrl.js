'use strict';
angular.module('sf')
  .controller('CaseEditCtrl', function(growl, $q, $scope, $rootScope, $routeParams, caseService, navigationService) { 
    var cachedNote = caseService.getSelectedNote($routeParams.caseId);
    var caze = caseService.getSelected($routeParams.caseId);
    var notes = caseService.getSelectedNote($routeParams.caseId);
    var general = caseService.getSelectedGeneral($routeParams.caseId);

    var dfds = [];
    dfds.push(caze.promise, notes.promise, general.promise, cachedNote.promise);
    $q.all(dfds)
    .then(function(response){
      $scope.caze = response[0];
      $scope.notes = response[1];
      $scope.general = response[2];
      $scope.cachedNote = response[3];
    })
    .then(function(){
      $scope.sidebarData = {};
      $scope.sidebarData.caseData = {caseId: $routeParams.caseId, caze: $scope.caze};
      $scope.sidebarData.general = $scope.general;
      $scope.sidebarData.notes = $scope.notes;
      $scope.commands = caseService.getSelectedCommands($routeParams.caseId);
    });


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