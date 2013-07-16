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

  sf.directives.directive('sfMultipleSelectAutoSend', ['$parse', '$routeParams', 'caseService', function($parse, $params, caseService) {
    return {
      require: 'ngModel',
      link: function(scope, element, attr, ngModel) {

        var hasRunAtLeastOnce = false;
        scope.$watch(attr.ngModel, function (newValue, oldValue) {
          if (hasRunAtLeastOnce) {

            var espacedValues = _.map(newValue, function(value){
              return value.indexOf(",") !== -1 ? "[" + value + "]" : value;
            });

            var valueToSend = espacedValues.join(", ");
            caseService.updateField($params.projectId, $params.projectType, $params.caseId, scope.$parent.form[0].draftId, attr.name, valueToSend);
          }

          hasRunAtLeastOnce = true;
        });
      }
    }
  }]);

})();
