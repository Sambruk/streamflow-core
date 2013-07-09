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

  var createFormResponse = {"events":[{"identity":"0092cac0-2293-42ad-be16-87080cbae891-cf","by":"administrator","entity":"37ed3d41-3338-427e-8542-c8ac0e794c2d-2","entityType":"se.streamsource.streamflow.web.domain.entity.caze.CaseEntity","name":"createdFormDraft","on":"2013-07-08T13:14:03.914Z","parameters":"{\"param1\":\"0092cac0-2293-42ad-be16-87080cbae891-ce\"}","usecase":"create","version":"1.8.0.1"},{"identity":"0092cac0-2293-42ad-be16-87080cbae891-d0","by":"administrator","entity":"0092cac0-2293-42ad-be16-87080cbae891-ce","entityType":"se.streamsource.streamflow.web.domain.entity.form.FormDraftEntity","name":"changedFormDraft","on":"2013-07-08T13:14:03.914Z","parameters":"{\"param1\":\"{\\\"description\\\":\\\"Informatörstest\\\",\\\"enteredEmails\\\":null,\\\"form\\\":\\\"6f466f61-0c8b-4171-96bb-7e14b6045743-53\\\",\\\"mailSelectionEnablement\\\":null,\\\"pages\\\":[{\\\"fields\\\":[{\\\"enabled\\\":true,\\\"field\\\":{\\\"datatypeUrl\\\":null,\\\"description\\\":\\\"Välj ett datum\\\",\\\"field\\\":\\\"6f466f61-0c8b-4171-96bb-7e14b6045743-5d\\\",\\\"fieldId\\\":\\\"Date1\\\",\\\"fieldValue\\\":{\\\"_type\\\":\\\"se.streamsource.streamflow.api.administration.form.DateFieldValue\\\",\\\"date\\\":null},\\\"mandatory\\\":false,\\\"note\\\":\\\"\\\",\\\"rule\\\":{\\\"condition\\\":\\\"anyof\\\",\\\"field\\\":\\\"\\\",\\\"values\\\":[],\\\"visibleWhen\\\":true}},\\\"message\\\":null,\\\"value\\\":\\\"2013-07-09T00:00:00.000Z\\\"},{\\\"enabled\\\":true,\\\"field\\\":{\\\"datatypeUrl\\\":null,\\\"description\\\":\\\"Namn\\\",\\\"field\\\":\\\"6f466f61-0c8b-4171-96bb-7e14b6045743-60\\\",\\\"fieldId\\\":\\\"Text2\\\",\\\"fieldValue\\\":{\\\"_type\\\":\\\"se.streamsource.streamflow.api.administration.form.TextFieldValue\\\",\\\"hint\\\":\\\"förnamn, efternamn\\\",\\\"mandatory\\\":false,\\\"regularExpression\\\":null,\\\"width\\\":30},\\\"mandatory\\\":true,\\\"note\\\":\\\"\\\",\\\"rule\\\":{\\\"condition\\\":\\\"anyof\\\",\\\"field\\\":\\\"\\\",\\\"values\\\":[],\\\"visibleWhen\\\":true}},\\\"message\\\":null,\\\"value\\\":\\\"Gustaf, Nilsson Kotte22\\\"}],\\\"page\\\":\\\"6f466f61-0c8b-4171-96bb-7e14b6045743-57\\\",\\\"rule\\\":{\\\"condition\\\":\\\"anyof\\\",\\\"field\\\":\\\"\\\",\\\"values\\\":[],\\\"visibleWhen\\\":true},\\\"title\\\":\\\"1. Första sidan\\\"},{\\\"fields\\\":[{\\\"enabled\\\":true,\\\"field\\\":{\\\"datatypeUrl\\\":null,\\\"description\\\":\\\"Meddelande\\\",\\\"field\\\":\\\"6f466f61-0c8b-4171-96bb-7e14b6045743-66\\\",\\\"fieldId\\\":\\\"TextArea1\\\",\\\"fieldValue\\\":{\\\"_type\\\":\\\"se.streamsource.streamflow.api.administration.form.TextAreaFieldValue\\\",\\\"cols\\\":30,\\\"rows\\\":5},\\\"mandatory\\\":false,\\\"note\\\":\\\"\\\",\\\"rule\\\":{\\\"condition\\\":\\\"anyof\\\",\\\"field\\\":\\\"\\\",\\\"values\\\":[],\\\"visibleWhen\\\":true}},\\\"message\\\":null,\\\"value\\\":\\\"Aoeaoeaoeaoeaoe\\\"},{\\\"enabled\\\":true,\\\"field\\\":{\\\"datatypeUrl\\\":null,\\\"description\\\":\\\"Välj nåt\\\",\\\"field\\\":\\\"6f466f61-0c8b-4171-96bb-7e14b6045743-69\\\",\\\"fieldId\\\":\\\"ComboBox2\\\",\\\"fieldValue\\\":{\\\"_type\\\":\\\"se.streamsource.streamflow.api.administration.form.ComboBoxFieldValue\\\",\\\"values\\\":[\\\"Alt 1\\\",\\\"Alt 2\\\",\\\"Alt 3\\\"]},\\\"mandatory\\\":false,\\\"note\\\":\\\"\\\",\\\"rule\\\":{\\\"condition\\\":\\\"anyof\\\",\\\"field\\\":\\\"\\\",\\\"values\\\":[],\\\"visibleWhen\\\":true}},\\\"message\\\":null,\\\"value\\\":\\\"Alt 1\\\"}],\\\"page\\\":\\\"6f466f61-0c8b-4171-96bb-7e14b6045743-5a\\\",\\\"rule\\\":{\\\"condition\\\":\\\"anyof\\\",\\\"field\\\":\\\"\\\",\\\"values\\\":[],\\\"visibleWhen\\\":true},\\\"title\\\":\\\"2. Andra sidan\\\"}],\\\"secondsignee\\\":null,\\\"signatures\\\":[],\\\"visibilityrules\\\":null}\"}","usecase":"create","version":"1.8.0.1"}],"timestamp":1373289243960};
  var formDraft = {"description":"Informatörstest","enteredEmails":null,"form":"6f466f61-0c8b-4171-96bb-7e14b6045743-53","mailSelectionEnablement":null,"pages":[{"fields":[{"enabled":true,"field":{"datatypeUrl":null,"description":"Välj ett datum","field":"6f466f61-0c8b-4171-96bb-7e14b6045743-5d","fieldId":"Date1","fieldValue":{"_type":"se.streamsource.streamflow.api.administration.form.DateFieldValue","date":null},"mandatory":false,"note":"","rule":{"condition":"anyof","field":"","values":[],"visibleWhen":true}},"message":null,"value":"2013-07-09T00:00:00.000Z"},{"enabled":true,"field":{"datatypeUrl":null,"description":"Namn","field":"6f466f61-0c8b-4171-96bb-7e14b6045743-60","fieldId":"Text2","fieldValue":{"_type":"se.streamsource.streamflow.api.administration.form.TextFieldValue","hint":"förnamn, efternamn","mandatory":false,"regularExpression":null,"width":30},"mandatory":true,"note":"","rule":{"condition":"anyof","field":"","values":[],"visibleWhen":true}},"message":null,"value":"Gustaf, Nilsson Kotte22"}],"page":"6f466f61-0c8b-4171-96bb-7e14b6045743-57","rule":{"condition":"anyof","field":"","values":[],"visibleWhen":true},"title":"1. Första sidan"},{"fields":[{"enabled":true,"field":{"datatypeUrl":null,"description":"Meddelande","field":"6f466f61-0c8b-4171-96bb-7e14b6045743-66","fieldId":"TextArea1","fieldValue":{"_type":"se.streamsource.streamflow.api.administration.form.TextAreaFieldValue","cols":30,"rows":5},"mandatory":false,"note":"","rule":{"condition":"anyof","field":"","values":[],"visibleWhen":true}},"message":null,"value":"Aoeaoeaoeaoeaoe"},{"enabled":true,"field":{"datatypeUrl":null,"description":"Välj nåt","field":"6f466f61-0c8b-4171-96bb-7e14b6045743-69","fieldId":"ComboBox2","fieldValue":{"_type":"se.streamsource.streamflow.api.administration.form.ComboBoxFieldValue","values":["Alt 1","Alt 2","Alt 3"]},"mandatory":false,"note":"","rule":{"condition":"anyof","field":"","values":[],"visibleWhen":true}},"message":null,"value":"Alt 1"}],"page":"6f466f61-0c8b-4171-96bb-7e14b6045743-5a","rule":{"condition":"anyof","field":"","values":[],"visibleWhen":true},"title":"2. Andra sidan"}],"secondsignee":null,"signatures":[],"visibilityrules":false};

  var main = angular.module('sf.controllers', ['sf.services.project', 'sf.services.case']);

  function toggleToolbar($event) {
    $event.preventDefault();
    $('.functions-menu').toggleClass('open');
  }

  main.controller('ProjectListCtrl', ['$scope', 'projectService', function($scope, projectService) {
    $scope.projects = projectService.getAll();
    $scope.toggleToolbar = toggleToolbar;
  }]);

  main.controller('CaseListCtrl', ['$scope', 'projectService', '$routeParams',
                  function($scope, projectService, $params) {
    $scope.cases = projectService.getSelected($params.projectId, $params.projectType);
  }]);

  main.controller('CaseDetailCtrl', ['$scope', 'caseService', '$routeParams',
                  function($scope, caseService, $params){
    console.log('params', $params);
    $scope.case = caseService.getSelected($params.projectId, $params.projectType, $params.caseId);
    $scope.general = caseService.getSelectedGeneral($params.projectId, $params.projectType, $params.caseId);
    $scope.contacts = caseService.getSelectedContacts($params.projectId, $params.projectType, $params.caseId);
    $scope.notes = caseService.getSelectedNotes($params.projectId, $params.projectType, $params.caseId);
    $scope.conversations = caseService.getSelectedConversations($params.projectId, $params.projectType, $params.caseId);
    $scope.possibleForms = caseService.getSelectedPossibleForms($params.projectId, $params.projectType, $params.caseId);

    $scope.selectForm = function(formId){

      caseService.createSelectedForm($params.projectId, $params.projectType, $params.caseId, formId).then(function(response){
        if (response.data.events.length === 0) {
          // TODO!!! Use more general version!!!
          $scope.form = caseService.getFormDraftDebug($params.projectId, $params.projectType, $params.caseId)
        }
        else {
          var draftId = JSON.parse(response.data.events[0].parameters).param1;
          $scope.form = caseService.getFormDraft($params.projectId, $params.projectType, $params.caseId, draftId);
        }
      });
    }

    $scope.fooTest = function(){
      $scope.form = caseService.getFormDraftDebug($params.projectId, $params.projectType, $params.caseId);
    }

    $scope.selectFormPage = function(page){
      $scope.currentFormPage = page;
    }

    window.form = formDraft;
    //$scope.form = formDraft;
  }]);


})();
