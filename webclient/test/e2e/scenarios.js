(function() {
  'use strict';

  describe("project list", function() {
    beforeEach(function() {
      browser().navigateTo('../../app/index.html');
    });

    it("should list all available projects", function() {
      expect(repeater('.projects').column('project.text')).toEqual(["Streamflow","Kontaktcenter"]);
    });


    it("should display the case list view when clicking on a project", function() {
      element('.project-type:first-child a:first-child').click();
      expect(browser().location().url()).toBe("/b35873ba-4007-40ac-9936-975eab38395a-ff/inbox");
    });
  });

  describe("when first project inbox is chosen", function() {
    beforeEach(function() {
      browser().navigateTo('../../app/index.html#/b35873ba-4007-40ac-9936-975eab38395a-ff/inbox');
    });

    it("should display the cases for selected project and inbox", function() {
      expect(repeater('.case-item').column('case.text')).toEqual(["Test Test Test 2","Kontakt Center Thingy"]);
    });

  });
})();


