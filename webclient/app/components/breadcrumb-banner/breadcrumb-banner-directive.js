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
      
      $rootScope.$on('breadcrumb-updated', function(event, breadcrumbList) {
        console.log('BREADCRUMB UPDATED');
        console.log(breadcrumbList);
        var newbcItems = getBreadcrumbItems(breadcrumbList);
        console.log(newbcItems);
        scope.breadcrumbList = getBreadcrumbItems(breadcrumbList);
      });

      var getBreadcrumbItems = function(breadcrumbList){
        var bcList = [];
        _.each(breadcrumbList, function(breadcrumbItem){
          _.each(breadcrumbItem, function(val, key){
            if(typeof val === 'string' && val !== undefined){
              val = val.charAt(0).toUpperCase() + val.slice(1);
              bcList.push(val);
            }
          });
        });
        return bcList;
      };
    }
  };
});