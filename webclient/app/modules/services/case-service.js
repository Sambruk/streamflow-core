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


  var sfServices = angular.module('sf.services.case', ['sf.services.backend', 'sf.services.navigation']);

  sfServices.factory('caseService', ['backendService', 'navigationService', function (backendService, navigationService) {

    return {
      getSelected: function() {
        return backendService.get({
          specs:[
            {resources:'workspace'},
            {resources: 'projects'},
            {'index.links': navigationService.projectId},
            {resources: navigationService.caseType },
            {queries: 'cases'},
            {links: navigationService.caseId}
          ],
          onSuccess:function (resource, result) {
            result.index = resource.response.index;
          }
        });
      },

      getSelectedContacts: function() {
        return backendService.get({
          specs:[
            {resources:'workspace'},
            {resources: 'projects'},
            {'index.links': navigationService.projectId},
            {resources: navigationService.caseType },
            {queries: 'cases'},
            {links: navigationService.caseId},
            {resources: 'contacts'}
          ],
          onSuccess:function (resource, result) {
            //
            resource.response.index.contacts.forEach(function(item){result.push(item)});
          }
        });

      }

    }
  }]);

})();
