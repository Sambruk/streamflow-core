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

 // https://github.com/angular/angular.js/issues/2690
angular.module('sf')
.factory('debounce', function ($timeout) {
      return function(fn, timeout, apply){ // debounce fn
          timeout = angular.isUndefined(timeout) ? 0 : timeout;
          apply = angular.isUndefined(apply) ? true : apply; // !!default is true! most suitable to my experience
          var nthCall = 0;
          return function(){ // intercepting fn
              var that = this;
              var argz = arguments;
              nthCall++;
              var later = (function(version){
                  return function(){
                      if (version === nthCall){
                          return fn.apply(that, argz);
                      }
                  };
              })(nthCall);
              return $timeout(later, timeout, apply);
          };
      };
  });