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
.factory('contactService', function(caseService, navigationService, $rootScope){
    var contact = {
      name: '',
      contactId: '',
      note: '',
      addresses: [{ address: '', zipCode: '', city: '', region: '', country: '', contactType: 'HOME' }],
      emailAddresses: [{ emailAddress: '', contactType: 'HOME'}],
      phoneNumbers: [{ phoneNumber: '', contactType: 'HOME' }],
      contactPreference: 'email'
    };

    var _submitContact = function(caseId, contactIndex) {
      caseService.addContact(caseId, contact).then(function(){
        $rootScope.$broadcast('contact-created');
        var href = navigationService.caseHrefSimple(caseId);
        window.location.assign(href + '/contact/' + contactIndex + '/');
      });
    };

  return {
    submitContact: _submitContact
  };
});
