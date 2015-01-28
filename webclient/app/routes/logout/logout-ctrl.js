'use strict';

angular.module('sf')
  .controller('LogoutCtrl', function($rootScope, $route){
    $rootScope.$broadcast('logout', true);
  });
