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
      $rootScope.$on('breadcrumb-updated', function(scope, breadcrumbList) {
        var breadcrumb = [];
        for (var i=0;i<breadcrumbList.length;i++)
        {
          if(breadcrumbList[i].projectId){ 
            breadcrumb.push(breadcrumbList[i].projectId);
          }if(breadcrumbList[i].projectType){ 
            // translate didn't work
            if(breadcrumbList[i].projectType == 'inbox'){
              breadcrumb.push('Inkorg');
            }if(breadcrumbList[i].projectType == 'assignments'){
              breadcrumb.push('Mina ärenden');
            }
          }if(breadcrumbList[i].caseId){ 
            breadcrumb.push(breadcrumbList[i].caseId);
          }
        }
        scope.breadcrumbList = breadcrumb;
      });
    }
  }
});