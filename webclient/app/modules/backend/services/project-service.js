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


  var sfServices = angular.module('sf.backend.services.project', ['sf.backend.services.backend', 'sf.backend.services.navigation']);

  sfServices.factory('projectService', ['backendService', 'navigationService', function (backendService, navigationService) {

    return {
      getAll:function () {
        return backendService.get({
          specs:[
            {resources:'workspace'},
            {resources: 'projects'}
          ],
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(item){
              // TODO maybe filter rel='project'
              result.push({text: item.text, types: [{name: 'inbox', href: item.href + 'inbox'}, {name: 'assignments', href: item.href + 'assignments'}]});
            });
          }
        });
      },
      //http://localhost:3501/b35873ba-4007-40ac-9936-975eab38395a-3f/inbox/f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-0
      getSelected: function() {
        return backendService.get({
          specs:[
            {resources:'workspace'},
            {resources: 'projects'},
            {'index.links': navigationService.projectId},
            {resources: navigationService.caseType },
            {queries: 'cases'}
          ],
          onSuccess:function (resource, result) {
            resource.response.links.forEach(function(item){
              var href = navigationService.caseHref(item.id);
              console.log(item)
              result.push({id: item.caseId, caseType: item.caseType, text: item.text, href: href, project: item.project, creationDate: item.creationDate});
            });
          }
        });
      }
    }
  }]);

})();
//http://localhost:3501/api/workspace/cases/0f90c758-cf19-4cce-abcd-94f0f3373fce-28/
