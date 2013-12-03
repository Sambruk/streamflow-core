/*
 *
 * Copyright 2009-2013 Jayway Products AB
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
(function () {
  'use strict';


  var sfServices = angular.module('sf.services.navigation', []);

  sfServices.factory('navigationService', ['$location', '$routeParams', function ($location, $routeParams) {

    return {
      caseHref: function(caseId) {
        return "#/" + this.projectId() + '/' + this.projectType() + '/' + caseId;
      },
      caseHrefSimple: function(caseId) {
        return '#/cases/' + caseId;
      },
      caseListHrefFromCase: function(caze) {
        return "#/" + "projects/" + caze[0].ownerId + '/' + caze[0].listType + '/';
      },
      projectId: function() {
        return $routeParams.projectId;
      },
      projectType: function() {
        return $routeParams.projectType;
      },
      caseId: function() {
        return $routeParams.caseId;
      }
    };
  }]);


})();
