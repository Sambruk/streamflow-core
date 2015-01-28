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
  .controller('ProfileEditCtrl', function ($scope, profileService, $rootScope, $routeParams, navigationService, httpService) {
    $scope.profile = profileService.getCurrent();
    $scope.$on('profile-name-updated', function () {
      $scope.profile.invalidate();
      $scope.profile.resolve();
    });

    $scope.changeMessageDeliveryType = function ($event, $success, $error) {
      $event.preventDefault();
      var valueChange = {};
      valueChange[$event.currentTarget.name] = $event.currentTarget.value;

      profileService.changeMessageDeliveryType(valueChange).then(function () {
          $success($($event.target));
        },
        function (error) {
          $error($($event.target));
        });
    };

    $scope.changeMailFooter = function ($event, $success, $error) {
      $event.preventDefault();
      var valueChange = {};
      valueChange[$event.currentTarget.name] = $event.currentTarget.value;

      profileService.changeMailFooter(valueChange).then(function () {
          $success($($event.target));
        },
        function (error) {
          $error($($event.target));
        });
    };

    $scope.updateField = function ($event, $success, $error) {
      $event.preventDefault();
      var profile = {};
      profile[$event.currentTarget.name] = $event.currentTarget.value;

      if ($event.currentTarget.id === 'profile-phone' && !$event.currentTarget.value.match(/^([0-9\(\)\/\+ \-]*)$/)) {
        $error($($event.target));
      } else if ($event.currentTarget.id === 'profile-email' && !$event.currentTarget.value.match(/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/)) {
        $error($($event.target));
      } else {

        profileService.updateCurrent(profile).then(function () {
            if ($event.currentTarget.id === 'profile-name') {
              $rootScope.$broadcast('profile-name-updated');
            }
            $success($($event.target));
          },
          function (error) {
            $error($($event.target));
          });
      }
    };


    $scope.changeMarkReadTimeout = function ($event, $success, $error) {
      $event.preventDefault();
      var valueChange = {};
      valueChange[$event.currentTarget.name] = $event.currentTarget.value;

      profileService.changeMarkReadTimeout(valueChange).then(function () {
          $success($($event.target));
        },
        function (error) {
          $error($($event.target));
        });
    };

  });
