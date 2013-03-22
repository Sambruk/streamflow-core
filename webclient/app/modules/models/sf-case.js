(function() {
  'use strict';
  var sfModels = angular.module('sf.models', []);

  sfModels.factory('SfCase', function() {

    function SfCase(model, href) {
      _.extend(this, model, {href: href});
    }

    SfCase.prototype = {
      overdueDays: function() {
        var oneDay = 24*60*60*1000;
        var now = new Date();
        var dueDate = new Date(this.dueDate);
        var diff = Math.round((now.getTime() - dueDate.getTime())/(oneDay));
        console.log(this.dueDate);
        return diff > 0 ? diff : 0;
      },

      overdueStatus: function() {
        if (!this.dueDate) return 'unset';
        return this.overdueDays() > 0 ? 'overdue' : 'set';
      },

      modificationDate: function() {
        return this.lastModifiedDate || this.creationDate;
      }
    };
    return SfCase;
  });

}());
