/*
 *
 * Copyright 2009-2014 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

angular.module('sf')
.config(function($routeProvider) {
  // $locationProvider.html5Mode(true);
  $routeProvider
    .when('/login',{
      templateUrl: 'routes/login/login.html',
      controller: 'LoginCtrl'
    })
    .when('/logout', {
      templateUrl: 'routes/logout/logout.html',
      controller: 'LogoutCtrl'
    })
    .when('/search', {
      templateUrl: 'routes/projects/caselist.html',
      controller: 'SearchCtrl'
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
