'use strict';
angular.module('sf')
  .controller('CaseListCtrl', function(growl, $scope, $routeParams, projectService, $rootScope) {
    $scope.currentCases = projectService.getSelected($routeParams.projectId, $routeParams.projectType);
    $scope.projectType = $routeParams.projectType;

    /**
     * ERROR HANDLER
     **/
    //TODO: Implement error handler listener on other controllers where needed
    /*$scope.errorHandler = function(){;
     var bcMessage = caseService.getMessage();
     if(bcMessage === 200)  {
     growl.addSuccessMessage('successMessage');
     }else {
     growl.addWarnMessage('errorMessage');
     }
     };*/

    $rootScope.$broadcast('breadcrumb-updated', [{projectId: $routeParams.projectId}, {projectType: $routeParams.projectType}]);

    //error-handler
    //$scope.$on('httpRequestInitiated', $scope.errorHandler);
});