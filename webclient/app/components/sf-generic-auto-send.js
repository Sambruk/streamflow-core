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
.directive('sfGenericAutoSend', ['$parse', '$routeParams', 'caseService', 'formMapperService', function($parse, $routeParams, caseService, formMapper) {
    return {
      require: 'ngModel',
      link: function(scope, element, attr, ngModel) {

        var hasRunAtLeastOnce = false;
        scope.$watch(attr.ngModel, function (newValue, oldValue) {

          if (hasRunAtLeastOnce) {

            // Validation
            if (element.hasClass("ng-invalid")) {
              _.each(element.attr("class").split(" "), function(klass){
                var errorClass = ".error-" + klass
                $(errorClass, element.parent()).show();
              });

              return;
            }

            // Valid input, clear error warnings
            $("[class^=error]", element.parent()).hide();

            var value = formMapper.getValue(newValue, attr);
            caseService.updateField($routeParams.caseId,  scope.$parent.form[0].draftId, attr.name, value);

          }

          hasRunAtLeastOnce = true;
        });
      }
    }
  }]);
