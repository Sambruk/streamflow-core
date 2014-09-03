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
      controller: 'ProfileCtrl'
    })
    .when('/projects/:projectId/:projectType', {
      templateUrl: 'routes/cases/caselist.html',
      controller: 'CaseListCtrl'
    })
    .when('/cases/:caseId/edit', {
      templateUrl:'routes/cases/caseedit.html',
      controller: 'CaseEditCtrl'
    })
    .when('/cases/:caseId', {
      templateUrl:'routes/cases/casedetail.html',
      controller: 'CaseDetailCtrl'
    })
    //TODO: This should probably not be in a route but maybe
    .when('/cases/:caseId/conversation/create', {
      templateUrl:'routes/conversation/conversationcreate.html',
      controller: 'ConversationCreateCtrl'
    })
    .when('/cases/:caseId/conversation/:conversationId/participants/create', {
      templateUrl:'routes/conversation/conversationparticipantcreate.html',
      controller: 'ConversationParticipantCreateCtrl'
    })
    .when('/cases/:caseId/conversation/:conversationId', {
      templateUrl:'routes/conversation/conversationdetail.html',
      controller: 'ConversationDetailCtrl'
    })
    .when('/cases/:caseId/caselog', {
      templateUrl:'routes/caselog/caseloglist.html',
      controller: 'CaselogListCtrl'
    })
      .when('/cases/:caseId/contact/add', {
        templateUrl:'routes/contact/contactcreate.html',
        controller: 'ContactCreateCtrl'
      })
      .when('/cases/:caseId/contact/:contactIndex/', {
        templateUrl:'routes/contact/contactedit.html',
        controller: 'ContactEditCtrl'
      })
    .when('/cases/:caseId/formhistory/:formId', {
      templateUrl:'routes/form/formhistory.html',
      controller: 'FormHistoryCtrl'
    })
    .when('/cases/:caseId/formdrafts/:formId', {
      templateUrl:'routes/form/forms.html',
      controller: 'FormCtrl'
    })
    .when('/cases/:caseId/noteshistory/', {
      templateUrl:'routes/note/noteshistory.html',
      controller: 'NotesHistoryCtrl'
    })
    .when('/cases/:caseId/print', {
      templateUrl:'routes/print/print.html',
      controller: 'PrintCtrl'
    })
    .otherwise({
      redirectTo: '/'
    });
});