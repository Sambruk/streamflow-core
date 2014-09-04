'use strict';
angular.module('sf')
  .controller('CaselogListCtrl', function($scope, caseService, $routeParams, navigationService, httpService) {
    $scope.caseId = $routeParams.caseId;
    var defaultFiltersUrl = caseService.getWorkspace() + '/cases/' + $scope.caseId + '/caselog/defaultfilters';
    httpService.getRequest(defaultFiltersUrl, false)
    .then(function(result){
      var filterObj = result.data;
      var filterArray = [];
      for (var prop in filterObj) {
        filterArray.push({ "filterName": prop, "filterValue": filterObj[prop] });
      }
      $scope.caseLogFilters = filterArray;
      $scope.caseLogs = caseService.getSelectedCaseLog($params.caseId);
      //$scope.caseLogs = caseService.getSelectedCaseLog($params.projectId, $params.projectType, $params.caseId, defaultFilters);
      //console.log($scope.caseLogs);
    });
  });