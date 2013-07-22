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
describe("sf.services.forms", function () {
  'use strict';

  var backend = window.mockBackend;

  beforeEach(module('sf.services.forms'));

  describe("formMapperService", function(){

    describe("DateFieldValue", function() {

      var type;

      beforeEach(function(){
        type = "se.streamsource.streamflow.api.administration.form.DateFieldValue";
      });

      it("addProperties only keeps the date part", inject(function (formMapperService) {

        var field = {
          value: "2013-06-01T00:00:00.000Z",
          field: {
            fieldValue: {
              _type: type
            }
          }
        };

        formMapperService.addProperties(field);

        expect(field.value).toEqual("2013-06-01");
      }));

      it("getValue adds the time", inject(function (formMapperService) {

        var attr = { fieldType: type };

        var value = formMapperService.getValue("2013-12-24", attr);

        expect(value).toEqual("2013-12-24T00:00:00.000Z");
      }));

    });
  });
});
