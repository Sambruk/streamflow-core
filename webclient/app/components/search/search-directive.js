'use strict';

angular.module('sf').directive('search', function ($location, navigationService) {
  return {
    restrict: 'E',
    templateUrl: 'components/search/search.html',
    link: function (scope) {
      scope.search = function (query) {
        $location.search({'query': query});
        navigationService.linkTo('/search');
      };
    }
  };
});
