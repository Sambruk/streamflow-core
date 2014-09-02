'use strict';
angular.module('sf')
    .controller('FormHistoryCtrl',
        function($scope, caseService, $params) {

            $scope.caseId = $params.caseId;

            $scope.submittedForms = caseService.getSubmittedForms($params.caseId, $params.formId);

            $scope.submittedFormList = caseService.getSubmittedFormList($params.caseId);

            $scope.$watch("selectedSubmittedForm", function(){
                var index = $scope.selectedSubmittedForm;
                if (_.isNumber(index))
                    $scope.submittedForm = caseService.getSubmittedForm($params.caseId, index);
            });

        });
