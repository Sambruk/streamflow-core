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
(function () {
  'use strict';

  sf.directives.directive('sfUpdateOnBlur', ['$parse', function($parse) {
    return function(scope, element, attr) {
      var fn = $parse(attr['sfUpdateOnBlur']);
      var form = scope[element.closest('form').attr('name')];


      var successCallback = function (element){

        if (element[0].type === 'select-one') {
          element.parent().addClass('saved saved-select');
        }else{
          element.parent().addClass('saved');
        }
        if(element.parent().hasClass('error')){
          element.parent().removeClass('error');
        }

        if ($("#createContactForm div").not('.error')&&$("#contact-name").val()!=""){
            $('#contact-submit-button').attr('disabled', false);
        }
        else{
            $('#contact-submit-button').attr('disabled', true);
        }

        //Talk of removing the saved icon after a while, whis coule be one way.
        //Looked at fading it in and out however you cannot fade the "content" in a :after pseudo element
        //it triggers a remove of the last one and add of a new element and that can not be transitioned
        //setTimeout(removeIt,2000);

        setPristine(form, element);
        $("[class^=error]", element.parent()).hide();
      };

      function removeIt(){
        element.parent().removeClass('saved');
      }

      var errorCallback = function (element){
        element.parent().addClass('error');
        $('#contact-submit-button').attr('disabled', true);

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

      $("select").change(function () {
          $(this).parent().removeClass('saved');
          $(this).parent().removeClass('saved-select');
          $("[class^=error]", $(this).parent()).hide()
      })

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
