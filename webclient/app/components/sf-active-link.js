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

  // see http://jsfiddle.net/p3ZMR/3/ for another example of doing this
  angular.module('sf')
  .directive('sfActiveLink', ['$location', function (location) {
    return {
      restrict:'A',
      link: function (scope, element, attrs, controller) {
        scope.$watch('location.path()', function (newPath) {
          scope.location = location;
          var href = attrs.href.substring(1);
          if (!newPath) return;
          if (newPath.match(href))
            element.addClass('sel');
          else
            element.removeClass('sel');
        });
      }
    };
  }]);
