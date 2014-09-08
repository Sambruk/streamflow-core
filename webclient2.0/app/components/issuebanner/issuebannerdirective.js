'use strict';

angular.module('sf')
.directive('issuebanner', function(profileService, $rootScope){
  return {
    restrict: 'E',
    templateUrl: 'components/issuebanner/issuebanner.html',
    scope: {
      breadcrumblist: '=?'
    },
    link: function(scope) {
      scope.breadcrumbList;
      $rootScope.$on('breadcrumb-updated', function(scope, breadcrumbList) {
        var breadcrumb = [];
        var a = _.reduce(breadcrumbList, function(a,b){
          return a;
        }, []);
        console.log(a);
        scope.breadcrumbList = getBreadcrumbItems(breadcrumbList);
      });

      scope.$watch('breadcrumbList', function(newVal){
        scope.breadcrumbList = newVal;
        console.log(newVal);
      });

      var getBreadcrumbItems = function(breadcrumbList){
        var bcList = [];
        _.each(breadcrumbList, function(breadcrumbItem){
          _.each(breadcrumbItem, function(val, key){
            if(typeof val === 'string' && val !== undefined){
              bcList.push(val);
            }
          });
        });
        return bcList;
      };
    }
  }
});