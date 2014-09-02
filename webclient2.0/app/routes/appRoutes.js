'use strict';

angular.module('sf')
.config(function($routeProvider) {
  // $locationProvider.html5Mode(true);
  $routeProvider
    .when('/search', {
      templateUrl: 'modules/views/case-search.html',
      controller: 'CaseSearchCtrl'
    })
    .when('/perspectives', {
      templateUrl: 'modules/views/case-overview.html',
      controller: 'CaseOverviewCtrl'
    })
    .when('/profile', {
      templateUrl: 'modules/views/profile-edit.html',
      controller: 'ProfileCtrl'
    })
    .when('/projects/:projectId/:projectType', {
      templateUrl: 'modules/views/case-list.html',
      controller: 'CaseListCtrl'
    })
    .when('/cases/:caseId/edit', {
      templateUrl:'modules/views/case-edit.html',
      controller: 'CaseEditCtrl'
    })
    .when('/cases/:caseId', {
      templateUrl:'modules/views/case-detail.html',
      controller: 'CaseDetailCtrl'
    })
    .when('/cases/:caseId/conversation/create', {
      templateUrl:'modules/views/conversation-create.html',
      controller: 'ConversationCreateCtrl'
    })
    .when('/cases/:caseId/conversation/:conversationId/participants/create', {
      templateUrl:'modules/views/conversation-participant-create.html',
      controller: 'ConversationParticipantCreateCtrl'
    })
    .when('/cases/:caseId/conversation/:conversationId', {
      templateUrl:'modules/views/conversation-detail.html',
      controller: 'ConversationDetailCtrl'
    })
    .when('/cases/:caseId/caselog', {
      templateUrl:'modules/views/caselog-list.html',
      controller: 'CaselogListCtrl'
    })
      .when('/cases/:caseId/contact/add', {
        templateUrl:'modules/views/contact-create.html',
        controller: 'ContactCreateCtrl'
      })
      .when('/cases/:caseId/contact/:contactIndex/', {
        templateUrl:'modules/views/contact-edit.html',
        controller: 'ContactEditCtrl'
      })
    .when('/cases/:caseId/formhistory/:formId', {
      templateUrl:'modules/views/form-history.html',
      controller: 'FormHistoryCtrl'
    })
    .when('/cases/:caseId/formdrafts/:formId', {
            templateUrl:'modules/views/forms.html',
            controller: 'FormCtrl'
    })
    .when('/cases/:caseId/noteshistory/', {
      templateUrl:'modules/views/notes-history.html',
      controller: 'NotesHistoryCtrl'
    })
    .when('/cases/:caseId/print', {
      templateUrl:'modules/views/print.html',
      controller: 'PrintCtrl'
    })
    .otherwise({
      redirectTo: '/'
    });
});