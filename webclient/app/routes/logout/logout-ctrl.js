'use strict';

angular.module('sf')
.controller('LogoutCtrl', function($scope, $location, $http, buildMode, navigationService){
	$scope.logout = function(){
    var urlValue;
    if(buildMode == 'dev'){
      urlValue = 'https://dummyuser:dummypass@test-sf.jayway.com/streamflow/';
    } else {
      urlValue = $location.$$protocol + '://dummyuser:dummypass@' + $location.$$host + ':' + $location.$$port + '/webclient/api';
    }
    document.execCommand("ClearAuthenticationCache");
    $http.get(urlValue).error(function(res){
      navigationService.linkTo('#/');
      location.reload();
    });
  }

  $scope.logout();
});