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
(function () {
  'use strict';


  var sfServices = angular.module('sf.services.forms', []);

  sfServices.factory('formMapperService', ['$parse', function ($parse) {

    function addOptions(fieldValue){
      var options = _.map(fieldValue.values, function(value){
        return {name: value, value: value}
      });

      fieldValue.options = options;
    }

    var mappings = {
      "se.streamsource.streamflow.api.administration.form.ComboBoxFieldValue": {
        addProperties: function(field){
          addOptions(field.field.fieldValue);
        }
      },
      "se.streamsource.streamflow.api.administration.form.DateFieldValue": {
        addProperties: function(field){
          if (field.value)
            field.value = field.value.split("T")[0];
        },
        getValue: function(value, attr){
          return value + "T00:00:00.000Z";
        }
      },
      "se.streamsource.streamflow.api.administration.form.CheckboxesFieldValue": {
        addProperties: function(field){
          var checkings = _.map(field.field.fieldValue.values, function(value){
            return {name: value, checked: field.value && field.value.indexOf(value) != -1};
          });

          field.field.fieldValue.checkings = checkings;
        },
        getValue: function(value, attr){
          var checked = _.chain($parse(attr.backingField)())
            .filter(function(input){
              return input.checked;
            }).map(function(input){
              return input.name
            }).value();

          return checked.join(", ");
        }
      },
      "se.streamsource.streamflow.api.administration.form.ListBoxFieldValue": {
        addProperties: function(field){
          addOptions(field.field.fieldValue);

          if (field.value) {
            var escapedValue = field.value.replace(/\[(.*),(.*)\]/, "$1" + encodeURIComponent(",")  + "$2");
            var values = _.map(escapedValue.split(", "), function(espaced){
              return decodeURIComponent(espaced);
            });

            field.value = values;
          }
        },
        getValue: function(value, attr){
          var espacedValues = _.map(value, function(value){
            return value.indexOf(",") !== -1 ? "[" + value + "]" : value;
          });

          return espacedValues.join(", ");
        }
      },
      "se.streamsource.streamflow.api.administration.form.NumberFieldValue": {
        addProperties: function(field){
          var regex;
          if (field.field.fieldValue.integer) {
             regex = /^\d+$/; // Integer
          }
          else {
            regex = /^(\d+(?:[\.\,]\d*)?)$/ // Possible decimal, with . or ,
          }

          field.field.fieldValue.regularExpression = regex;
        }
      },
      "se.streamsource.streamflow.api.administration.form.TextFieldValue": {
        addProperties: function(field){
          if (!field.field.fieldValue.regularExpression) {
            field.field.fieldValue.regularExpression = /(?:)/;
          }
          else {
            field.field.fieldValue.regularExpression = new RegExp(field.field.fieldValue.regularExpression)
          }
        }
      },
      "se.streamsource.streamflow.api.administration.form.OpenSelectionFieldValue": {
        addProperties: function(field){
          field.field.fieldValue.extendedValues = _.map(field.field.fieldValue.values, function(value){
            return {
              value: value,
              display: value
            };
          });

          var value;
          if (field.field.fieldValue.values.indexOf(field.value) == -1) {
            value = field.value
          }

          field.field.fieldValue.extendedValues.push({
            value: value,
            display: field.field.fieldValue.openSelectionName
          });
        }
      }
    };

    return {
      addProperties: function(field) {

        var mapper = mappings[field.field.fieldValue._type];
        if (mapper && mapper.addProperties) {
          mapper.addProperties(field);
        }
      },
      getValue: function(value, attr) {
        if (attr.fieldType === "se.streamsource.streamflow.api.administration.form.DateFieldValue") {
          var mapper = mappings[attr.fieldType];
          if (mapper && mapper.getValue) {
            return mapper.getValue(value, attr);
          }
        }

        if (attr.fieldType === "se.streamsource.streamflow.api.administration.form.CheckboxesFieldValue") {
          var mapper = mappings[attr.fieldType];
          if (mapper && mapper.getValue) {
            return mapper.getValue(value, attr);
          }
        }

        if (attr.fieldType === "se.streamsource.streamflow.api.administration.form.ListBoxFieldValue") {
          var mapper = mappings[attr.fieldType];
          if (mapper && mapper.getValue) {
            return mapper.getValue(value, attr);
          }
        }

        return value;
      },
    };
  }]);

})();
