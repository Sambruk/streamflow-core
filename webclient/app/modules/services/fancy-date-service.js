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
(function () {
  'use strict';

	var sfServices = angular.module('sf.services.fancy-date', []);

 	sfServices.factory('fancyDateService', [function () {
    moment.locale('sv');

    return {
      format: function (value) {
        var formatString = 'YYYY-MM-DD',
          result = '';

        switch (value) {
          case moment().format(formatString):
            result = 'I dag';
            break;
          case moment().add(1, 'days').format(formatString):
            result = 'I morgon';
            break;
          case '':
            result = '';
            break;
          default:
            result = moment(value).fromNow();
            break;
        }

        return result;
      }
  };
 }]);
})();