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
.directive('breadcrumbbanner', function(profileService, $rootScope){
  return {
    restrict: 'E',
    templateUrl: 'components/breadcrumb-banner/breadcrumb-banner.html',
    scope: {
      breadcrumblist: '=?'
    },
    link: function(scope) {
      scope.breadcrumbList;
      scope.$watch('breadcrumbList', function(newVal){
        if(!newVal){
          return;
        }
        scope.breadcrumbList = newVal;   
      });
      
      $rootScope.$on('breadcrumb-updated', function(event, breadcrumbList) {
        console.log('BREADCRUMB UPDATED');
        console.log(breadcrumbList);
        var newbcItems = getBreadcrumbItems(breadcrumbList);
        console.log(newbcItems);
        scope.breadcrumbList = getBreadcrumbItems(breadcrumbList);
      });

      var getBreadcrumbItems = function(breadcrumbList){
        var bcList = [];
        _.each(breadcrumbList, function(breadcrumbItem){
          _.each(breadcrumbItem, function(val, key){
            if(typeof val === 'string' && val !== undefined){
              val = val.charAt(0).toUpperCase() + val.slice(1);
              bcList.push(val);
            }
          });
        });
        return bcList;
      };
    }
  };
});