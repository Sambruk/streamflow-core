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
            result.push(resource.response.index);
          }
        });
      },

      // For development only!!! API needs to be fixed to support more general case
      getFormDraftDebug: function(projectId, projectType, caseId){
        return $http({
          method: 'GET',
          url: 'https://test.sf.streamsource.se/streamflow/workspacev2/cases/37ed3d41-3338-427e-8542-c8ac0e794c2d-2/formdrafts/.json'
        }).then(function(response){
          return backendService.get({
            specs:caseBase(projectId, projectType, caseId).concat([
              {resources: 'formdrafts'},
              {'index.links': response.data.index.links[0].id}
              ]),
            onSuccess:function (resource, result) {
              result.push(resource.response.index);
            }
          });
        });
      }

    }
  }]);

})();
