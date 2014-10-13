'use strict';
angular.module('sf')
  .controller('FormHistoryCtrl', function($scope, caseService, $routeParams) {

    $scope.caseId = $routeParams.caseId;
    $scope.submittedForms = caseService.getSubmittedForms($routeParams.caseId, $routeParams.formId);

    $scope.submittedForms.promise.then(function(){
      if(!$scope.selectedSubmittedForm && $scope.submittedForms.length > 0){
        $scope.selectedSubmittedForm = $scope.submittedForms[0].index;
      }
    });

    $scope.$watch("selectedSubmittedForm", function(){
      var index = $scope.selectedSubmittedForm;
      if (_.isNumber(index)){
        $scope.submittedForm = caseService.getSubmittedForm($routeParams.caseId, index);
      }
    });
  });
