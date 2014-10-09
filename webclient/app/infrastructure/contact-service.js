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

    var _showContact = function(contactId){
      alert("Not supported - need UX for this.");
    };

    var _submitContact = function(caseId, contactIndex) {
      caseService.addContact(caseId, contact).then(function(){
        $rootScope.$broadcast('contact-created');
        var href = navigationService.caseHrefSimple(caseId);
        window.location.assign(href + "/contact/" + contactIndex + "/");
      });
    };

  return {
    submitContact: _submitContact
  };
});