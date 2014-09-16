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

  beforeEach(module('sf'));

  beforeEach(inject(function(httpService, navigationService) {
    httpService.apiUrl = 'mock/';
    spyOn(navigationService, 'projectId').andReturn('b35873ba-4007-40ac-9936-975eab38395a-3f');
    //spyOn(navigationService, 'caseType').andReturn('inbox');
  }));

  describe("projectService", function(){

    describe("getAll", function() {

      it("returns the profile", inject(function (projectService, $httpBackend) {
        // Given
        $httpBackend.expectGET('mock/').respond(backend.root);
        $httpBackend.expectGET('mock/workspacev2/').respond(backend.workspacev2);

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
        $httpBackend.expectGET('mock/workspacev2/').respond(backend.workspacev2);
        $httpBackend.expectGET('mock/workspacev2/projects/').respond(backend.projects);
        $httpBackend.expectGET('mock/workspacev2/projects/b35873ba-4007-40ac-9936-975eab38395a-3f/').respond(backend.project1);
        $httpBackend.expectGET('mock/workspacev2/projects/b35873ba-4007-40ac-9936-975eab38395a-3f/inbox/').respond(backend.project1Inbox);
        $httpBackend.expectGET('mock/workspacev2/projects/b35873ba-4007-40ac-9936-975eab38395a-3f/inbox/cases?tq=select+*').respond(backend.project1InboxCases);

        // When
        var response = projectService.getSelected('b35873ba-4007-40ac-9936-975eab38395a-3f', 'inbox');

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
    var yesterday, tomorrow, SfCaseClass;

    beforeEach(inject(function(projectService, SfCase) {

        SfCaseClass = SfCase;

        yesterday = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
    }));

    describe('overdueDays', function() {
      it('is overdue by 1 if dueDate is yesterday', function() {
        var object = new SfCaseClass({dueDate: yesterday.toString()});
        expect(object.overdueDays()).toEqual(1);
      });

      it('is not overdue (0) if dueDate is tomorrow', function() {
        var object = new SfCaseClass({dueDate: tomorrow.toString()});
        //object.dueDate = tomorrow.toString();
        expect(object.overdueDays()).toEqual(0);
      });

      it('is overdue for an old string date', function() {
        var object = new SfCaseClass({dueDate: '2013-01-24T12:04:34.220Z'});
        // var object = SfCaseClass({dueDate: '2013-01-24T12:04:34.220Z'});
        //object.dueDate = '2013-01-24T12:04:34.220Z';
        expect(object.overdueDays()).toBeGreaterThan(6);
      });

    });

    describe('overdueStatus', function() {
      it('is unset if dueDate is unset', function() {
        var object = new SfCaseClass({dueDate: null});
        //object.dueDate = null;
        expect(object.overdueStatus()).toEqual('unset');
      });

      it('is overdue if overdue:)', function() {
        var object = new SfCaseClass({dueDate: yesterday.toString()});
        //object.dueDate = yesterday;
        expect(object.overdueStatus()).toEqual('overdue');
      });

      it('is set if dueDate is set but not overdue', function() {
        var object = new SfCaseClass({dueDate: tomorrow.toString()});
        //object.dueDate = tomorrow;
        expect(object.overdueStatus()).toEqual('set');
      });
    });

    describe('modificationDate', function() {
      it('is lastModifiedDate if it exists', function() {
        var object = new SfCaseClass({lastModifiedDate: 'lmd', creationDate: 'cd'});
        //object.lastModifiedDate = 'lmd';
        //object.creationDate = 'cd';
        expect(object.modificationDate()).toEqual('lmd');
      });

      it('is creationDate if lastModifiedDate is missing', function() {
        var object = new SfCaseClass({lastModifiedDate: null, creationDate: 'cd'});
        //object.lastModifiedDate = null;
        //object.creationDate = 'cd';
        expect(object.modificationDate()).toEqual('cd');
      });
    });
  });
});
