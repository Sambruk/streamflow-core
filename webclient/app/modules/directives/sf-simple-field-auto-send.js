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

  sf.directives.directive('sfSimpleFieldAutoSend', ['$parse', '$routeParams', 'caseService', function($parse, $params, caseService) {
    return {
      require: 'ngModel',
      link: function(scope, element, attr, ngModel) {

        var hasRunAtLeastOnce = false;
        scope.$watch(attr.ngModel, function (newValue, oldValue) {

          if (newValue === oldValue)
            return;

          if (hasRunAtLeastOnce) {

            var resource = attr.resource;
            var command = attr.command;
            var name = attr.name;
            var value = newValue;

            caseService.updateSimpleValue($params.projectId, $params.projectType, $params.caseId, resource, command, name, value);
          }

          hasRunAtLeastOnce = true;
        });
      }
    }
  }]);

})();
