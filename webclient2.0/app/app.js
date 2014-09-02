'use strict';

angular.module('sf', [
    'ngRoute',
    'angular-growl'
  ])
  .run(function ($rootScope, tokenService) {
    $rootScope.hasToken = tokenService.hasToken;
    $rootScope.logout = tokenService.clear;
  });