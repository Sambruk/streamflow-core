'use strict';

angular.module('sf')
.directive('banner', function($rootScope, $q, profileService){
  return {
    restrict: 'E',
    templateUrl: 'components/banner/banner.html',
    scope: {
      //Optional attribute in case we want to have some communication for the profile object
      // from / to the parent scope i e. the controller ruling the view which injects the 
      // directive.
      profile: '=?'
    },
    link: function(scope){
      var profile = profileService.getCurrent();
      
      $q.all([profile.promise])
      .then(function(response){
        scope.profile = response;
      });

      scope.hasToken = $rootScope.hasToken;
      scope.logout = function(){
        $rootScope.logout();
        window.location.reload();
      }

      scope.$on('profile-name-updated', function(){
        scope.profile.invalidate();
        scope.profile.resolve();
      });
    }
  };
});