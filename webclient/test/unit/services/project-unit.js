describe("sf.backend.services.project", function () {
  'use strict';

  var backend = window.mockBackend;

  beforeEach(module('sf.backend.services.project'));

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
  })
  });
});
