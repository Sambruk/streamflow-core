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
  .controller('FormCtrl', function($scope, caseService, $routeParams, $rootScope, webformRulesService, $sce, navigationService, fileService, httpService) {
    $scope.sidebardata = {};

    $scope.caseId = $routeParams.caseId;
    $scope.currentFormId = $routeParams.formId;
    $scope.currentFormDescription = '';
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
            }
          });
        }, 1000);
      });

      $scope.formMessage = '';
      $scope.possibleForm = caseService.getPossibleForm($routeParams.caseId, formId);

      $scope.$watch('possibleForm[0]', function (){
        if (!$scope.possibleForm[0]){
          return;
        }
        if ($scope.possibleForm[0].queries.length !== 0) {
          caseService.getFormDraftId($routeParams.caseId, formId).promise.then(function(response){
            $scope.formDraftId = response[0].id;
          }).then(function(){
            var form = caseService.getFormDraftFromForm($routeParams.caseId, $scope.formDraftId);
            form.promise.then(function(response){
              $scope.form = response;
              $scope.formAttachments = [];
              $scope.showSpinner.form = false;

              $scope.form[0].enhancedPages.forEach(function(pages){
                pages.fields.forEach(function(field){

                  if(field.field.fieldValue._type === 'se.streamsource.streamflow.api.administration.form.AttachmentFieldValue'){
                    var name = null;
                    var id = null;


										if(field.value){
											var jsonParse = JSON.parse(field.value);
											name = jsonParse.name;
											id = jsonParse.attachment;
										}

                    var attachment = {
                    	name: name,
                    	id: id,
                    	fieldId: field.field.field
                    };

                    $scope.formAttachments.push(attachment);
									}
                });
              });
            })
            .then(function(){
              if($scope.isLastPage()){
                $scope.form.invalidate();
                $scope.form.resolve();
              }
            });
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
                return $scope.currentFormPage && $scope.form[0].enhancedPages.indexOf($scope.currentFormPage) === ($scope.form[0].enhancedPages.length - 1); //|| $scope.form[0].enhancedPages.indexOf($scope.currentFormPage) === visiblePages.length;
              };
            });
          });
        }
        $scope.currentFormPage = null;
      });
    };

    $scope.displayField = function(formPage){
      $scope.applyRules(formPage);
    };

    $scope.reapplyRules = function(){
      $scope.applyRules($scope.form[0].enhancedPages);
    };

    $scope.selectFormPage = function(page){
      $scope.currentFormPage = page;
    };

    $scope.submitForm = function(){
      caseService.submitForm($routeParams.caseId, $scope.form[0].draftId).then(function(){
        $scope.formMessage = 'Skickat!';

        $rootScope.$broadcast('form-submitted');

        $scope.form = [];
        $scope.currentFormPage = null;
      });
    };

    $scope.deleteFormDraftAttachment = function(fieldId){
			var attachment = _.find($scope.formAttachments, function(attachment) {
        return attachment.fieldId === fieldId;
      });

      caseService.deleteFormDraftAttachment($routeParams.caseId, $scope.formDraftId, attachment.id).then(function(){
      	$scope.formAttachments.forEach(function(attachment, index){
	       	if($scope.formAttachments[index].fieldId === fieldId){
	       		$scope.formAttachments[index].name = null;
	       		$scope.formAttachments[index].id = null;
	       	}
  			});
        caseService.updateField($routeParams.caseId, $scope.formDraftId, fieldId, null);
      });
    };

    $scope.onFormDraftFileSelect = function($files, fieldId){
      var url = httpService.apiUrl + 'workspacev2/cases/'+$routeParams.caseId+'/formdrafts/'+$scope.formDraftId +'/formattachments/createformattachment';

      fileService.uploadFile($files[0], url).then(function(data){
        return JSON.parse(data.data.events[0].parameters).param1;
      }).then(function(attachmentId){
        caseService.updateFormDraftAttachmentField($routeParams.caseId, $scope.formDraftId, $files[0].name, attachmentId, fieldId).then(function(){

  	    	$scope.formAttachments.forEach(function(attachment, index){
  	    		if($scope.formAttachments[index].fieldId === fieldId){
  	    			$scope.formAttachments[index].name = $files[0].name;
  	    			$scope.formAttachments[index].id = attachmentId;
  	    		}
		    	});
        });
      });
    };

    $scope.toggleLastPageTrue = function(val){
      $scope.forcedLastPage = val;
    };

    $scope.isLastPage = function(){
      if($scope.form && $scope.form[0]){
        return $scope.currentFormPage && $scope.form[0].enhancedPages.indexOf($scope.currentFormPage) === ($scope.form[0].enhancedPages.length - 1);
      }
      return false;
    };



    $scope.isFirstPage = function(){
      if($scope.form && $scope.form[0]){
        return $scope.currentFormPage && $scope.form[0].enhancedPages.indexOf($scope.currentFormPage) === 0;
     }
     return false;
    };

    $scope.nextFormPage = function(){
      var index = $scope.form[0].enhancedPages.indexOf($scope.currentFormPage);
      index += 1;
      $scope.currentFormPage = $scope.form[0].enhancedPages[index];
    };

    $scope.previousFormPage = function(){
      var index = $scope.form[0].enhancedPages.indexOf($scope.currentFormPage);
      index -= 1;
      $scope.currentFormPage = $scope.form[0].enhancedPages[index];
    };
  });
