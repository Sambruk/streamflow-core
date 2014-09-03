'use strict';
angular.module('sf')
.controller('CaseDetailCtrl', function(growl, $q, $scope, $timeout, $routeParams, caseService, navigationService, projectService, profileService, $rootScope){
  var caze = caseService.getSelected($routeParams.caseId);
  var notes = caseService.getSelectedNote($routeParams.caseId);
  var general = caseService.getSelectedGeneral($routeParams.caseId);

  var dfds = [];
  dfds.push(caze.promise, notes.promise, general.promise);
  $q.all(dfds)
  .then(function(response){
    $scope.caze = response[0];
    $scope.notes = response[1];
    $scope.general = response[2];
  })
  .then(function(){
      $scope.sidebarData = {};
    $scope.sidebarData.caseData = {caseId: $routeParams.caseId, caze: $scope.caze};
    $scope.sidebarData.general = $scope.general;
    $scope.sidebarData.notes = $scope.notes;
  });
  
  



  $scope.commands = caseService.getSelectedCommands($routeParams.caseId);
  $scope.profile = profileService.getCurrent();

  $scope.$watch('caze[0]', function(newVal){
    if(!newVal){
      return; 
    }
    if ($scope.caze.length === 1){
      $scope.caseListUrl = navigationService.caseListHrefFromCase($scope.caze);
      $rootScope.$broadcast('breadcrumb-updated', [{projectId: $scope.caze[0].owner}, {projectType: $scope.caze[0].listType}, {caseId: $scope.caze[0].caseId}]);
    }
  });

  $scope.$on('case-created', function() {
    $scope.caze.invalidate();
  });

  $scope.$on('case-changed', function() {
    $scope.caze.invalidate();
    $scope.caze.resolve();
  });

  $scope.$on('note-changed', function() {
    $scope.notes = caseService.getSelectedNote($routeParams.caseId);
  });

  $scope.$on('noteDescription-changed', function() {
    $scope.caze = caseService.getSelected($routeParams.caseId);
  })

  /**
   * ERROR HANDLER
   **/
      //TODO: Implement error handler listener on other controllers where needed
  $scope.errorHandler = function(){
    var bcMessage = caseService.getMessage();
    if(bcMessage === 200)  {
        //growl.addSuccessMessage('successMessage');
    }else {
      growl.warning('errorMessage');
    }
  };

  //error-handler
  $scope.$on('httpRequestInitiated', $scope.errorHandler);

  // Mark the case as Read after the ammount of time selected in profile.
  // TODO <before uncomment>. Find a way to update possible commands after post.
  /*$scope.$watch("commands[0] + profile[0]", function(){
   var commands = $scope.commands;
   var profile = $scope.profile[0];

   $scope.canRead = _.any(commands, function(command){
   return command.rel === "read";
   });

   if ($scope.canRead) {
   $timeout(function() {
   caseService.Read($routeParams.projectId, $routeParams.projectType, $routeParams.caseId);
   }, profile.markReadTimeout * 1000)

   }
   });*/
});
