'use strict';
angular.module('sf')
  .controller('CaseListCtrl', function(growl, $scope, $routeParams, projectService, $rootScope) {
    $scope.currentCases = projectService.getSelected($routeParams.projectId, $routeParams.projectType);
    $scope.projectType = $routeParams.projectType;

    $rootScope.$broadcast('breadcrumb-updated', [{projectId: $routeParams.projectId}, {projectType: $routeParams.projectType}]);
});