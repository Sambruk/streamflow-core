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
  .controller('ContactEditCtrl', function($scope, $rootScope, caseService, $routeParams, navigationService, checkPermissionService) {
    $scope.sidebardata = {};
    $scope.projectId = $routeParams.projectId;
    $scope.projectType = $routeParams.projectType;
    $scope.caseId = $routeParams.caseId;
    $scope.contactIndex = $routeParams.contactIndex;
    $scope.contact = caseService.getSelectedContact($routeParams.caseId, $routeParams.contactIndex);

    $scope.showSpinner = {
      contact: true
    };

    $scope.contact.promise.then(function(){
      $scope.showSpinner.contact = false;
      checkPermissionService.checkPermissions($scope, $scope.contact.commands, ['delete', 'update'], ['canDeleteContact', 'canUpdateContact']);
      if(!$scope.canUpdateContact) {
        $('.custom-select, .contact-pref').addClass('disabled');
      }
    });

    $scope.submitContact = function($event){
      $event.preventDefault();

      // Unfortunate API weirdness that demands manual object conversion.
      $scope.contact[0].address = $scope.contact[0].addresses[0].address;
      $scope.contact[0].city = $scope.contact[0].addresses[0].city;
      $scope.contact[0].country = $scope.contact[0].addresses[0].country;
      $scope.contact[0].region = $scope.contact[0].addresses[0].region;
      $scope.contact[0].zipCode = $scope.contact[0].addresses[0].zipCode;
      $scope.contact[0].email = $scope.contact[0].emailAddresses[0].emailAddress;
      $scope.contact[0].phone = $scope.contact[0].phoneNumbers[0].phoneNumber;
      $scope.contact[0].contactpreference = $scope.contact[0].contactPreference;

      $scope.contactId = caseService.updateContact($routeParams.caseId, $routeParams.contactIndex, $scope.contact[0])
      .then(function(){
        var href = navigationService.caseHref($routeParams.caseId);
        $scope.contact.invalidate();
        $scope.contact.resolve();

        window.location.assign(href);
      });
    };

    $scope.updateField = function ($event, $success, $error) {
      $event.preventDefault();
      var contact = {};

      contact[$event.currentTarget.name] = $event.currentTarget.value;

      if ($event.currentTarget.id === 'contact-phone' &&  !$event.currentTarget.value.match(/^$|^([0-9\(\)\/\+ \-]*)$/))  {
        $error($($event.target));
      }else if($event.currentTarget.id ==='contact-email' && !$event.currentTarget.value.match(/^$|^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/)){
        $error($($event.target));
      }else if($event.currentTarget.id === 'contact-id' && !$event.currentTarget.value.match(/^$|^19\d{10}$/)) {
        $error($($event.target));
      }else {
        $scope.contactId = caseService.updateContact($routeParams.caseId, $routeParams.contactIndex, contact)
        .then(function(){
          if ($event.currentTarget.id === 'contact-name') {
            $rootScope.$broadcast('contact-name-updated');
          }
          $success($($event.target));
        }, function (){
          $error($($event.target));
        });
      }
    };
  });
