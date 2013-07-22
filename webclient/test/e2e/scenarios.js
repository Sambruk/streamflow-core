(function() {
  'use strict';

  /* README

  These tests uses the actual test data on the server!
  And they are quite "fragile", since on must do sleep(1) between each click - unclear why.
  jQuery is included in runner.html, but the actual SUT is inside an iFrame, so one must do i.e.
     $(".m-issue-forms form .select:eq(0)", $("iframe").contents())
  ...to query the actual page.

  These tests should only be used as smoke tests and they help you see if navigation between pages break.
  */


  describe("Issue view", function() {
    beforeEach(function() {
      browser().navigateTo('../../app/index.html');

      element('.open-toolbar').click();
      element(".sub-category a:eq(1)").click();
      element(".issue-list a:eq(0)").click();
    });

    it("should display the title", function() {
      expect(element(".issue-description").text()).toMatch("Acceptance test");
    });

    it("should display the issue type", function() {
      expect(input("general[0].caseType.text").val()).toMatch("Abonnentservice");
    });

    it("should display a form", function() {
      element(".m-issue-forms .form-tabs a:eq(3)").click();
      sleep(1);
      expect(element(".m-issue-forms form .text:eq(0)").text()).toMatch("Text");
    });

    it("should display a form", function() {
      element(".m-issue-forms .form-tabs a:eq(3)").click();
      sleep(1);
      expect(element(".m-issue-forms form .text:eq(0)").text()).toMatch("Text");
    });

    it("should be possible to navigate to the second form page", function() {
      element(".m-issue-forms .form-tabs a:eq(3)").click();
      sleep(1);
      element(".m-issue-forms .form-sections li:eq(1) a:eq(0)").click();
      sleep(1);
      element(".m-issue-forms .btn:eq(1)").click();
      sleep(1);
      expect(element(".m-issue-forms form .select:eq(0)").text()).toMatch("Listbox");
    });

  });

})();


