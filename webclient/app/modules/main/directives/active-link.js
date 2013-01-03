(function () {
  'use strict';

  // see http://jsfiddle.net/p3ZMR/3/ for another example of doing this

  var main = angular.module('sf.main.directives', []);
  main.directive('activeLink', ['$location', function (location) {
    return {
      restrict:'A',
      link:function (scope, element, attrs, controller) {
        var path = "/" + scope.type.href;
        scope.location = location;
        scope.$watch('location.path()', function (newPath) {
          if (newPath.match(path)) {
            element.addClass('active');
          } else {
            element.removeClass('active');
          }
        });

      }
    }
  }]);
})();