/*
    Copyright 2009-2012 Jayway Products AB

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

*/
describe("sf.directives.sf-active-link", function () {
  'use strict';

  beforeEach(function() {
    module('sf.directives');
  });

  var scope, el;
  beforeEach(inject(function($rootScope, $compile) {
    var html = '<a href="#/tapir" sf-active-link>Hello</a>';
    scope = $rootScope;
    el = angular.element(html);
    $compile(el)(scope);
  }));

  it('sets the sel class for matching path', inject(function($location) {
    $location.path('/tapir');
    scope.$digest();
    var klass = el.attr('class');
    expect(klass).toMatch(/sel/);
  }));

  it('does not set sel class for non matching path', inject(function($location) {
    $location.path('/dingo');
    scope.$digest();
    var klass = el.attr('class');
    expect(klass).toNotMatch(/layout-2/);
  }));
});

