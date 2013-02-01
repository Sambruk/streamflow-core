/*
 *
 * Copyright 2009-2012 Jayway Products AB
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
describe("sf.services.project", function () {
  'use strict';

  var backend = window.mockBackend;

  beforeEach(module('sf.services.project'));

  beforeEach(inject(function(httpService, navigationService) {
    httpService.apiUrl = 'mock/';
    spyOn(navigationService, 'projectId').andReturn('b35873ba-4007-40ac-9936-975eab38395a-3f');
    spyOn(navigationService, 'caseType').andReturn('inbox');
  }));

  describe("projectService", function(){

    describe("getAll", function() {

      it("returns the profile", inject(function (projectService, $httpBackend) {
        // Given
        $httpBackend.expectGET('mock/').respond(backend.root);
        $httpBackend.expectGET('mock/workspace/').respond(backend.workspace);
        $httpBackend.expectGET('mock/workspace/projects/').respond(backend.projects);

        // When
        var response = projectService.getAll();

        // Then
        expect(response.length).toEqual(0);

        // When
        $httpBackend.flush();

        // Then
        expect(response.length).toEqual(1);
        expect(response[0].text).toEqual('Streamflow');
      }));

    });

    describe("getSelected", function() {

      it("returns the selected project", inject(function(projectService, $httpBackend){
        // Given
        $httpBackend.expectGET('mock/').respond(backend.root);
        $httpBackend.expectGET('mock/workspace/').respond(backend.workspace);
        $httpBackend.expectGET('mock/workspace/projects/').respond(backend.projects);
        $httpBackend.expectGET('mock/workspace/projects/b35873ba-4007-40ac-9936-975eab38395a-3f/').respond(backend.project1);
        $httpBackend.expectGET('mock/workspace/projects/b35873ba-4007-40ac-9936-975eab38395a-3f/inbox/').respond(backend.project1Inbox);
        $httpBackend.expectGET('mock/workspace/projects/b35873ba-4007-40ac-9936-975eab38395a-3f/inbox/cases').respond(backend.project1InboxCases);

        // When
        var response = projectService.getSelected();

        // Then
        expect(response.length).toEqual(0);

        // When
        $httpBackend.flush();

        // Then
        expect(response.length).toEqual(2);
        expect(response[0].text).toEqual('Test Test Test 2');
      }));
    });
  });

  describe('caseMixin', function() {
    var yesterday, tomorrow, object;

    beforeEach(inject(function(projectService) {
        object = _.extend({}, projectService.caseMixin);
        yesterday = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
    }));

    describe('overdueDays', function() {
      it('is overdue by 1 if dueDate is yesterday', function() {
        object.dueDate = yesterday.toString();
        expect(object.overdueDays()).toEqual(1);
      });

      it('is not overdue (0) if dueDate is tomorrow', function() {
        object.dueDate = tomorrow.toString();
        expect(object.overdueDays()).toEqual(0);
      });

      it('is overdue for an old string date', function() {
        object.dueDate = '2013-01-24T12:04:34.220Z';
        expect(object.overdueDays()).toBeGreaterThan(6);
      });

    });

    describe('overdueStatus', function() {
      it('is unset if dueDate is unset', function() {
        object.dueDate = null;
        expect(object.overdueStatus()).toEqual('unset');
      });

      it('is overdue if overdue:)', function() {
        object.dueDate = yesterday;
        expect(object.overdueStatus()).toEqual('overdue');
      });

      it('is set if dueDate is set but not overdue', function() {
        object.dueDate = tomorrow;
        expect(object.overdueStatus()).toEqual('set');
      });

    });
  });
});
