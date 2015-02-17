/*
 *
 * Copyright 2009-2014 Jayway Products AB
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

angular.module('sf')
.filter('attachmentJson', function () {
  return function (attachment) {
    var jsonParse = JSON.parse(attachment);
    return jsonParse.name;
  };
})
.filter('positive', function () {
  return function (input) {
    return input > 0 ? input : '';
  };
})
.filter('shortDate', ['$filter', function ($filter) {
  return function (input) {
    return $filter('date')(input, 'd MMM');
  };
}])
.filter('longDate', ['$filter', function ($filter) {
  return function (input) {
    return $filter('date')(input, 'yyyy-MM-dd');
  };
}])
// Date formatting á la Google Mail.
.filter('googleDate', ['$filter', function ($filter) {
  function isSameDay(date1, date2) {
    return date1.getDate() === date2.getDate() &&
           date1.getMonth() === date2.getMonth() &&
           date1.getFullYear() === date2.getFullYear();
  }

  function isSameYear(date1, date2) {
    return date1.getFullYear() === date2.getFullYear();
  }

  return function (input) {
    var today = new Date();
    var other = new Date(input);

    // Default formatting: same year
    var format = 'd MMM';

    // Change formatting if year differs or if it's the same day.
    if (!isSameYear(today, other)) {
      format = 'yyyy-MM-dd';
    } else if (isSameDay(today, other)) {
      format = 'HH:mm';
    }

    return $filter('date')(input, format);
  };
}])
.filter('dateTime', ['$filter', function ($filter) {
  return function (input) {
    return $filter('date')(input, 'yyyy-MM-dd, HH:mm');
  };
}])
.filter('translate', ['$filter', function () {
  return function (input) {

    // So far, we keep it simple by just using a lookup table
    var translation = {
      inbox: 'Inkorg',
      assignments: 'Mina ärenden',
      attachment: 'Bifogande',
      contact: 'Kontakt',
      conversation: 'Konversation',
      custom: 'custom',
      form: 'Formulär',
      system: 'System',
      systemTrace: 'systemTrace',
      successMessage: 'Hämtning lyckades',
      errorMessage: 'Hämtning misslyckades',
      'read: All': 'Läsa: Alla',
      'write: All': 'Skriva: Alla',
      'read: Project': 'Läsa: Projekt',
      'write: Project': 'Skriva: Projekt',
      'read: Organization': 'Läsa: Organisatorisk enhet',
      'write: Organization': 'Skriva: Organisatorisk enhet',
      'read: Sameoubranch': 'Läsa: Samma organisatoriska gren',
      'write: Sameoubranch': 'Skriva: Samma organisatoriska gren',
      '0 Förfallna': 'Förfallna',
      '1 Förfaller idag': 'Förfaller idag',
      '2 Förfaller imorgon': 'Förfaller imorgon',
      '3 Förfaller inom en vecka': 'Förfaller inom en vecka',
      '4 Förfaller inom en månad': 'Förfaller inom en månad',
      '5 Förfaller inom en månad': 'Förfaller om mer än en månad'
    };

    return translation[input] || input;
  };
}])
.filter('caseLogFilter', function () {
  return function (logEntries, filterArray) {
    var i, j, matchingItems = [];

    if (logEntries && filterArray) {
      // loop through the items
      for (i = 0; i < logEntries.length; i++) {

        // for each item, loop through the filter values
        for (j = 0; j < filterArray.length; j++) {

          //If the caseLogType is the same as the name of the filter
          if (logEntries[i].caseLogType === filterArray[j].filterName) {
            //Check the value of the filter
            if (filterArray[j].filterValue) {
                matchingItems.push(logEntries[i]);
            }
            break;
          }
        }
      }
    }

    return matchingItems;
  };
})
.filter('truncate', function () {
  return function (text, length, end) {
    if (isNaN(length)) {
      length = 10;
    }

    if (end === undefined) {
      end = '...';
    }

    if (text.length <= length || text.length - end.length <= length) {
      return text;
    }
    else {
      return String(text).substring(0, length-end.length) + end;
    }

  };
});

