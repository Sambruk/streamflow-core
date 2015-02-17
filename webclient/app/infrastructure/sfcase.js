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

angular.module('sf').factory('SfCase', function() {
  function SfCase(model, href) {
    _.extend(this, model, {href: href});
  }

  SfCase.prototype = {
    overdueDays: function() {
      var oneDay = 24*60*60*1000;
      var now = new Date();
      var dueOn = new Date(this.dueOn);
      var diff = Math.floor((now.getTime() - dueOn.getTime())/(oneDay));
      return diff > 0 ? diff : 0;
    },

    checkdueDay: function() {
      var oneDay = 24*60*60*1000;
      var now = new Date();
      var dueOn = new Date(this.dueOn);
      var diff = Math.floor((now.getTime() - dueOn.getTime())/(oneDay));
      return diff;
    },

    overdueStatus: function() {
      if (!this.dueOn) {
        return 'unset';
      }
      return this.overdueDays() > 0 ? 'overdue' : 'set';
    },

    modificationDate: function() {
      return this.lastModifiedDate || this.creationDate;
    }
  };
  return SfCase;
});
