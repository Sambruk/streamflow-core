/*
 *
 * Copyright 2009-2012 Jayway Products AB
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

  var sfForm = angular.module('sf.controllers.form', ['sf.services.case']);

  sfForm.controller('FormCtrl', ['$scope', 'caseService', '$routeParams',
    function($scope, caseService, $params) {

      $scope.possibleForms = caseService.getSelectedPossibleForms($params.projectId, $params.projectType, $params.caseId);

      $scope.selectForm = function(formId){

        // TODO Is there a better way than this?
        $scope.$watch("form", function(){
          setTimeout(function(){
            $scope.$apply(function () {
              if ($scope.form && $scope.form[0]) {
                $scope.currentFormPage = $scope.form[0].enhancedPages[0];
              };
            });
          }, 1000);

        })

        $scope.formMessage = "";

        caseService.createSelectedForm($params.projectId, $params.projectType, $params.caseId, formId).then(function(response){
          if (response.data.events.length === 0) {
            $scope.form = caseService.getFormDraftFromForm($params.projectId, $params.projectType, $params.caseId, formId)
          }
          else {
            var draftId = JSON.parse(response.data.events[0].parameters).param1;
            $scope.form = caseService.getFormDraft($params.projectId, $params.projectType, $params.caseId, draftId);
          }

          $scope.currentFormPage = null;
        });
      }

      $scope.selectFormPage = function(page){
        $scope.currentFormPage = page;
      }

      $scope.submitForm = function(){
        caseService.submitForm($params.projectId, $params.projectType, $params.caseId, $scope.form[0].draftId);
        $scope.formMessage = "Skickat!";

        $scope.form = [];
        $scope.currentFormPage = null;
      }

      $scope.isLastPage = function(){
        return $scope.currentFormPage && $scope.form[0].enhancedPages.indexOf($scope.currentFormPage) === ($scope.form[0].enhancedPages.length - 1);
      }

      $scope.isFirstPage = function(){
        return $scope.currentFormPage && $scope.form[0].enhancedPages.indexOf($scope.currentFormPage) === 0;
      }

      $scope.nextFormPage = function(){
        var index = $scope.form[0].enhancedPages.indexOf($scope.currentFormPage);
        index += 1;
        $scope.currentFormPage = $scope.form[0].enhancedPages[index];
      }

      $scope.previousFormPage = function(){
        var index = $scope.form[0].enhancedPages.indexOf($scope.currentFormPage);
        index -= 1;
        $scope.currentFormPage = $scope.form[0].enhancedPages[index];
      }
    }]);

  sfForm.controller('FormHistoryCtrl', ['$scope', 'caseService', '$routeParams',
    function($scope, caseService, $params) {

      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;
      $scope.caseId = $params.caseId;

      $scope.submittedForms = caseService.getSubmittedForms($params.projectId, $params.projectType, $params.caseId, $params.formId);

      $scope.$watch("selectedSubmittedForm", function(){
        var index = $scope.selectedSubmittedForm;
        if (_.isNumber(index))
          $scope.submittedForm = caseService.getSubmittedForm($params.projectId, $params.projectType, $params.caseId, index);
      });

    }]);

})();
