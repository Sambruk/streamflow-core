'use strict';

angular.module('sf')
.directive('login', function($rootScope,$location, $http, $window, tokenService, httpService){
  return {
    restrict: 'E',
    templateUrl: 'components/login/login.html',
    scope: {

    },
    link: function(scope){
      scope.errorMessage = "";
      scope.username;
      scope.password;
      scope.hasToken = tokenService.hasToken();
      $rootScope.isLoggedIn = tokenService.hasToken();

      scope.validate = function () {
        var basicAuthBase64 = btoa(scope.username + ':' + scope.password);
        $http.defaults.headers.common.Authorization = 'Basic ' + basicAuthBase64;

        $http({
          method: 'GET',
          url: httpService.absApiUrl('account/profile'),
          cache: 'false'
        }).then(function () {
          tokenService.storeToken(basicAuthBase64);
          window.location.reload();
        }, function () {
          scope.errorMessage = "Användarnamn / lösenord ej giltigt!";
        });
      };
    }
  };
});