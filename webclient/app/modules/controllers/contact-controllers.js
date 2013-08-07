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

  var sfContact = angular.module('sf.controllers.contact', ['sf.services.case', 'sf.services.navigation']);

  sfContact.controller('ContactCreateCtrl', ['$scope', 'caseService', '$routeParams','navigationService',
    function($scope, caseService, $params, navigationService) {

      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;
      $scope.caseId = $params.caseId;
      $scope.contacts = caseService.getSelectedContacts($params.projectId, $params.projectType, $params.caseId);

      // TODO: Initialise contact in another way
      $scope.contact = {
        name: '',
        contactId: '',
        note: '',
        addresses: [{ address: '', zipCode: '', city: '', region: '', country: '', contactType: 'HOME' }],
        emailAddresses: [{ emailAddress: '', contactType: 'HOME'}],
        phoneNumbers: [{ phoneNumber: '', contactType: 'HOME' }],
        contactPreference: 'email'
      };

      $scope.submitContact = function($event){
        $event.preventDefault();

        // TODO: Fix this weird workaround for serializing form values to json
        $scope.contact.phoneNumbers = angular.toJson($scope.contact.phoneNumbers);
        $scope.contact.addresses = angular.toJson($scope.contact.addresses);
        $scope.contact.emailAddresses = angular.toJson($scope.contact.emailAddresses);
        $scope.contactId = caseService.addContact($params.projectId, $params.projectType, $params.caseId, $scope.contact).then(function(){
          var href = navigationService.caseHref($params.caseId);
          $scope.contacts.invalidate();
          $scope.contacts.resolve();
          window.location.assign(href);
        });
      }

    }]);

  sfContact.controller('ContactDetailCtrl', ['$scope', 'caseService', '$routeParams','navigationService',
    function($scope, caseService, $params, navigationService) {

      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;
      $scope.caseId = $params.caseId;
      $scope.contacts = caseService.getSelectedContacts($params.projectId, $params.projectType, $params.caseId);

      // TODO: Initialise contact in another way
      $scope.contact = {
        name: '',
        contactId: '',
        note: '',
        addresses: [{ address: '', zipCode: '', city: '', region: '', country: '', contactType: 'HOME' }],
        emailAddresses: [{ emailAddress: '', contactType: 'HOME'}],
        phoneNumbers: [{ phoneNumber: '', contactType: 'HOME' }],
        contactPreference: 'email'
      };

      $scope.submitContact = function($event){
        $event.preventDefault();

        // TODO: Fix this weird workaround for serializing form values to json
        $scope.contact.phoneNumbers = angular.toJson($scope.contact.phoneNumbers);
        $scope.contact.addresses = angular.toJson($scope.contact.addresses);
        $scope.contact.emailAddresses = angular.toJson($scope.contact.emailAddresses);
        $scope.contactId = caseService.addContact($params.projectId, $params.projectType, $params.caseId, $scope.contact).then(function(){
          var href = navigationService.caseHref($params.caseId);
          $scope.contacts.invalidate();
          $scope.contacts.resolve();
          window.location.assign(href);
        });
      }

    }]);

  sfContact.controller('ContactEditCtrl', ['$scope', 'caseService', '$routeParams','navigationService',
    function($scope, caseService, $params, navigationService) {

      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;
      $scope.caseId = $params.caseId;
      $scope.contactIndex = $params.contactIndex;
      $scope.contact = caseService.getSelectedContact($params.projectId, $params.projectType, $params.caseId, $params.contactIndex);
      $scope.contacts = caseService.getSelectedContacts($params.projectId, $params.projectType, $params.caseId);

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

          // Delete unused properties
        /*delete $scope.contact[0].addresses;
        delete $scope.contact[0].emailAddresses;
        delete $scope.contact[0].phoneNumbers;
        delete $scope.contact[0].contactPreference;*/

        $scope.contactId = caseService.updateContact($params.projectId, $params.projectType, $params.caseId, $params.contactIndex, $scope.contact[0]).then(function(){
          var href = navigationService.caseHref($params.caseId);
          $scope.contact.invalidate();
          $scope.contact.resolve();
          $scope.contacts.invalidate();
          $scope.contacts.resolve();
          window.location.assign(href);
        });
      }

    }]);


})();
