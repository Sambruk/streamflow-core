Feature: Open Inbox (Example of a feature)
  As a Streamflow user
  I want to be able to open my inbox
  So that I can see if some new issues have come

  
 Background:
 Given I logged in as 'administrator'

 @smoke
 Scenario: I am able to open my Inbox and edit description of the selected case
    Given I clicked button with name 'btnSelectContext'
    #Given I clicked button with index '0'
    When I select Inbox under group 'Avfall' 
    Then I will see label with text '  Avfall : Inbox '
    When I select case in the table with index '0'
    #When I select case in the table with text 'ärendet är anmält'
    #Then the selected case and the text field with index '1' will match
    Then the selected case and the text field with name 'txtCaseDescription' will match
    When I set text field with name 'txtCaseDescription' to 'Cuke Testing is cool'
    	And I click button with text 'Refresh'
    	And I select case in the table with index '0'
    Then the selected case and the text field with name 'txtCaseDescription' will match
    When I set text field with name 'txtCaseDescription' to old case description
    	And I click button with text 'Refresh'
    	And I select case in the table with index '0'
    Then the selected case and the text field with name 'txtCaseDescription' will match
    
 @work
 Scenario: I can open Search
    Given I clicked button with name 'btnSelectContext'
    When I select Context item with index '2' 
    Then I will see label with text '  Search '
    
    
