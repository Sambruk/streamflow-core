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

  sf.directives.directive('sfUpdateOnBlur', ['$parse', function($parse) {
    return function(scope, element, attr) {
      var fn = $parse(attr['sfUpdateOnBlur']);
      var form = scope[element.closest('form').attr('name')];

      var successCallback = function (element){
        element.parent().addClass('saved');
        setPristine(form, element);
        $("[class^=error]", element.parent()).hide();
      };

      var errorCallback = function (element){
        element.parent().addClass('error');
      };

      element.bind('blur', function(event) {
        if (!element.hasClass("ng-invalid") && element.hasClass("ng-dirty")) {

          scope.$apply(function() {
            fn(scope, {$event:event, $success:successCallback, $error:errorCallback});
          });
        }
        else {
          _.each(element.attr("class").split(" "), function(klass){
            var errorClass = ".error-" + klass
            $(errorClass, element.parent()).show();
          });
        }
      });

      var setPristine = function(form, element){
        if (form.$setPristine){//only supported from v1.1.x
          form.$setPristine();
        } else {
          form.$dirty = false;
          form.$pristine = true;
          element.$dirty = false;
          element.$pristine = true;
        }
      };

      if (element[0].type === 'text' || element[0].type === 'textarea') {
        var resetFieldState = function (value){
          $(this).parent().removeClass('saved');
          $("[class^=error]", $(this).parent()).hide();
        };

        var options = {
          callback: resetFieldState,
          wait: 0,
          highlight: false,
          captureLength: 1
        }

        element.typeWatch( options );
      }
    }
  }]);

})();
