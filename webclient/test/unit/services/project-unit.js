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
