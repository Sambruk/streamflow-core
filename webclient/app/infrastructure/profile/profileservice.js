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
.factory('profileService', function(backendService) {
  return {
    getCurrent: function() {
      return backendService.get({
        specs: [{resources: 'account'},
            {resources: 'profile'},
            {queries: 'index'}],
        onSuccess:function (resource, result) {
          result.push(resource.response);
          /*console.log("Result from profile services - result: ");
          console.log(result);
          console.log("Result from profile services - response: ");
          console.log(resource.response);*/
        }
      });
    },
    updateCurrent: function(value) {
      return backendService.postNested(
        [{resources: 'account'},
          {resources: 'profile'},
          {commands: 'update'}
        ],
        value
      );
    },
    changeMessageDeliveryType: function(value) {
      return backendService.postNested(
        [{resources: 'account'},
           {resources: 'profile'},
           {commands: 'changemessagedeliverytype'}],
        value);
    },
    changeMailFooter: function(value) {
      return backendService.postNested(
        [{resources: 'account'},
           {resources: 'profile'},
           {commands: 'changemailfooter'}],
        value);
    },
    changeMarkReadTimeout: function(value) {
      return backendService.postNested(
        [{resources: 'account'},
           {resources: 'profile'},
           {commands: 'changemarkreadtimeout'}],
        value);
    }
  }
 });