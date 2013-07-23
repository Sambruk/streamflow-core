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

    describe("ComboBoxFieldValue", function() {

      var type;

      beforeEach(function(){
        type = "se.streamsource.streamflow.api.administration.form.ComboBoxFieldValue";
      });

      it("addProperties maps to name and value pairs", inject(function (formMapperService) {

        var field = {
          field: {
            fieldValue: {
              _type: type,
              values: ["one", "two", "three"]
            }
          }
        };

        formMapperService.addProperties(field);

        expect(field.field.fieldValue.options[0].name).toEqual("one");
        expect(field.field.fieldValue.options[0].value).toEqual("one");
      }));

      it("getValue does nothing", inject(function (formMapperService) {

        var attr = { fieldType: type };

        var value = formMapperService.getValue("two", attr);

        expect(value).toEqual("two");
      }));

    });

    describe("ListBoxFieldValue", function() {

      var type;

      beforeEach(function(){
        type = "se.streamsource.streamflow.api.administration.form.ListBoxFieldValue";
      });

      it("addProperties maps to name and value pairs and unescapes comma values", inject(function (formMapperService) {

        var field = {
          value: "one, [three, last one]",
          field: {
            fieldValue: {
              _type: type,
              values: ["one", "two", "three, last one"]
            }
          }
        };

        formMapperService.addProperties(field);

        expect(field.field.fieldValue.options[0].name).toEqual("one");
        expect(field.field.fieldValue.options[0].value).toEqual("one");

        expect(field.value).toEqual(["one", "three, last one"]);
      }));

      it("getValue escapes values containing commas", inject(function (formMapperService) {

        var attr = { fieldType: type };

        var value = formMapperService.getValue(["one", "three, last one"], attr);

        expect(value).toEqual("one, [three, last one]");
      }));

    });

    describe("CheckboxesFieldValue", function() {

      var type;

      beforeEach(function(){
        type = "se.streamsource.streamflow.api.administration.form.CheckboxesFieldValue";
      });

      it("addProperties checks the chosen values", inject(function (formMapperService) {

        var field = {
          value: "one, [three, last one]",
          field: {
            fieldValue: {
              _type: type,
              values: ["one", "two", "three, last one"]
            }
          }
        };

        formMapperService.addProperties(field);

        expect(field.field.fieldValue.checkings[0].name).toEqual("one");
        expect(field.field.fieldValue.checkings[0].checked).toEqual(true);
      }));

      xit("getValue joins the checked values", inject(function (formMapperService) {
        // TODO: inject stubbed $parse, but how?
      }));

    });

    describe("OpenSelectionFieldValue", function() {

      var type;

      beforeEach(function(){
        type = "se.streamsource.streamflow.api.administration.form.OpenSelectionFieldValue";
      });

      it("addProperties maps to name value pairs", inject(function (formMapperService) {

        var field = {
          value: "two",
          field: {
            fieldValue: {
              _type: type,
              values: ["one", "two", "three"]
            }
          }
        };

        formMapperService.addProperties(field);

        expect(field.field.fieldValue.extendedValues[0].display).toEqual("one");
        expect(field.field.fieldValue.extendedValues[0].value).toEqual("one");
      }));

      it("addProperties assigns the saved value to the extra item if the value is not found in the options", inject(function (formMapperService) {

        var field = {
          value: "I was written by the user",
          field: {
            fieldValue: {
              _type: type,
              values: ["one", "two", "three"],
              openSelectionName: "Other"
            }
          }
        };

        formMapperService.addProperties(field);

        expect(field.field.fieldValue.extendedValues[3].display).toEqual("Other");
        expect(field.field.fieldValue.extendedValues[3].value).toEqual("I was written by the user");
      }));

      it("addProperties assign the extra item no value if the saved value is found in the options", inject(function (formMapperService) {

        var field = {
          value: "two",
          field: {
            fieldValue: {
              _type: type,
              values: ["one", "two", "three"],
              openSelectionName: "Other"
            }
          }
        };

        formMapperService.addProperties(field);

        expect(field.field.fieldValue.extendedValues[3].display).toEqual("Other");
        expect(field.field.fieldValue.extendedValues[3].value).toEqual(undefined);
      }));


    });
   

  });
});
