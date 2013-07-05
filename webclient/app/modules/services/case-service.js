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

  sfServices.factory('caseService', ['backendService', 'navigationService', 'SfCase', function (backendService, navigationService, SfCase) {

    return {
      getSelected: function(projectId, projectType, caseId) {
        return backendService.get({
          specs:[
            {resources:'workspacev2'},
            {resources: 'projects'},
            {'index.links': projectId},
            {resources: projectType },
            {queries: 'cases?tq=select+*'},
            {links: caseId}
          ],
          onSuccess:function (resource, result) {
            result.push(new SfCase(resource.response.index));
          }
        });
      },

      getSelectedNotes: function(projectId, projectType, caseId) {
        return backendService.get({
          specs:[
            {resources:'workspacev2'},
            {resources: 'projects'},
            {'index.links': projectId},
            {resources: projectType },
            {queries: 'cases?tq=select+*'},
            {links: caseId},
            {resources: 'note'}
          ],
          onSuccess:function (resource, result) {
            result.push(resource.response.index.note);
          }
        });
      },

      getSelectedGeneral: function(projectId, projectType, caseId) {
        return backendService.get({
          specs:[
            {resources:'workspacev2'},
            {resources: 'projects'},
            {'index.links': projectId},
            {resources: projectType },
            {queries: 'cases?tq=select+*'},
            {links: caseId},
            {resources: 'general'}
          ],
          onSuccess:function (resource, result) {
            result.push(resource.response.index);
          }
        });
      },

      getSelectedContacts: function(projectId, projectType, caseId) {
        return backendService.get({
          specs:[
            {resources:'workspacev2'},
            {resources: 'projects'},
            {'index.links': projectId},
            {resources: projectType },
            {queries: 'cases?tq=select+*'},
            {links: caseId},
            {resources: 'contacts'}
          ],
          onSuccess:function (resource, result) {
            resource.response.index.contacts.forEach(function(item){result.push(item)});
          }
        });

      }

    }
  }]);

})();
