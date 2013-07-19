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


  var sfServices = angular.module('sf.services.project', ['sf.services.backend', 'sf.services.navigation', 'sf.models']);

  sfServices.factory(
    'projectService',
    ['backendService', 'navigationService', 'SfCase',
      function (backendService, navigationService, SfCase) {

    return {
      getAll:function () {
        return backendService.get({
          specs:[
            {resources:'workspacev2'}
            //{resources: 'projects'}
          ],
          onSuccess:function (resource, result) {

            var projects = _.chain(resource.response.index.links)
              .filter(function(item){
                return item.rel === 'inbox' || item.rel === 'assignments'
              })
              .groupBy('text')
              .value();

            _.forEach(projects, function(values, key){

              var types = _.map(values, function(item){
                return {name: item.rel, href: item.href};
              });

              result.push({text: key, types: types});
            });
          }
        });
      },
      //http://localhost:3501/b35873ba-4007-40ac-9936-975eab38395a-3f/inbox/f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-0
      getSelected: function(projectId, projectType) {
        var self = this;
        return backendService.get({
          specs:[
            {resources:'workspacev2'},
            {resources: 'projects'},
            {'index.links': projectId},
            {resources: projectType},
            {queries: 'cases?tq=select+*'}
          ],
          onSuccess:function (resource, result) {
            resource.response.links.forEach(function(item){
              result.push(self.sfCaseFactory(item));
            });
          }
        });
      },

      sfCaseFactory: function(model) {
        var href = navigationService.caseHref(model.id);
        var o = new SfCase(model, href);
        return o;
      },

      createCase: function(projectId, projectType) {

        return backendService.postNested(
          [
            {resources:'workspacev2'},
            {resources: 'projects'},
            {'index.links': projectId},
            {resources: projectType },
            {commands: 'createcase'}
          ],
          {});
      },


    };
  }]);

})();
