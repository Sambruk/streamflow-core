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
.factory('perspectiveService', function (backendService, navigationService, SfCase, $http, debounce, caseService) {

    var perspectiveBase = function(){
     return [
       {resources: caseService.getWorkspace()},
       {resources:'perspectives'}
      ];
    };

    return {
      getPerspectives: function() {
        return backendService.get({
          specs: perspectiveBase(),
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(item){
              result.push(item);
            });
          }
        });
      }

/*      getMyCases: function() {
        return backendService.get({
          specs: perspectiveBase().concat([
            {queries: 'skapadav:mig'}
          ]),
          onSuccess:function (resource, result) {
            resource.response.links.forEach(function(item){result.push(item)});
          }
        });
      },

      getMyLatestCases: function() {
        return backendService.get({
          specs: perspectiveBase().concat([
            {queries: 'skapadav:mig'},
            {createdOnPeriod: 'six_months'}
          ]),
          onSuccess:function (resource, result) {
            resource.response.links.forEach(function(item){result.push(item)});
          }
        });
      },

      getTodaysCases: function() {
        return backendService.get({
          specs: perspectiveBase().concat([
            {createdOnPeriod: 'one_day'}
           ]),
          onSuccess:function (resource, result) {
            resource.response.links.forEach(function(item){result.push(item)});
          }
        });
      }
      */
    };
});
