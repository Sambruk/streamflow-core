BDD Tests
#########

Guidelines
**********
We are using JBehave for the BDD tests.

Some small rules
================
Here's what I have so far:

    * Setup classes must use regular Step classes to do the setup

    * Setup classes may use other Setup classes to build upon each other

    * Setup classes may NOT be used outside of the Setup classes

    * Some Step classes are for managing collections, such as ProjectsSteps, GroupsSteps, OrganizationalUnitsSteps, etc. These should have a "givenFoo" public field, and a @Given-marked method that can be used to populate the field. Example: UserSteps has the following:

        .. code-block:: java

            @Given("user named §user")
            public void givenUser(String name)
            {
                givenUser = organizationsSteps.givenOrganizations().getUserByName(
                name );
            }

        .. note::
            If another Step class needs a user, it must have been set up by the scenario by doing "Given user named foo" first, and then the UserSteps.givenUser field is accessed directly.

    * Whenever a "When" method does a create, populate the givenFoo field at the same time

    * If you have When's like this "When foo named xyz is ..." then you know something is wrong, because the "foo named xyz" should have been a Given. Refactor it.

With these basic rules I think our step and setup classes becomes much easier to write and maintain. The Given's become much more logical as well, and easier to compose into complex scenarios.

More guidelines
===============

For test setup, there is a TestSetupStep that has a bunch of Given-methods, each of which sets up test data for a specific type of thing (users, groups, projects, etc.). You can use these individually, or if you want "all of it" there is one that calls the rest, which you can use by doing this in your scenario:
Given basic setup 1

This will create all the entities. After that you will typically want to specify what entities you need for a scenario. Commonly you will want to specify which user to use. This is done in OrganizationsSteps (there are no longer any maps or given-fields in the setup classes), and you use it with this:
Given user named user1

In your When-methods you can then refer to the public field OrganizationsSteps.givenUser. Do @Uses on OrganizationsSteps to get a reference to it. Look at existing steps for examples.

This removes the need to pass in names of things in all the When-methods (i.e. there is no longer any methods that say @When("user named $name do
..."))

For structural tests, which create/add/remove, I have in most cases put add/remove into one scenario, which simplifies things a lot. Here's an
example:

    .. code-block:: plain

        Scenario: Create, add, remove group
        Given basic organizational unit setup
        Given basic user setup
        When a group named newGroup is created
        Then events groupCreated,groupAdded,descriptionChanged occurred

        When group is removed
        Then events groupRemoved,removedChanged occurred

        When group is removed
        Then no events occurred

All create @When's methods both create the entity and sets it as the givenFoo, in this case givenGroup, so that the other methods will use that if nothing else is said.

I think that's about it. Please look at the scenarios and look how they setup the state for each case. If you ever specify an object in a @When-method (e.g. include "user named $name") you are most likely doing something wrong, and should be using a Given before that method instead.
Look at the existing step-classes for examples of how they do it.

