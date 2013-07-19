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


  var sfServices = angular.module('sf.services.case', ['sf.services.backend', 'sf.services.navigation', 'sf.models', 'sf.services.forms']);

  sfServices.factory('caseService', ['backendService', 'navigationService', 'SfCase', '$http', 'debounce', 'formMapperService', function (backendService, navigationService, SfCase, $http, debounce, formMapper) {

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
            var index = resource.response.index;

            if (index.dueOn)
              index.dueOnShort = index.dueOn.split("T")[0]

            result.push(index);
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

      getSelectedAttachments: function(projectId, projectType, caseId) {
        return backendService.get({
          specs:caseBase(projectId, projectType, caseId).concat([{resources: 'attachments'}]),
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(link){
              result.push(link);
            });
          }
        });
      },

      deleteAttachment: function(projectId, projectType, caseId, attachmentId, callback) {
        return backendService.postNested(
          caseBase(projectId, projectType, caseId).concat([
            {resources: 'attachments'},
            {'index.links': attachmentId},
            {commands: 'delete'}
            ]),
          {}).then(_.debounce(callback)());
      },

      getSelectedContacts: function(projectId, projectType, caseId) {
        return backendService.get({
          specs:caseBase(projectId, projectType, caseId).concat([{resources: 'contacts'}]),
          onSuccess:function (resource, result) {
            resource.response.index.contacts.forEach(function(item){result.push(item)});
          }
        });
      },

      getPossibleCaseTypes: function(projectId, projectType, caseId) {
        return backendService.get({
          specs:caseBase(projectId, projectType, caseId).concat([
            {resources: 'general'},
            {queries: 'possiblecasetypes'}
            ]),
          onSuccess:function (resource, result) {
            var caseTypeOptions = _.map(resource.response.links, function(link){
              return {name: link.text, value: link.id};
            });

            caseTypeOptions.forEach(function(item){result.push(item)});
          }
        });
      },

      updateSimpleValue: debounce(function(projectId, projectType, caseId, resource, command, property, value, callback) {

        var toSend = {};
        toSend[property] = value;

        return backendService.postNested(
          caseBase(projectId, projectType, caseId).concat([
            {resources: resource},
            {commands: command}
            ]),
          toSend).then(_.debounce(callback)());
      }, 1000),

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

      addViewModelProperties: function(pages){

        _.forEach(pages, function(page){
          _.forEach(page.fields, function(field){
            formMapper.addProperties(field)
          });
        });
      },

      getFormDraft: function(projectId, projectType, caseId, draftId) {
        var that = this;
        return backendService.get({
          specs:caseBase(projectId, projectType, caseId).concat([
            {resources: 'formdrafts'},
            {'index.links': draftId}
            ]),
          onSuccess:function (resource, result) {
            var index = resource.response.index;

            index.draftId = draftId;
            that.addViewModelProperties(index.pages);

            result.push(index);
          }
        });
      },

      getFormDraftFromForm: function(projectId, projectType, caseId, formId) {
        var that = this;
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

              that.addViewModelProperties(index.pages);

              index.draftId = id;

              result.push(index);
            }
          });
          }
        });
      },

      updateField: debounce(function(projectId, projectType, caseId, formId, fieldId, value) {
        return backendService.postNested(
          caseBase(projectId, projectType, caseId).concat([
            {resources: 'formdrafts'},
            {'index.links': formId},
            {commands: 'updatefield'}
            ]),
          {field: fieldId, value: value});
      }, 1000),

      submitForm: function(projectId, projectType, caseId, formId) {
        return backendService.postNested(
          caseBase(projectId, projectType, caseId).concat([
            {resources: 'formdrafts'},
            {'index.links': formId},
            {commands: 'submit'}
            ]),
          {});
      },

      // Conversations
      getConversationMessages: function(projectId, projectType, caseId, conversationId) {
        return backendService.get({
          specs:caseBase(projectId, projectType, caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'messages'},
            ]),
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(item){result.push(item)});
          }
        });
      },
      getConversationParticipants: function(projectId, projectType, caseId, conversationId) {
        return backendService.get({
          specs:caseBase(projectId, projectType, caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'participants'},
            ]),
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(item){result.push(item)});
          }
        });
      },

    }
  }]);

  // https://github.com/angular/angular.js/issues/2690
  sfServices.factory('debounce', ['$timeout', function ($timeout) {
      return function(fn, timeout, apply){ // debounce fn
          timeout = angular.isUndefined(timeout) ? 0 : timeout;
          apply = angular.isUndefined(apply) ? true : apply; // !!default is true! most suitable to my experience
          var nthCall = 0;
          return function(){ // intercepting fn
              var that = this;
              var argz = arguments;
              nthCall++;
              var later = (function(version){
                  return function(){
                      if (version === nthCall){
                          return fn.apply(that, argz);
                      }
                  };
              })(nthCall);
              return $timeout(later, timeout, apply);
          };
      };
  }]);

})();
