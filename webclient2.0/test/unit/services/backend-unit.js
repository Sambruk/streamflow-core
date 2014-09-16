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

'use strict';
describe("sf.services.backend", function () {

  var backend = window.mockBackend;

  beforeEach(module('sf'));

  // mock the error handler
  var error;
  beforeEach(module(function($provide) {
    //error = undefined;
    $provide.value('errorHandlerService', function(e) { error = e;});
  }));


  beforeEach(inject(function (httpService) {
    httpService.apiUrl = "mock/";
  }));


  describe("get", function() {

    describe('backendService', function () {
      // TODO: Fix test
      xit("calls the error handler when a resource is unavailable", inject(function (backendService, $httpBackend) {
        $httpBackend.expectGET('mock/').respond(backend.customer);
        $httpBackend.expectGET('mock/open/').respond(backend.open);
        $httpBackend.expectGET('mock/open/cases').respond(404, 'oops');

        var self = this;
        var result = backendService.get({
          specs: [{resources:'open'},{queries:'cases'}],
          onSuccess: function () { }
          });
        $httpBackend.flush();
        expect(error.status).toBe(404);
      }));

      it("can chain several request in one go", inject(function (backendService, $httpBackend) {
        $httpBackend.expectGET('mock/').respond(backend.customer);
        $httpBackend.expectGET('mock/open/').respond(backend.open);
        $httpBackend.expectGET('mock/open/cases').respond(backend.openCases);

        var result = backendService.get({
          specs: [{resources:'open'},{queries:'cases'}],
          onSuccess: function (resource, result) {
            expect(resource.response.links.length).toEqual(2);
            expect(resource.response.links[0].href).toEqual('f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-0/');
            result.foo = 42;
          }});
        $httpBackend.flush();
        expect(result.foo).toBe(42);
      }));

      it("can get one resource with empty specs", inject(function (backendService, $httpBackend) {
        $httpBackend.expectGET('mock/').respond(backend.customer);

        var result = backendService.get({specs: [], onSuccess: function (resource, result) {
          expect(resource.response.resources.length).toEqual(3);
          result.foo = "bar";
        }});
        $httpBackend.flush();
        expect(result.foo).toBe('bar');
      }));

      it("can get return arrays", inject(function (backendService, $httpBackend) {
        $httpBackend.expectGET('mock/').respond(backend.customer);

        var result = backendService.get({specs: [], onSuccess: function (resource, result) {
          result.push("hej");
        }});
        $httpBackend.flush();
        expect(result[0]).toBe('hej');
      }));

      it("can use functions as specs", inject(function(backendService, httpService, $httpBackend) {
        $httpBackend.expectGET('mock/').respond(backend.conversation1Messages);
        $httpBackend.expectGET('mock/f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-17/').respond(backend.conversation1Messages);

        // when
        var r = backendService.get({
          specs: [ {'index.links':function() { return 'f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-17'}}],
          onSuccess: function(resource, result) { resource.response.index.links.forEach(function(i){ result.push(i)})}
        });
        $httpBackend.flush();
        expect(r.length).toBe(3);

      }));


      it("resolve can be prevented with a condition", inject(function(backendService, httpService, $httpBackend) {
        var flag = false;

        // when
        var r = backendService.get({
          specs: [ {'index.links':function() { return 'f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-17'}}],
          condition: function() { return flag},
          onSuccess: function(resource, result) { resource.response.index.links.forEach(function(i){ result.push(i)})}
        });
        expect(r.length).toBe(0);
        flag = true;

        $httpBackend.expectGET('mock/').respond(backend.conversation1Messages);
        $httpBackend.expectGET('mock/f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-17/').respond(backend.conversation1Messages);

        r.resolve();
        $httpBackend.flush();

        expect(r.length).toBe(3);
      }));


      it("can resolve the result again", inject(function(backendService, httpService, $httpBackend) {
        $httpBackend.expectGET('mock/').respond(backend.customer);

        var calls = 0;
        var result = backendService.get({specs: [], onSuccess: function (resource, result) {
          result.push("hej");
          calls += 1;
        }});
        $httpBackend.flush();
        expect(calls).toBe(1);
        expect(result.length).toBe(1);

        $httpBackend.expectGET('mock/').respond(backend.customer);
        httpService.invalidate(['']); // invalidate the root url
        result.resolve();
        $httpBackend.flush();

        expect(calls).toBe(2);
        expect(result.length).toBe(1);
      }));

      it("can return keys specified as x.y.z for finding nested keys in json", inject(function(backendService, $httpBackend) {
        $httpBackend.expectGET('mock/').respond(backend.conversation1Messages);
        $httpBackend.expectGET('mock/f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-17/').respond(backend.conversation1Messages);

        var r = backendService.get({
          specs: [ {'index.links':'f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-17'}],
          onSuccess: function (resource, result) {
            expect(resource.response.index.links.length).toEqual(3);
            expect(resource.response.index.links[0].href).toEqual('f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-17/');
            result.links = resource.response.index.links;
          }});
        $httpBackend.flush();
        expect(r.links.length).toBe(3);
      }));
    });



    describe("postNested", function() {
      it("Can post on the root resource", inject(function(backendService, $httpBackend) {
        $httpBackend.expectGET('mock/').respond(backend.profile);
        $httpBackend.expectPOST('mock/update', "name=andreas").respond("Japp");
        backendService.postNested([{commands: 'update'}], {name: 'andreas'});
        $httpBackend.flush();
      }));

      it("Can post on the root resource", inject(function(backendService, $httpBackend) {
        $httpBackend.expectGET('mock/').respond(backend.customer);
        $httpBackend.expectGET('mock/profile/').respond(backend.profile);
        $httpBackend.expectPOST('mock/profile/update', "name=andreas").respond("Japp");
        backendService.postNested([{resources: 'profile'}, {commands: 'update'}], {name: 'andreas'});
        $httpBackend.flush();
      }));

    })
  });
});

