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
describe("sf.backend.services.project", function () {
  'use strict';

  var backend = window.mockBackend;

  beforeEach(module('sf.backend.services.project'));

  beforeEach(inject(function(httpService) {
    httpService.apiUrl = 'mock/';
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
      }))

    });
  });
});
