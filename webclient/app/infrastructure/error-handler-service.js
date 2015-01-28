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
.factory('errorHandlerService', function ($rootScope, $window, $q, $location, tokenService) {
    return function(error) {
      console.log('ERROR -------------', error);
      // TODO - this works for the mycases web application, should it work the same in this application
      if (error.status === 403) {
        $window.location.reload();
      }

      if (error.status === 401) {
        tokenService.clear();
      }

      return $q.reject(error);
    };
  });
