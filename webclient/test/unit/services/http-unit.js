describe("sf.backend.services.http", function () {
  'use strict';

  var backend = window.mockBackend;

  beforeEach(module('sf.backend.services.http'));

  describe('httpService', function () {
    beforeEach(inject(function ($location) {
      spyOn($location, 'absUrl').andReturn("http://localhost:8000/app/index.html#/customers/197606030001");
      spyOn($location, 'path').andReturn("/customers/197606030001");
    }));

    it("can get the entry point", inject(function (httpService) {
        expect(httpService.apiUrl).toEqual("http://localhost:8000/app/api/proxy/");
      }
    ));

    it("can get data from server", inject(function(httpService, $httpBackend){
      httpService.apiUrl = "mock/";

      $httpBackend.expectGET('mock/customer1/').respond(backend.customer);

      httpService.getRequest("customer1/").then (function(data) {
        expect(data.data.resources.length).toBe(3);
      });
      $httpBackend.flush();

    }));


    it("does cache requests", inject(function(httpService, $httpBackend) {
      httpService.apiUrl = "";
      expect(httpService.isCached('bla/a/')).toBe(false);
      $httpBackend.expectGET("bla/a/").respond(backend.customer);

      httpService.getRequest("bla/a/").then(function() {
        expect(httpService.isCached('bla/a/')).toBe(true);
      });
      $httpBackend.flush();
    }));

    it("can empty the cache", inject(function(httpService, $httpBackend) {
      httpService.apiUrl = "";
      $httpBackend.expectGET("bla/a/").respond(backend.customer);

      httpService.getRequest("bla/a/");
      $httpBackend.flush();
      expect(httpService.isCached('bla/a/')).toBe(true);

      // when
      httpService.invalidate(["bla/a/"]);

      // then
      expect(httpService.isCached('bla/a/')).toBe(false);

    }));
  });
});

