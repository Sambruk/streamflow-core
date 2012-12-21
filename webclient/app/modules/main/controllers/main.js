'use strict';

webclientApp.controller('MainCtrl', function($scope) {
  console.log("HEJ HOPP");
  $scope.click = function() {
    console.log("CLICK IT");
  }
  $scope.awesomeThings = [
    'HTML5 Boilerplate',
    'AngularJS',
    'Testacular'
  ];
});
