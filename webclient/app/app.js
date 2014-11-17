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

angular.module('sf', [
    'ngRoute',
    'ngResource',
    'angular-growl',
    'ngSanitize'
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
      $rootScope.contextmenuParams = {};
      if($routeParams.projectType){
        $rootScope.contextmenuParams.projectType = $routeParams.projectType;
      }
      if($routeParams.projectId){
        $rootScope.contextmenuParams.projectId = $routeParams.projectId;        
      }
    });
  });