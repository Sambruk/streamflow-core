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
.directive('banner', function($rootScope, $q, profileService){
  return {
    restrict: 'E',
    templateUrl: 'components/banner/banner.html',
    scope: {
      //Optional attribute in case we want to have some communication for the profile object
      // from / to the parent scope i e. the controller ruling the view which injects the 
      // directive.
      profile: '=?'
    },
    link: function(scope){
      var profile = profileService.getCurrent();
      
      $q.all([profile.promise])
      .then(function(response){
        scope.profile = response[0];
      });

      scope.hasToken = $rootScope.hasToken;
      scope.logout = function(){
        $rootScope.logout();
        window.location.reload();
      }

      scope.$on('profile-name-updated', function(){
        scope.profile.invalidate();
        scope.profile.resolve();
      });
    }
  };
});