/*
 *
 * Copyright 2009-2014 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function() {
  'use strict';

  var sfCase = angular.module('sf.controllers.case', ['angular-growl','sf.services.case', 'sf.services.navigation', 'sf.services.perspective', 'sf.services.project']);

  sfCase.config(['growlProvider', function(growlProvider) {
    growlProvider.globalTimeToLive(5000);
  }]);

  sfCase.controller('CaseOverviewCtrl', ['$scope', '$routeParams', 'perspectiveService', 'navigationService',
    function($scope, $params, perspectiveService, navigationService) {
      $scope.perspectives = perspectiveService.getPerspectives();
      /*$scope.myCases = perspectiveService.getMyCases();
      $scope.myLatestCases = perspectiveService.getMyLatestCases();
      $scope.myTodaysCases = perspectiveService.getTodaysCases();*/
    }]);

  sfCase.controller('CaseSearchCtrl', ['$scope', '$routeParams', 'caseService', 'navigationService',
    function($scope, $params, caseService, navigationService) {

    }]);

  sfCase.controller('CaseListCtrl', ['growl','$scope', '$routeParams', 'projectService','$rootScope', 
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
  }]);

  sfCase.controller('CaseDetailCtrl', ['growl', '$scope', '$timeout', '$routeParams', 'caseService', 'navigationService', 'projectService', 'profileService', '$rootScope',
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
  }]);

  sfCase.controller('PrintCtrl', ['growl','$scope', '$routeParams', 'caseService', '$q',
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
  }]);

  sfCase.controller('CaseEditCtrl', ['growl','$scope', '$rootScope', '$routeParams', 'caseService', 'navigationService',
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

    }]);

})();


