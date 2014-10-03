'use strict';
angular.module('sf')
.controller('CaseDetailCtrl', function($scope){
  $scope.sidebardata = {};
  $scope.showSpinner = {
    caze: true
  };

  $scope.$watch('sidebardata.caze', function(newVal){
    if(!newVal){
      return;
    }
    $scope.caze = $scope.sidebardata.caze;
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

});
