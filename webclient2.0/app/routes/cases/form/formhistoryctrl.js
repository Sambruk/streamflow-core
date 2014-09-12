'use strict';
angular.module('sf')
  .controller('FormHistoryCtrl', function($scope, caseService, $routeParams) {

    $scope.caseId = $routeParams.caseId;
    $scope.submittedForms = caseService.getSubmittedForms($routeParams.caseId, $routeParams.formId);
    $scope.submittedFormList = caseService.getSubmittedFormList($routeParams.caseId);

    $scope.$watch("selectedSubmittedForm", function(){
      var index = $scope.selectedSubmittedForm;
      if (_.isNumber(index)){
        $scope.submittedForm = caseService.getSubmittedForm($routeParams.caseId, index);
      }
    });
  });
