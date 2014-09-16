'use strict';

angular.module('sf')
.directive('issuelist', function(){
  return {
    restrict: 'E',
    templateUrl: 'components/issuelist/issuelist.html',
    scope: {
      items: '=?'
    },
    link: function(scope){
      scope.currentCases = scope.items;
      scope.$watch('items', function(newVal){
        if(newVal){
          scope.currentCases = newVal;
        }
      });
    }
  };
});