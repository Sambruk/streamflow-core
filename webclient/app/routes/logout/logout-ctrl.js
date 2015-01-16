'use strict';

angular.module('sf')
.controller('LogoutCtrl', function($scope, $location, $http, buildMode, navigationService, httpService, $window, $rootScope, $route){
  $rootScope.$broadcast('loggedin', false);
  $location.path('#/');
  $window.location.reload();
	/*$scope.logout = function(){

    var urlValue;
    if(buildMode == 'dev'){
      urlValue = 'https://username:password@test-sf.jayway.com/streamflow/';
    } else {
      urlValue = $location.$$protocol + '://username:password@' + $location.$$host + ':' + $location.$$port + '/webclient/api';
    }
    //document.execCommand("ClearAuthenticationCache");
    $http.get(urlValue).error(function(res){
      $http.get(httpService.apiUrl)
      .success(function(res){
        $rootScope.isLoggedIn = true;
        console.log(res);
      })
      .error(function(err){
        $rootScope.isLoggedIn = false;
        $location.path('#/');
        $window.location.reload();
        console.log(err);
      });

      console.log(res);
    });
  }

  $scope.logout();*/
});