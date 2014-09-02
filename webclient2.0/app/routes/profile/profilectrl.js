'use strict';
angular.module('sf')
  .controller('ProfileCtrl', function($scope, profileService, $routeParams, navigationService, httpService) {
    $scope.profile = profileService.getCurrent();
    $scope.$on('profile-name-updated', function(){
      $scope.profile.invalidate();
      $scope.profile.resolve();
    });
  });