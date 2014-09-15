'use strict';
angular.module('sf')
.controller('CaseDetailCtrl', function($scope){
  $scope.sidebardata = {};

  $scope.$watch('sidebardata.caze', function(newVal){
    if(!newVal){
      return;
    }
    console.log('sidebardata updated');
    $scope.caze = $scope.sidebardata.caze;
  });

  $scope.$watch('sidebardata.notes', function(newVal){
    if(!newVal){
      return;
    }
    $scope.notes = $scope.sidebardata.notes;
  });

});
