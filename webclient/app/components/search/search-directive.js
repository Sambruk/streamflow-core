'use strict';

angular.module('sf').directive('search', function ($location, navigationService) {
  return {
    restrict: 'E',
    templateUrl: 'components/search/search.html',
    link: function (scope) {
      scope.searchTerms = {
        'createdOn': 'skapad',
        'caseType': 'ärendetyp',
        'project': 'projekt',
        'label': 'etikett',
        'assignedTo': 'tilldelad',
        'name': 'namn',
        'contactId': 'kontakdid',
        'phoneNumber': 'telefon',
        'emailAddress': 'email',
        'today': 'idag',
        'yesterday': 'igår',
        'hour': 'timme',
        'week': 'vecka',
        'me': 'mig',
        'createdBy': 'skapadav'
      };

      scope.addSearchTermToQuery = function (event) {
        var searchTerm = _.invert(scope.searchTerms)[event.currentTarget.innerHTML] + ':';
        scope.query = scope.query || '';
        scope.query += (scope.query.length === 0) ? searchTerm : ' ' + searchTerm;
      };

      scope.search = function (query) {
        $location.search({'query': query});
        navigationService.linkTo('/search');
      };
    }
  };
});
