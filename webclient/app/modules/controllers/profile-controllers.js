/*
 *
 * Copyright 2009-2012 Jayway Products AB
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

 (function() {
  'use strict';

  var sfProfile = angular.module('sf.controllers.profile', ['sf.services.profile','sf.services.case', 'sf.services.navigation', 'sf.services.http']);

  sfProfile.controller('ProfileCtrl', ['$scope', 'profileService', '$routeParams','navigationService', 'httpService',
    function($scope, profileService, $params, navigationService, httpService) {

	  $scope.profile = profileService.getCurrent();

	   $scope.$on('profile-name-updated', function(){
        $scope.profile.invalidate();
        $scope.profile.resolve();
      });
	  /*console.log("Result from profile controller: ");
	            console.log($scope.profile);*/
	}]);

  sfProfile.controller('ProfileEditCtrl', ['$scope', 'profileService', '$rootScope', '$routeParams','navigationService',
    function($scope, profileService, $rootScope, $params, navigationService) {

      
      $scope.profile = profileService.getCurrent();

      $scope.changeMessageDeliveryType = function ($event, $success, $error) {
		$event.preventDefault();
        var valueChange = {};
        valueChange[$event.currentTarget.name] = $event.currentTarget.value;

        profileService.changeMessageDeliveryType(valueChange).then(function(){
          $success($($event.target));

          $scope.profile.invalidate();
	      $scope.profile.resolve();
        },
        function (error){
          $error($($event.target));
        });
      }

      $scope.changeMailFooter = function ($event, $success, $error) {
		$event.preventDefault();
        var valueChange = {};
        valueChange[$event.currentTarget.name] = $event.currentTarget.value;

        profileService.changeMailFooter(valueChange).then(function(){
          $success($($event.target));

          $scope.profile.invalidate();
	      $scope.profile.resolve();
        },
        function (error){
          $error($($event.target));
        });
      }

      $scope.updateField = function ($event, $success, $error) {
        $event.preventDefault();
        var profile = {};
        profile[$event.currentTarget.name] = $event.currentTarget.value;

        profileService.updateCurrent(profile).then(function(){
        	/*console.log("profileService.updateCurrent: ");
        	console.log(profile);*/
          if ($event.currentTarget.id === 'profile-name') {
            $rootScope.$broadcast('profile-name-updated');
          }
          $success($($event.target));

          $scope.profile.invalidate();
	      $scope.profile.resolve();
        },
        function (error){
          $error($($event.target));
        });
      }
      

      $scope.changeMarkReadTimeout = function ($event, $success, $error) {
		$event.preventDefault();
        var valueChange = {};
        valueChange[$event.currentTarget.name] = $event.currentTarget.value;

        profileService.changeMarkReadTimeout(valueChange).then(function(){
          $success($($event.target));

          $scope.profile.invalidate();
	      $scope.profile.resolve();
        },
        function (error){
          $error($($event.target));
        });
      }

    }]);

  })();