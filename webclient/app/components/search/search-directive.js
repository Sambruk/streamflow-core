'use strict';

angular.module('sf').directive('search', function ($location, $timeout, navigationService) {
  return {
    restrict: 'E',
    templateUrl: 'components/search/search.html',
    link: function (scope, element) {
      // Translation map. Should use i18n for this.
      scope.searchTerms = {
        'skapad': 'createdOn',
        'ärendetyp': 'caseType',
        'projekt': 'project',
        'etikett': 'label',
        'tilldelad': 'assignedTo',
        'namn': 'name',
        'kontaktid': 'contactId',
        'telefon': 'phoneNumber',
        'email': 'emailAddress',
        'idag': 'today',
        'igår': 'yesterday',
        'timme': 'hour',
        'vecka': 'week',
        'mig': 'me',
        'skapadav': 'createdBy'
      };

      scope.toggleSearchTerms = function (visible) {
        if (visible) {
          scope.showSearchTerms = true;
        } else {
          // Delay the hiding of search terms since we might have clicked a
          // search term button, which would trigger a blur on the input field,
          // hiding it immediately.
          $timeout(function () {
            if (!element.find('#main-searchtext').is(':focus')) {
              scope.showSearchTerms = false;
            }
          }, 500);
        }
      };

      scope.addSearchTermToQuery = function ($event) {
        // Get key from search term value.
        var searchTerm = $event.currentTarget.innerHTML + ':';

        // Don't let query be undefined.
        scope.query = scope.query || '';

        // Add a whitespace to seperate from previous search value.
        scope.query += (scope.query.length === 0) ? searchTerm : ' ' + searchTerm;

        // Defer focus of text field to next cycle to avoid problems with
        // $apply. There must be a better way to do this?
        _.defer(function () {
          element.find('#main-searchtext').focus();
        });
      };

      scope.search = function (query) {
        // Replace any search term with its proper API equivalent.
        query = query.replace(/(\w+)(?=:)/gi, function (match) {
          return scope.searchTerms[match];
        });

        $location.search({'query': query});
        navigationService.linkTo('/search');
      };
    }
  };
});
