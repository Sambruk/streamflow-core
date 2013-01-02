(function () {
  'use strict';


  var sfServices = angular.module('sf.backend.services.project', ['sf.backend.services.backend']);

  sfServices.factory('projectService', ['backendService', function (backendService) {

    return {
      getAll:function () {
        return backendService.get({
          specs:[
            {resources:'workspace'},
            {resources: 'projects'}
          ],
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(item){
              // TODO maybe filter rel='project'
              result.push({text: item.text, href: item.href});
            });
          }
        });
      }
    }
  }]);

})();
