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


  var sfServices = angular.module('sf.services.project', ['sf.services.backend', 'sf.services.navigation']);

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
        var self = this;
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
              result.push(self.createCase(item));
            });
          }
        });
      },

      createCase: function(model) {
        var href = navigationService.caseHref(model.id);
        var o = _.extend(model, {href: href}, this.caseMixin);
        return o;
      },

      caseMixin: {
        overdueDays: function() {
          var oneDay = 24*60*60*1000;
          var now = new Date();
          var dueDate = new Date(this.dueDate);
          var diff = Math.round((now.getTime() - dueDate.getTime())/(oneDay));
          return diff > 0 ? diff : 0;
        },

        overdueStatus: function() {
          if (!this.dueDate) return 'unset';
          return this.overdueDays() > 0 ? 'overdue' : 'set';
        }
      }

    };
  }]);

})();
//http://localhost:3501/api/workspace/cases/0f90c758-cf19-4cce-abcd-94f0f3373fce-28/
