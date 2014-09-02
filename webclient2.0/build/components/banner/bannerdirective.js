'use strict';

angular.module('sf')
.directive('banner', function($q, profileService){
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
      profilePromise = profileService.getCurrent();

      $q.allSettled([profilePromise])
      .then(function(response){
        scope.profile = response;
      });

      scope.$on('profile-name-updated', function(){
        scope.profile.invalidate();
        scope.profile.resolve();
      });
    }
  };
});