'use strict';

angular.module('sf')
  .controller('CaseListCtrl', function(growl, $scope, $routeParams, projectService, $rootScope) {
    $scope.currentCases = projectService.getSelected($routeParams.projectId, $routeParams.projectType);
    $scope.projectType = $routeParams.projectType;

    $scope.showSpinner = {
      currentCases: true
    };

    //Set breadcrumbs to case-owner if possible else to project id
    $scope.currentCases.promise.then(function(response){
     var owner = _.filter(response, function(sfCase){
        if(sfCase.owner.length > 0){
          return sfCase.owner;
        }
      });

      if(owner.length > 0){
        $rootScope.$broadcast('breadcrumb-updated', [{owner: response[0].owner}, {projectType: $routeParams.projectType}]);
      } else {
        $rootScope.$broadcast('breadcrumb-updated', [{projectId: $routeParams.projectId}, {projectType: $routeParams.projectType}]);
      }

      $scope.showSpinner.currentCases = false;
    });

    var updateObject = function(itemToUpdate){
      itemToUpdate.invalidate();
      itemToUpdate.resolve();
    };
    
    // Event listeners
    $rootScope.$on('case-created', function(){
      updateObject($scope.currentCases);
    });
    $rootScope.$on('case-closed', function(){
      updateObject($scope.currentCases);
    });
    $rootScope.$on('case-assigned', function(){
      updateObject($scope.currentCases);
    });
    $rootScope.$on('case-unassigned', function(){
      updateObject($scope.currentCases);
    });
    $rootScope.$on('case-resolved', function(){
      updateObject($scope.currentCases);
    });
    $rootScope.$on('case-deleted', function(){
      updateObject($scope.currentCases);
    });
    $rootScope.$on('case-owner-changed', function(){
      updateObject($scope.currentCases);
    });
    $rootScope.$on('casedescription-changed', function(){
      updateObject($scope.currentCases);
    });
    // End Event listeners
  });