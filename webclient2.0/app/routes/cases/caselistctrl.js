'use strict';
angular.module('sf')
    .controller('CaseListCtrl',
        function(growl, $scope, $params, projectService, $rootScope) {

            $scope.currentCases = projectService.getSelected($params.projectId, $params.projectType);
            $scope.projectType = $params.projectType;

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

            $rootScope.$broadcast('breadcrumb-updated', [{projectId: $params.projectId}, {projectType: $params.projectType}]);

            //error-handler
            //$scope.$on('httpRequestInitiated', $scope.errorHandler);
        });