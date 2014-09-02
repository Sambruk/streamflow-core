'use strict';

angular.module('sf')
.directive('toolbar', function(projectService, navigationService, $rootScope){
  return {
    restrict: 'E',
    templateUrl: 'components/toolbar/toolbar.html',
    scope: {
      params: '=?'
    },
    link: function(scope){
      scope.projects = projectService.getAll();

      scope.toggleToolbar = function($event) {
        $('.functions-menu').toggleClass('open');
        if ( $('.functions-menu').hasClass('open') ) {
          $('.sub-category').show();
        } else {
          $('.sub-category').hide();
        }
      };

      scope.canCreateCase = function() {
        var canCreate = true;
        if (scope.params.projectType === 'inbox') {
          canCreate = false;
        }
        if (!scope.params.projectType) {
          canCreate = false;
        }
        return canCreate;
      };

      scope.createCase = function(){
        if(!canCreateCase()){
          return;
        }

        $rootScope.$broadcast('case-created');

        projectService.createCase(scope.params.projectId, scope.params.projectType).then(function(response){
          //NOTE: Why is caseId defined here?
          var caseId = response.data.events[1].entity;
          var href = navigationService.caseHrefSimple(caseId);

          window.location.replace(href + "/edit");
        });
      }
    }
  };
});