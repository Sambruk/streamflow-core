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

'use strict';
describe("sf.filters", function () {

  beforeEach(function() {
    module('sf');
  });

  describe('positive', function() {
    var filter;
    beforeEach(inject(function($filter) {
      filter = $filter('positive');
    }));

    it('returns the number for positive numbers', function() {
      expect(filter(5)).toEqual(5);
    });
    it('returns empty for zero', function() {
      expect(filter(0)).toEqual('');
    });
  });

  describe('shortDate', function() {
    var filter;
    beforeEach(inject(function($filter) {
      filter = $filter('shortDate');
    }));

    it('returns the short format of the date', function() {
      var date = new Date(2012, 9, 4);
      expect(filter(date)).toEqual('10/04');
    });
  });

});