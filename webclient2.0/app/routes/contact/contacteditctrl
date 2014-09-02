'use strict';
angular.module('sf')
    .controller('ContactEditCtrl',
        function($scope, $rootScope, caseService, $params, navigationService) {

            $scope.projectId = $params.projectId;
            $scope.projectType = $params.projectType;
            $scope.caseId = $params.caseId;
            $scope.contactIndex = $params.contactIndex;
            $scope.contact = caseService.getSelectedContact($params.caseId, $params.contactIndex);
            $scope.contacts = caseService.getSelectedContacts($params.caseId);

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

                $scope.contactId = caseService.updateContact($params.caseId, $params.contactIndex, $scope.contact[0]).then(function(){
                    var href = navigationService.caseHref($params.caseId);
                    $scope.contact.invalidate();
                    $scope.contact.resolve();
                    $scope.contacts.invalidate();
                    $scope.contacts.resolve();
                    window.location.assign(href);
                });
            }

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
                    $scope.contactId = caseService.updateContact($params.caseId, $params.contactIndex, contact).then(function(){
                            if ($event.currentTarget.id === 'contact-name') {
                                $rootScope.$broadcast('contact-name-updated');
                            }
                            $success($($event.target));
                        },
                        function (error){
                            $error($($event.target));
                        });
                }

            }

        });