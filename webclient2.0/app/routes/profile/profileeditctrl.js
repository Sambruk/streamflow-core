'use strict';
angular.module('sf')
    .controller('ProfileEditCtrl',
        function($scope, profileService, $rootScope, navigationService) {


            $scope.profile = profileService.getCurrent();

            $scope.changeMessageDeliveryType = function ($event, $success, $error) {
                $event.preventDefault();
                var valueChange = {};
                valueChange[$event.currentTarget.name] = $event.currentTarget.value;

                profileService.changeMessageDeliveryType(valueChange).then(function(){
                        $success($($event.target));
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
                    },
                    function (error){
                        $error($($event.target));
                    });
            }

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
            }


            $scope.changeMarkReadTimeout = function ($event, $success, $error) {
                $event.preventDefault();
                var valueChange = {};
                valueChange[$event.currentTarget.name] = $event.currentTarget.value;

                profileService.changeMarkReadTimeout(valueChange).then(function(){
                        $success($($event.target));
                    },
                    function (error){
                        $error($($event.target));
                    });
            }

        });