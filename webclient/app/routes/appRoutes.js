'use strict';

angular.module('sf')
.config(function($routeProvider) {
  // $locationProvider.html5Mode(true);
  $routeProvider
    .when('/search', {
      templateUrl: 'routes/cases/casesearch.html',
      controller: 'CaseSearchCtrl'
    })
    .when('/perspectives', {
      templateUrl: 'routes/cases/caseoverview.html',
      controller: 'CaseOverviewCtrl'
    })
    .when('/profile', {
      templateUrl: 'routes/profile/profile-edit.html',
      controller: 'ProfileEditCtrl'
    })
    .when('/projects/:projectId/:projectType', {
      templateUrl: 'routes/projects/caselist.html',
      controller: 'CaseListCtrl'
    })
    .when('/cases/:caseId/edit', {
      templateUrl:'routes/cases/case-edit/caseedit.html',
      controller: 'CaseEditCtrl'
    })
    .when('/cases/:caseId', {
      templateUrl:'routes/cases/case-details/casedetail.html',
      controller: 'CaseDetailCtrl'
    })
    //TODO: This should probably not be in a route but maybe
    .when('/cases/:caseId/conversation/create', {
      templateUrl:'routes/cases/conversation/conversationcreate.html',
      controller: 'ConversationCreateCtrl'
    })
    .when('/cases/:caseId/conversation/:conversationId/participants/create', {
      templateUrl:'routes/cases/conversation/conversationparticipantcreate.html',
      controller: 'ConversationParticipantCreateCtrl'
    })
    .when('/cases/:caseId/conversation/:conversationId', {
      templateUrl:'routes/cases/conversation/conversationdetail.html',
      controller: 'ConversationDetailCtrl'
    })
    .when('/cases/:caseId/caselog', {
      templateUrl:'routes/cases/caselog/caseloglist.html',
      controller: 'CaselogListCtrl'
    })
    .when('/cases/:caseId/contact/:contactIndex/', {
      templateUrl:'routes/cases/contact/contactedit.html',
      controller: 'ContactEditCtrl'
    })
    .when('/cases/:caseId/formhistory/:formId', {
      templateUrl:'routes/cases/form/formhistory.html',
      controller: 'FormHistoryCtrl'
    })
    .when('/cases/:caseId/formdrafts/:formId', {
      templateUrl:'routes/cases/form/forms.html',
      controller: 'FormCtrl'
    })
    .when('/cases/:caseId/noteshistory/', {
      templateUrl:'routes/cases/note/noteshistory.html',
      controller: 'NotesHistoryCtrl'
    })
    .when('/cases/:caseId/print', {
      templateUrl:'routes/cases/print/print.html',
      controller: 'PrintCtrl'
    })
    .otherwise({
      redirectTo: '/'
    });
});