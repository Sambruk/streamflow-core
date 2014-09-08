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
.directive('sfDatePickerFancy', ['fancyDateService', '$timeout', function (fancyDateService, $timeout) {

    return {
      restrict:'A',
      require: "ngModel",

      link: function (scope, element, attrs, ngModel) {
        var dateRegex = /^\d{4}-\d{2}-\d{2}/,
            //$element = $(element);
            $element = $('#fancy-date-hidden');
            //debugger;
        var $input = $(element).pickadate({
          selectYears: true,
          selectMonths: true,
          format: 'yyyy-mm-dd',
          min: new Date(),
          close: false,
          clear: false,
          onClose: function () {
            var value = $element.val();

            if (dateRegex.test(value)) {
              // Set fancy date without triggering ng-change.
              $element.val(fancyDateService.format(value)).blur();
            }
          }
       });

        scope.$watch('dueOnShortStartValue', function (newVal) {
          if (!!newVal) {
            // run on the next digest
            $timeout(function () {
              var picker = $input.pickadate('picker');
              picker.set('select', new Date(newVal));
              scope.dueOnShort = fancyDateService.format(newVal);
            });
          }
        });
      }
    };
  }]);
