'use strict';
angular.module('sf')
  .controller('CaselogListCtrl', function($scope, $rootScope, caseService, $routeParams, navigationService, httpService) {
    $scope.sidebardata = {};
    $scope.caseId = $routeParams.caseId;
    var defaultFiltersUrl = caseService.getWorkspace() + '/cases/' + $scope.caseId + '/caselog/defaultfilters';

    $scope.showSpinner = {
      caseLogs: true
    };
    
    httpService.getRequest(defaultFiltersUrl, false)
    .then(function(result){
      var filterObj = result.data;
      var filterArray = [];
      for (var prop in filterObj) {
        filterArray.push({ "filterName": prop, "filterValue": filterObj[prop] });
      }
      $scope.caseLogFilters = filterArray;
      $scope.caseLogs = caseService.getSelectedCaseLog($routeParams.caseId);

      $scope.caseLogs.promise.then(function(){
        $scope.showSpinner.caseLogs = false;
      });
    });

    $rootScope.$on('update-caseLogs', function(){
      $scope.showSpinner.caseLogs = true;
      updateObject($scope.caseLogs);

      $scope.caseLogs.promise.then(function(){
        $scope.showSpinner.caseLogs = false;
      });
    });
    
    $scope.$on('caselog-message-created', function(){
      $scope.showSpinner.caseLogs = true;
      updateObject($scope.caseLogs);

      $scope.caseLogs.promise.then(function(){
        $scope.showSpinner.caseLogs = false;
      });
    });

    var updateObject = function(itemToUpdate){
      itemToUpdate.invalidate();
      itemToUpdate.resolve();
    };
  });