'use strict';
angular.module('sf')
  .controller('ContactCreateCtrl', function($scope, caseService, $routeParams, navigationService) {

    $scope.projectId = $routeParams.projectId;
    $scope.projectType = $routeParams.projectType;
    $scope.caseId = $routeParams.caseId;
    $scope.contacts = caseService.getSelectedContacts($routeParams.caseId);

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
      $scope.contactId = caseService.addContact($routeParams.caseId, $scope.contact).then(function(){
        var href = navigationService.caseHrefSimple($routeParams.caseId);
        $scope.contacts.invalidate();
        $scope.contacts.resolve();
        window.location.assign(href);
      });
    }

    $scope.updateField = function ($event, $success, $error) {
      $event.preventDefault();
      var contact = {};

      contact[$event.currentTarget.name] = $event.currentTarget.value;

      if ($event.currentTarget.id === 'contact-phone' &&  !$event.currentTarget.value.match(/^$|^([0-9\(\)\/\+ \-]*)$/))   {
        //handle no number error
        $error($($event.target));
      }else if($event.currentTarget.id ==='contact-email' && !$event.currentTarget.value.match(/^$|^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/)){
        //handle no email error
        $error($($event.target));
      }else if($event.currentTarget.id === 'contact-id' && !$event.currentTarget.value.match(/^$|^19\d{10}$/)) {
        $error($($event.target));
      }else{
        if($success){
          $success($($event.target));
        }else if($error) {
          $error($($event.target));
        }
      }
    }
});
