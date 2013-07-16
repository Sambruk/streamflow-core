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

  sfServices.factory('formMapperService', [function () {

    return {
      addProperties: function(field) {
        function addOptions(fieldValue){
          var options = _.map(field.field.fieldValue.values, function(value){
            return {name: value, value: value}
          });

          fieldValue.options = options;
        }

        if (field.field.fieldValue._type === "se.streamsource.streamflow.api.administration.form.ComboBoxFieldValue") {
          addOptions(field.field.fieldValue);
        }

        if (field.field.fieldValue._type === "se.streamsource.streamflow.api.administration.form.CheckboxesFieldValue") {
          var checkings = _.map(field.field.fieldValue.values, function(value){
            return {name: value, checked: field.value && field.value.indexOf(value) != -1};
          });

          field.field.fieldValue.checkings = checkings;
        }

        if (field.field.fieldValue._type === "se.streamsource.streamflow.api.administration.form.ListBoxFieldValue") {

          addOptions(field.field.fieldValue);

          if (field.value) {
            var escapedValue = field.value.replace(/\[(.*),(.*)\]/, "$1" + encodeURIComponent(",")  + "$2");
            var values = _.map(escapedValue.split(", "), function(espaced){
              return decodeURIComponent(espaced);
            });

            field.value = values;
          }
        }

        if (field.field.fieldValue._type === "se.streamsource.streamflow.api.administration.form.DateFieldValue") {
          if (field.value)
            field.value = field.value.split("T")[0];
        }

        if (field.field.fieldValue._type === "se.streamsource.streamflow.api.administration.form.NumberFieldValue") {
          var regex;
          if (field.field.fieldValue.integer) {
             regex = /^\d+$/; // Integer
          }
          else {
            regex = /^(\d+(?:[\.\,]\d*)?)$/ // Possible decimal, with . or ,
          }

          field.field.fieldValue.regularExpression = regex;
        }

        if (field.field.fieldValue._type === "se.streamsource.streamflow.api.administration.form.TextFieldValue") {

          if (!field.field.fieldValue.regularExpression) {
            field.field.fieldValue.regularExpression = /(?:)/;
          }
          else {
            field.field.fieldValue.regularExpression = new RegExp(field.field.fieldValue.regularExpression)
          }
        }

        if (field.field.fieldValue._type === "se.streamsource.streamflow.api.administration.form.OpenSelectionFieldValue") {

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
      },
      getValue: function() {
        return "bar";
      },
    };
  }]);


})();
