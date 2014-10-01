'use strict';

angular.module('sf')
  .controller('CaseListCtrl', function(growl, $scope, $routeParams, projectService, $rootScope) {
    $scope.currentCases = projectService.getSelected($routeParams.projectId, $routeParams.projectType);
    $scope.projectType = $routeParams.projectType;

    $rootScope.$on('case-changed-update-context-and-caselist', function(){
      $scope.currentCases.invalidate();
      $scope.currentCases.resolve();
      console.log('case-changed-update-caselist');
    });

    $rootScope.$on('casedescription-changed', function(){
      $scope.currentCases.invalidate();
      $scope.currentCases.resolve();
    });

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
    });
  });