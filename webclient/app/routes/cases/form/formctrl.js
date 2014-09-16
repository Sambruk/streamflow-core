'use strict';
angular.module('sf')
  .controller('FormCtrl', function($scope, caseService, $routeParams) {
    $scope.sidebardata = {};
    $scope.caseId = $routeParams.caseId;
    $scope.currentFormId = $routeParams.formId;
    $scope.currentFormDescription;
    $scope.possibleForms = caseService.getSelectedPossibleForms($routeParams.caseId);

    $scope.selectForm = function(formId){
      // TODO Is there a better way than this?
      $scope.$watch('form', function(){
        setTimeout(function(){
          $scope.$apply(function () {
            if ($scope.form && $scope.form[0]) {
              $scope.currentFormDescription = $scope.form[0].description;
              $scope.currentFormPage = $scope.form[0].enhancedPages[0];
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
          $scope.form = caseService.getFormDraftFromForm($routeParams.caseId, formId);
        }
        else {
          caseService.createSelectedForm($routeParams.caseId, formId).then(function(response){
            var draftId = JSON.parse(response.data.events[0].parameters).param1;
            $scope.form = caseService.getFormDraft($routeParams.caseId, draftId);
          });
        }
        $scope.currentFormPage = null;
      });
    }

    $scope.selectFormPage = function(page){
      $scope.currentFormPage = page;
    }

    $scope.submitForm = function(){
      caseService.submitForm($routeParams.caseId, $scope.form[0].draftId);
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
  });