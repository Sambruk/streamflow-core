'use strict';

angular.module('sf')
.directive('breadcrumbbanner', function(profileService, $rootScope){
  return {
    restrict: 'E',
    templateUrl: 'components/breadcrumb-banner/breadcrumb-banner.html',
    scope: {
      breadcrumblist: '=?'
    },
    link: function(scope) {
      scope.breadcrumbList;
      scope.$watch('breadcrumbList', function(newVal){
        if(!newVal){
          return;
        }
        scope.breadcrumbList = newVal;   
      });
      
      scope.$on('breadcrumb-updated', function(event, breadcrumbList) {
        scope.breadcrumbList = getBreadcrumbItems(breadcrumbList);
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
  };
});