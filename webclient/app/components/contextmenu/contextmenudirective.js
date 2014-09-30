'use strict';

angular.module('sf')
.directive('contextmenu', function(projectService, navigationService, $rootScope, $timeout){
  return {
    restrict: 'E',
    templateUrl: 'components/contextmenu/contextmenu.html',
    scope: {
      params: '=?'
    },
    link: function(scope){
      scope.projects = projectService.getAll();

      scope.displayToolbar = false;

      /*scope.toggleToolbar = function(){
        scope.displayToolbar = !scope.displayToolbar;
      }*/

      $rootScope.$on('case-changed-update-context-and-caselist', function(){
        scope.projects.invalidate();
        scope.projects.resolve();
        console.log('case-changed-update-contect-menu')
      });

      scope.navigateTo = function(href, $event){
        $event.preventDefault();
        scope.toggleToolbar($event);
        navigationService.linkTo(href);
      };
      

      scope.toggleToolbar = function($event) {
        $event.preventDefault();
        $('.functions-menu').toggleClass('open');
        if ( $('.functions-menu').hasClass('open') ) {
          $('.sub-category').show();
        } else {
          $('.sub-category').hide();
        }
      };

      scope.canCreateCase = function() {
        if(!scope.params){
          return false;
        }
        if (scope.params.projectType === 'inbox') {
          return false;
        }
        if (!scope.params.projectType) {
          return false;
        }
        return true;
      };

      scope.createCase = function(){
        if(!scope.canCreateCase()){
          return;
        }

        projectService.createCase(scope.params.projectId, scope.params.projectType).then(function(response){
          //NOTE: Why is caseId defined here?
          var caseId = response.data.events[1].entity;
          var href = navigationService.caseHrefSimple(caseId);

          $rootScope.$broadcast('case-changed-update-context-and-caselist');

          window.location.replace(href + "/edit");
        });
      }
    }
  };
});