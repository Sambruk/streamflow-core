'use strict';
angular.module('sf')
    .controller('CaseDetailCtrl',
        function(growl, $scope, $timeout, $params, caseService, navigationService, projectService, profileService, $rootScope){

            $scope.caze = caseService.getSelected($params.caseId);
            $scope.general = caseService.getSelectedGeneral($params.caseId);
            $scope.notes = caseService.getSelectedNote($params.caseId);

            $scope.commands = caseService.getSelectedCommands($params.caseId);
            $scope.profile = profileService.getCurrent();

            $scope.$watch('caze[0]', function(){
                if ($scope.caze.length === 1){
                    $scope.caseListUrl = navigationService.caseListHrefFromCase($scope.caze);
                    $rootScope.$broadcast('breadcrumb-updated', [{projectId: $scope.caze[0].owner}, {projectType: $scope.caze[0].listType}, {caseId: $scope.caze[0].caseId}]);
                }
            });

            $scope.$on('case-created', function() {
                $scope.caze.invalidate();
            });

            $scope.$on('case-changed', function() {
                $scope.caze.invalidate();
                $scope.caze.resolve();
            });

            $scope.$on('note-changed', function() {
                $scope.notes = caseService.getSelectedNote($params.caseId);
            });

            $scope.$on('noteDescription-changed', function() {
                $scope.caze = caseService.getSelected($params.caseId);
            })

            /**
             * ERROR HANDLER
             **/
                //TODO: Implement error handler listener on other controllers where needed
            $scope.errorHandler = function(){
                var bcMessage = caseService.getMessage();
                if(bcMessage === 200)  {
                    //growl.addSuccessMessage('successMessage');
                }else {
                    growl.warning('errorMessage');
                }
            };

            //error-handler
            $scope.$on('httpRequestInitiated', $scope.errorHandler);

            // Mark the case as Read after the ammount of time selected in profile.
            // TODO <before uncomment>. Find a way to update possible commands after post.
            /*$scope.$watch("commands[0] + profile[0]", function(){
             var commands = $scope.commands;
             var profile = $scope.profile[0];

             $scope.canRead = _.any(commands, function(command){
             return command.rel === "read";
             });

             if ($scope.canRead) {
             $timeout(function() {
             caseService.Read($params.projectId, $params.projectType, $params.caseId);
             }, profile.markReadTimeout * 1000)

             }
             });*/
        });
