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

angular.module('sf').directive('sfDatePickerFancy', function (fancyDateService) {
  return {
    restrict:'A',
    require: 'ngModel',
    link: function (scope, element) {
      var $element = $(element);

      function setDateWithoutTriggeringChange(el, date) {
        el.val(fancyDateService.format(date)).blur();
      }

      $element.pickadate({
        selectYears: true,
        selectMonths: true,
        format: 'yyyy-mm-dd',
        min: new Date(),
        close: false,
        clear: false,
        onStart: function () {
          scope.$watch('dueOnShortStartValue', function (value) {
            setDateWithoutTriggeringChange($element, value);
          });
        }
      });
    }
  };
});

