describe("sf.backend.services.case", function () {
  'use strict';

  var backend = window.mockBackend;

  beforeEach(module('sf.backend.services.case'));

  beforeEach(inject(function (httpService, navigationService) {
    httpService.baseUrl = '';
    httpService.apiUrl = 'mock/';
    spyOn(navigationService, 'projectId').andReturn('b35873ba-4007-40ac-9936-975eab38395a-3f');
    spyOn(navigationService, 'caseType').andReturn('inbox');
    spyOn(navigationService, 'caseId').andReturn('b35873ba-4007-40ac-9936-975eab38395a-30');
  }));

  describe("caseService", function () {


    describe("getSelected", function () {

      it("returns the selected project", inject(function (caseService, $httpBackend) {
        // Given
        $httpBackend.expectGET('mock/').respond(backend.root);
        $httpBackend.expectGET('mock/workspace/').respond(backend.workspace);
        $httpBackend.expectGET('mock/workspace/projects/').respond(backend.projects);
        $httpBackend.expectGET('mock/workspace/projects/b35873ba-4007-40ac-9936-975eab38395a-3f/').respond(backend.project1);
        $httpBackend.expectGET('mock/workspace/projects/b35873ba-4007-40ac-9936-975eab38395a-3f/inbox/').respond(backend.project1Inbox);
        $httpBackend.expectGET('mock/workspace/projects/b35873ba-4007-40ac-9936-975eab38395a-3f/inbox/cases').respond(backend.project1InboxCases);
        $httpBackend.expectGET('/api/workspace/cases/b35873ba-4007-40ac-9936-975eab38395a-30/').respond(backend.case1);
        
        // When
        var response = caseService.getSelected();

        // Then
        expect(response.length).toEqual(0);

        // When
        $httpBackend.flush();

        // Then
        expect(response.index.id).toEqual('0f0008c2-4d6e-453a-b255-0d6ec86145f9-2');
        expect(response.index.text).toEqual('formulärstest');
      }));
    });

   describe("getSelectedContacts", function() {
     it("returns the selected project", inject(function (caseService, $httpBackend) {
       // Given
       $httpBackend.expectGET('mock/').respond(backend.root);
       $httpBackend.expectGET('mock/workspace/').respond(backend.workspace);
       $httpBackend.expectGET('mock/workspace/projects/').respond(backend.projects);
       $httpBackend.expectGET('mock/workspace/projects/b35873ba-4007-40ac-9936-975eab38395a-3f/').respond(backend.project1);
       $httpBackend.expectGET('mock/workspace/projects/b35873ba-4007-40ac-9936-975eab38395a-3f/inbox/').respond(backend.project1Inbox);
       $httpBackend.expectGET('mock/workspace/projects/b35873ba-4007-40ac-9936-975eab38395a-3f/inbox/cases').respond(backend.project1InboxCases);
       $httpBackend.expectGET('/api/workspace/cases/b35873ba-4007-40ac-9936-975eab38395a-30/').respond(backend.case1);
       $httpBackend.expectGET('/api/workspace/cases/b35873ba-4007-40ac-9936-975eab38395a-30/contacts/').respond(backend.contact1);

       // When
       var response = caseService.getSelectedContacts();

       // Then
       expect(response.length).toEqual(0);

       // When
       $httpBackend.flush();

       // Then
       expect(response.length).toEqual(2);
       expect(response[0].name).toEqual("Frida Kranstege");
     }));

   })
  });
});
