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
'use strict';
angular.module('sf')
  .controller('FormCtrl', function($scope, caseService, $routeParams, $rootScope, webformRulesService, $sce) {
    $scope.sidebardata = {};
    
    $scope.caseId = $routeParams.caseId;
    $scope.currentFormId = $routeParams.formId;
    $scope.currentFormDescription;
    $scope.possibleForms = caseService.getSelectedPossibleForms($routeParams.caseId);
    $scope.selectedItems = {};
    $scope.applyRules = webformRulesService.applyRules;

    $scope.showSpinner = {
      form: true
    };

    $scope.$watch('currentFormPage', function(newVal){
      if(!newVal){
        return;
      }
      $scope.reapplyRules();
    });

    $scope.selectForm = function(formId){
      // TODO Is there a better way than this?
      $scope.$watch('form', function(){
        setTimeout(function(){
          $scope.$apply(function () {
            if ($scope.form && $scope.form[0]) {
              $scope.currentFormDescription = $scope.form[0].description;
              $scope.currentFormPage = $scope.form[0].enhancedPages[0];
              $scope.displayField($scope.form[0].enhancedPages);
            };
          });
        }, 1000);
       // $scope.currentFormId = formId;
      });

      $scope.formMessage = "";
      $scope.possibleForm = caseService.getPossibleForm($routeParams.caseId, formId);



      $scope.$watch('possibleForm[0]', function (){
        if (!$scope.possibleForm[0]){
          return;
        }
        if ($scope.possibleForm[0].queries.length !== 0) {
          var form = caseService.getFormDraftFromForm($routeParams.caseId, formId);
          form.promise.then(function(response){
            console.log("RESPONSE FORM")
            console.log(response);
            $scope.form = response;
            $scope.showSpinner.form = false;

          })
          .then(function(){
            if($scope.isLastPage()){

            }
          });
        }
        else {
          caseService.createSelectedForm($routeParams.caseId, formId).then(function(response){
            var draftId = JSON.parse(response.data.events[0].parameters).param1;
            $scope.showSpinner.form = false;
            $scope.possibleForm.invalidate();
            $scope.possibleForm.resolve();
            var form = caseService.getFormDraft($routeParams.caseId, draftId);
            form.promise.then(function(response){
              $scope.form = response;
              $scope.showSpinner.form = false;
            })
            .then(function(){
              $scope.isLastPage = function(){
                return $scope.currentFormPage && $scope.form[0].enhancedPages.indexOf($scope.currentFormPage) === ($scope.form[0].enhancedPages.length - 1); //|| $scope.form[0].enhancedPages.indexOf($scope.currentFormPage) == visiblePages.length;
              }
            });
          });
        }
        $scope.currentFormPage = null;
      });
    }

    $scope.displayField = function(formPage){
      $scope.applyRules(formPage);
    }

    $scope.reapplyRules = function(){
      //alert('reapplying rules');
      $scope.applyRules($scope.form[0].enhancedPages);
    };

    $scope.selectFormPage = function(page){
      console.log("PAGE");
      console.log(page);
      $scope.currentFormPage = page;
    }

    $scope.submitForm = function(){
      caseService.submitForm($routeParams.caseId, $scope.form[0].draftId).then(function(){
        $scope.formMessage = "Skickat!";
        $rootScope.$broadcast('form-submitted');
      });
      //$scope.form = [];
      //$scope.currentFormPage = null;
    }

    var formNavigationMachine = function(index){
      var allowedNav = {
        previousPage: false,
        nextPage: false,
        submitForm: false
      };
      if(index>0){
        allowedNav.previousPage = true;
        if(inputValid){
          if(isLast){
            allowedNav.submitForm = true;
          } else {
            allowedNav.nextPage = true;
          }
        }
      } else {
        if(inputValid){
          if(isLast){
            allowedNav.submitForm = true;
          } else {
            allowedNav.nextPage = true;
          }
        }
      }
      return allowedNav;
    }

    $scope.toggleLastPageTrue = function(val){
      alert(val);
      $scope.forcedLastPage = val;
    }

    $scope.isLastPage = function(){
      return $scope.currentFormPage && $scope.form[0].enhancedPages.indexOf($scope.currentFormPage) === ($scope.form[0].enhancedPages.length - 1); //|| $scope.form[0].enhancedPages.indexOf($scope.currentFormPage) == visiblePages.length;
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
  });