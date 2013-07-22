(function() {
  'use strict';

  describe("project list", function() {
    beforeEach(function() {
      browser().navigateTo('../../app/index.html');
    });

    it("should display the title of an issue", function() {
      element('.open-toolbar').click();
      // This selects the last(!) project type in the projects list
      element(".sub-category a").click();
      element(".issue-list a").click();

      expect(element(".issue-description").text()).toMatch("Acceptance test");
    });
  });

})();


