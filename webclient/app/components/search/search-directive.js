'use strict';

angular.module('sf').directive('search', function ($location, $timeout, navigationService) {
  return {
    restrict: 'E',
    templateUrl: 'components/search/search.html',
    link: function (scope, element) {
      // Translation map. Should use i18n for this.
      scope.searchTerms = {
        'skapad': 'createdOn', // +1
        'ärendetyp': 'caseType',
        'projekt': 'project',
        'etikett': 'label',
        'tilldelad': 'assignedTo',
        'namn': 'name',
        'kontaktid': 'contactId',
        'telefon': 'phoneNumber',
        'email': 'emailAddress',
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
            var $elements = $('#main-searchtext, .search-terms button, button[type="submit"]');
            var okToHide = true;

            // Only hide if none of the elements defined above have focus.
            angular.forEach($elements, function (el) {
              var $el = angular.element(el);
              if ($el.is(':focus')) {
                console.log(okToHide);
              }
            });

            // Focus has left the search terms dialog entirely.
            if (okToHide) {
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
        query = query.replace(/([a-z\xE5\xE4\xF6]+)(?=:)/gi, function (match) {
          return scope.searchTerms[match];
        });

        $location.search({'query': query});
        navigationService.linkTo('/search');
      };
    }
  };
});
