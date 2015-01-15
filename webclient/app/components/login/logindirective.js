/*
 *
 * Copyright 2009-2014 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

angular.module('sf')
.directive('login', function($rootScope,buildMode,$location, $http, $window, tokenService, httpService){
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

      function checkIfLoggedIn(){
        var urlValue;
        if(buildMode == 'dev'){
          urlValue = 'https://dummyuser:dummypass@test-sf.jayway.com/streamflow/';
        } else {
          urlValue = $location.$$protocol + '://dummyuser:dummypass@' + $location.$$host + ':' + $location.$$port + '/webclient/api';
        }
        $http.get(httpService.apiUrl)
        .success(function(res){
          $rootScope.isLoggedIn = true;
          console.log(res);
        })
        .error(function(err){
          $rootScope.isLoggedIn = false;
          console.log(err);
        });
      }
      checkIfLoggedIn();

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