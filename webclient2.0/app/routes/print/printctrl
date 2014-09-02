'use strict';
angular.module('sf')
    .controller('PrintCtrl',
        function(growl, $scope, $params, caseService, $q){

            $scope.caze = caseService.getSelected($params.caseId);
            $scope.general = caseService.getSelectedGeneral($params.caseId);
            $scope.notes = caseService.getSelectedNote($params.caseId);

            $q.all(
                $scope.caze.promise,
                $scope.general.promise,
                $scope.notes.promise
            ).then(function () {
                    // Page has to be rendered first.
                    setTimeout(function () {
                        window.print();
                    });
                });
        });