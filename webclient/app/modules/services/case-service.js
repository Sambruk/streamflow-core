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
(function () {
  'use strict';


  var sfServices = angular.module('sf.services.case', ['sf.services.backend', 'sf.services.navigation', 'sf.models']);

  sfServices.factory('caseService', ['backendService', 'navigationService', 'SfCase', "$http", function (backendService, navigationService, SfCase, $http) {

    var caseBase = function(projectId, projectType, caseId){
     return [
        {resources:'workspacev2'},
        {resources: 'projects'},
        {'index.links': projectId},
        {resources: projectType },
        {queries: 'cases?tq=select+*'},
        {links: caseId}
      ];
    };

    return {
      getSelected: function(projectId, projectType, caseId) {
        return backendService.get({
          specs: caseBase(projectId, projectType, caseId),
          onSuccess:function (resource, result) {
            result.push(new SfCase(resource.response.index));
          }
        });
      },

      getSelectedNotes: function(projectId, projectType, caseId) {
        return backendService.get({
          specs:caseBase(projectId, projectType, caseId).concat([{resources: 'note'}]),
          onSuccess:function (resource, result) {
            result.push(resource.response.index.note);
          }
        });
      },

      getSelectedGeneral: function(projectId, projectType, caseId) {
        return backendService.get({
          specs:caseBase(projectId, projectType, caseId).concat([{resources: 'general'}]),
          onSuccess:function (resource, result) {
            result.push(resource.response.index);
          }
        });
      },

      getSelectedConversations: function(projectId, projectType, caseId) {
        return backendService.get({
          specs:caseBase(projectId, projectType, caseId).concat([{resources: 'conversations'}]),
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(link){
              result.push(link);
            });
          }
        });
      },

      getSelectedContacts: function(projectId, projectType, caseId) {
        return backendService.get({
          specs:caseBase(projectId, projectType, caseId).concat([{resources: 'contacts'}]),
          onSuccess:function (resource, result) {
            resource.response.index.contacts.forEach(function(item){result.push(item)});
          }
        });
      },

      getSelectedPossibleForms: function(projectId, projectType, caseId) {
        return backendService.get({
          specs:caseBase(projectId, projectType, caseId).concat([{resources: 'possibleforms'}]),
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(item){result.push(item)});
          }
        });
      },

      createSelectedForm: function(projectId, projectType, caseId, formId) {
        return backendService.postNested(
          caseBase(projectId, projectType, caseId).concat([
            {resources: 'possibleforms'},
            {'index.links': formId.replace("/", "")},
            {commands: 'create'}
            ]),
          {});
      },

      getFormDraft: function(projectId, projectType, caseId, draftId) {
        return backendService.get({
          specs:caseBase(projectId, projectType, caseId).concat([
            {resources: 'formdrafts'},
            {'index.links': draftId}
            ]),
          onSuccess:function (resource, result) {
            var index = resource.response.index;

            index.draftId = draftId;

            // Fix for ng-options et al
            _.forEach(index.pages, function(page){
              _.forEach(page.fields, function(field){
                var options = _.map(field.field.fieldValue.values,
                  function(value){ return {name: value, value: value}
                });

                field.field.fieldValue.options = options;
              });
            });

            result.push(index);
          }
        });
      },

      getFormDraftFromForm: function(projectId, projectType, caseId, formId) {
        return backendService.get({
          specs:caseBase(projectId, projectType, caseId).concat([
            {resources: 'possibleforms'},
            {'index.links': formId.replace("/", "")},
            {queries: 'formdraft'}
            ]),
          onSuccess:function (resource, result) {
            var id = resource.response.id;

            return backendService.get({
            specs:caseBase(projectId, projectType, caseId).concat([
              {resources: 'formdrafts'},
              {'index.links': id}
              ]),
            onSuccess:function (resource) {
              var index = resource.response.index;

              // Fix for ng-options et al
              _.forEach(index.pages, function(page){
                _.forEach(page.fields, function(field){
                  var options = _.map(field.field.fieldValue.values, function(value){
                    return {name: value, value: value}
                  });
                  if (field.field.fieldValue._type === "se.streamsource.streamflow.api.administration.form.CheckboxesFieldValue") {
                    var checkings = _.map(field.field.fieldValue.values, function(value){
                      return {name: value, checked: field.value && field.value.indexOf(value) != -1};
                    });

                    field.field.fieldValue.checkings = checkings;
                  }

                  if (field.field.fieldValue._type === "se.streamsource.streamflow.api.administration.form.OpenSelectionFieldValue") {

                    field.field.fieldValue.extendedValues = _.map(field.field.fieldValue.values, function(value){
                      return {
                        value: value,
                        display: value
                      }
                    });

                    var value;
                    if (field.field.fieldValue.values.indexOf(field.value) == -1) {
                      value = field.value
                    }

                    field.field.fieldValue.extendedValues.push({
                      value: value,
                      display: field.field.fieldValue.openSelectionName
                    });
                  }

                  field.field.fieldValue.options = options;
                });
              });

              index.draftId = id;

              result.push(index);
            }
          });
          }
        });
      },

      updateField: function(projectId, projectType, caseId, formId, fieldId, value) {
        return backendService.postNested(
          caseBase(projectId, projectType, caseId).concat([
            {resources: 'formdrafts'},
            {'index.links': formId},
            {commands: 'updatefield'}
            ]),
          {field: fieldId, value: value});
      },

      submitForm: function(projectId, projectType, caseId, formId) {
        return backendService.postNested(
          caseBase(projectId, projectType, caseId).concat([
            {resources: 'formdrafts'},
            {'index.links': formId},
            {commands: 'submit'}
            ]),
          {});
      }
    }
  }]);

})();
