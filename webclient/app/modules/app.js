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
(function() {
  'use strict';

  window.sf = {
    directives: angular.module('sf.directives', []),
    filters: angular.module('sf.filters', []),
    controllers: angular.module('sf.controllers', 'sf.controllers.case', [])
  };

  sf.env = sf.env || 'development';

  var app = angular.module('sf', ['angular-growl','sf.filters', 'sf.controllers.case', 'sf.controllers.conversation','sf.controllers.caselog',
    'sf.controllers.profile', 'sf.controllers.contact', 'sf.controllers.form', 'sf.controllers.notes', 
    'sf.controllers.project', 'sf.controllers.sidebar', 'sf.directives'])
    .config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
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
      .when('/:cases/:caseId', {
        templateUrl:'modules/views/case-detail.html',
        controller: 'CaseDetailCtrl'
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


  }]);

  app.controller('BreadcrumbCtrl', ['$scope', 'profileService', '$routeParams','navigationService', 'httpService', '$rootScope',
    function($scope, profileService, $params, navigationService, httpService, $rootScope) {
      
      $rootScope.$on('breadcrumb-updated', function(scope, breadcrumbList) {  
        var breadcrumb = [];
        for (var i=0;i<breadcrumbList.length;i++)
        {
          if(breadcrumbList[i].projectId){ 
            breadcrumb.push(breadcrumbList[i].projectId);
          }if(breadcrumbList[i].projectType){ 
            // translate didn't work
            if(breadcrumbList[i].projectType == 'inbox'){
              breadcrumb.push('Inkorg');
            }if(breadcrumbList[i].projectType == 'assignments'){
              breadcrumb.push('Mina ärenden');
            }
          }if(breadcrumbList[i].caseId){ 
            breadcrumb.push(breadcrumbList[i].caseId);
          }
        }
        $scope.breadcrumbList = breadcrumb;
      });
    }]);

})();

