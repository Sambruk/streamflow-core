'use strict';
angular.module('sf')
    .controller('CaseEditCtrl',
        function(growl, $scope, $rootScope, $params, caseService, navigationService) {

            $scope.caze = caseService.getSelected($params.caseId);
            $scope.general = caseService.getSelectedGeneral($params.caseId);

            $scope.notes = caseService.getSelectedNote($params.caseId);
            $scope.cachedNote = caseService.getSelectedNote($params.caseId);


            $scope.addNote = function($event, $success, $error){
                $event.preventDefault();
                if ($scope.notes[0].note !== $scope.cachedNote[0].note || $event.target.value == $scope.caze[0].text)
                    caseService.addNote($params.caseId, $scope.notes[0]).then(function(){
                            $success($($event.target));

                            $rootScope.$broadcast('note-changed');
                        }, function (error){
                            $error($error($event.target));
                        }
                    );
            }

            $scope.changeCaseDescription = function($event, $success, $error){
                $event.preventDefault();

                caseService.changeCaseDescription($params.caseId, $scope.caze[0].text).then(function(){
                        $success($($event.target));

                        $rootScope.$broadcast('casedescription-changed');
                    }, function(error) {
                        $error($error($event.target));
                    }
                );
            }

        });