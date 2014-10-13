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
.directive('sfRadioExtendedTextField', ['$parse', '$routeParams', '$timeout', 'caseService', function($parse, $params, $timeout, caseService) {
    return {
      require: 'ngModel',
      link: function(scope, element, attr, ngModel) {

        var hasRunAtLeastOnce = false;
        scope.$watch(attr.ngModel, function (newValue, oldValue) {

          if (hasRunAtLeastOnce) {
            var extendedOptions = $parse(attr['sfRadioExtendedTextField'])();
            var isOther = !_.any(extendedOptions, function(option){ return option.value === newValue })
            if (isOther) {

              var input = _.last($("input[type=radio]", $(element).parent().parent()));
              $(input).val(newValue);
              $timeout(function(){
                $(input).prop('checked',true);
              });

              caseService.updateField($params.caseId, scope.$parent.form[0].draftId, attr.name, newValue);
            }

          }

          hasRunAtLeastOnce = true;
        });
      }
    }
  }]);
