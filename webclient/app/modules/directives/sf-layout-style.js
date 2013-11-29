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
  "use strict";

  sf.directives.directive('sfLayoutStyle', ['$location', function (location) {
    function clearLayoutClasses(element) {
      _.each(['layout-1', 'layout-2'], function(c) {
        element.removeClass(c);
      });
    }

    return {
      restrict:'A',
      link:function (scope, element, attrs, controller) {
        scope.location = location;
        scope.$watch('location.path()', function (newPath) {
          clearLayoutClasses(element);
          if (location.path().indexOf('/projects') === 0) {
            element.addClass('layout-1');
          }
          else {
            element.addClass('layout-2');
          }
        });
      }
    };
  }]);
})();

