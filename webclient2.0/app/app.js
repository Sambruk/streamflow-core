'use strict';

angular.module('sf', [
    'ngRoute',
    'ngResource',
    'angular-growl'
  ])
  .run(function ($rootScope, $location, $routeParams, tokenService) {
    $rootScope.hasToken = tokenService.hasToken;
    $rootScope.isLoggedIn = $rootScope.hasToken();
    $rootScope.logout = tokenService.clear;


    //Add current project type to rootScope to let toolbar update accordingly in index.html
    $rootScope.$on('$routeChangeSuccess', function(e, current, pre) {
      console.log('Current route name: ' + $location.path());
      console.log($routeParams);
      // Get all URL parameter
      $rootScope.toolbarParams = {};
      if($routeParams.projectType){
        $rootScope.toolbarParams.projectType = $routeParams.projectType;
      }
      if($routeParams.projectId){
        $rootScope.toolbarParams.projectId = $routeParams.projectId;        
      }
    });
  });