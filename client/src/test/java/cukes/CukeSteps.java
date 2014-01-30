/**
 *
 * Copyright 2009-2013 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cukes;

import cuke4duke.annotation.After;
import cuke4duke.annotation.Before;
import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.annotation.I18n.EN.When;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.Robot;
import org.fest.swing.core.matcher.FrameMatcher;
import org.fest.swing.core.matcher.JButtonMatcher;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JListFixture;
import org.fest.swing.fixture.JTableFixture;
import org.fest.swing.timing.Pause;
import org.fest.swing.timing.Timeout;
import se.streamsource.streamflow.client.MainClient;

import javax.swing.*;

import static org.fest.swing.data.TableCell.row;
import static org.fest.swing.launcher.ApplicationLauncher.application;
import static org.junit.Assert.assertTrue;


public class CukeSteps
{

   private FrameFixture workspaceFrame;
   private String selectedCase = "";
   private String oldCase = "";

   @Before
   public void startClient()
   {
      Robot robot = BasicRobot.robotWithNewAwtHierarchy();
      application(MainClient.class).start();
      FrameMatcher matcher = FrameMatcher.withTitle("Streamflow Workspace");
      workspaceFrame = WindowFinder.findFrame(matcher).withTimeout(10000).using(robot);
      workspaceFrame.show();
      workspaceFrame.maximize();
   }

   @After
   public void stopClient()
   {
      Pause.pause(1000);
      workspaceFrame.cleanUp();
      Pause.pause(1000);
   }

   @Given("I logged in as '(.*)'")
   public void loggInAs(String account)
   {
      workspaceFrame.scrollPane().requireEnabled();
      workspaceFrame.scrollPane().requireVisible();
      workspaceFrame.list().clickItem(account).requireEnabled(Timeout.timeout(10000));
   }

   @Given("I clicked button with index '(.*)'")
   public void clickButtonWithIndex(int i)
   {
      workspaceFrame.button(buttonMatcherByIndex(i)).click();
   }

   @Given("I clicked button with name '(.*)'")
   public void clickButtonWithName(String name)
   {
      workspaceFrame.button(name).click();
   }

   @When("I select Inbox under group '(.*)'")
   public void selectInboxUnderGroup(String group)
   {
      boolean found = false;
      JListFixture list = workspaceFrame.list();
      for (int i = 0; i < list.contents().length; i++)
      {
         if (list.valueAt(i) != null && list.valueAt(i).toString().matches(group))
         {
            found = true;
            list.item(i + 1).click();
            break;
         }
      }
      assertTrue("Group: '" + group + "' was not found", found);
   }

   @When("I select Context item with index '(.*)'")
   public void selectContextItemWithIndex(int index)
   {
      workspaceFrame.list().item(index).click();
   }

   @When("I select My Cases under group '(.*)'")
   public void selectMyCasesUnderGroup(String group)
   {
      boolean found = false;
      JListFixture list = workspaceFrame.list();

      for (int i = 0; i < list.contents().length; i++)
      {

         if (list.valueAt(i) != null && list.valueAt(i).toString().matches(group))
         {
            found = true;
            list.item(i + 2).click();
            break;
         }
      }
      assertTrue("Group: '" + group + "' was not found", found);
   }

   @When("I select context with index '(.*)'")
   public void selectContextByIndex(int index)
   {
      workspaceFrame.list().clickItem(index);
   }

   @When("I select case in the table with text '(.*)'")
   public void selectCaseInTheTableByText(String text)
   {
      boolean found = false;
      JTableFixture table = workspaceFrame.table();
      for (int i = 0; i < table.rowCount(); i++)
      {
         if (table.valueAt(row(i).column(0)) != null
                 && table.valueAt(row(i).column(0)).toString().contains(text))
         {
            found = true;
            table.cell(row(i).column(0)).click();
            break;
         }
         System.out.println(table.valueAt(row(i).column(0)));
      }
      assertTrue("Group: '" + text + "' was not found", found);
   }

   @When("I select case in the table with index '(.*)'")
   public void selectCaseInTheTableByText(int index)
   {
      selectedCase = workspaceFrame.table().cell(row(index).column(0)).value();
      workspaceFrame.table().cell(row(index).column(0)).click();
   }

   @When("I set text field with name '(.*)' to '(.*)'")
   public void setTextFieldWithNameTo(String name, String text)
   {
      oldCase = workspaceFrame.textBox(name).text();
      workspaceFrame.textBox(name).setText(text);
   }

   @When("I set text field with name '(.*)' to old case description")
   public void setTextFieldWithNameToOldValue(String name)
   {
      workspaceFrame.textBox(name).setText(oldCase);
   }

   @When("I click button with text '(.*)'")
   public void clickButtonWithText(String text)
   {
      JButtonMatcher matcher = JButtonMatcher.withText(text);
      workspaceFrame.button(matcher).click();
      workspaceFrame.button(matcher).requireEnabled(Timeout.timeout(10000));
      Pause.pause(1000);
   }

   @Then("I will see label with text '(.*)'")
   public void checkLabelExists(String txt)
   {
      workspaceFrame.label(labelMatcherByText(txt)).requireVisible();
   }

   @Then("the selected case and the text field with index '(.*)' will match")
   public void isSelectedCaseMatchingTextFieldWithIndex(int index)
   {
      String tf = workspaceFrame.textBox(textFieldMatcherByIndex(index)).text();
      assertTrue("Selected case and textfield text do not match", selectedCase.contains(tf));
   }

   @Then("the selected case and the text field with name '(.*)' will match")
   public void isSelectedCaseMatchingTextFieldWithName(String name)
   {
      String tf = workspaceFrame.textBox(name).text();
      assertTrue("Selected case and textfield text do not match", selectedCase.contains(tf));
   }


   /*
     * MATCHERS
     */
   private GenericTypeMatcher<JButton> buttonMatcherByIndex(final int index)
   {
      GenericTypeMatcher<JButton> matcher = new GenericTypeMatcher<JButton>(JButton.class)
      {
         private int count = -1;

         @Override
         protected boolean isMatching(JButton component)
         {
            count++;
            return count == index;
         }
      };
      return matcher;
   }

   private GenericTypeMatcher<JTextField> textFieldMatcherByIndex(final int index)
   {
      GenericTypeMatcher<JTextField> matcher = new GenericTypeMatcher<JTextField>(JTextField.class)
      {
         private int count = -1;

         @Override
         protected boolean isMatching(JTextField component)
         {
            count++;
            return count == index;
         }
      };
      return matcher;
   }

   private GenericTypeMatcher<JLabel> labelMatcherByText(final String text)
   {
      GenericTypeMatcher<JLabel> matcher = new GenericTypeMatcher<JLabel>(JLabel.class)
      {
         @Override
         protected boolean isMatching(JLabel label)
         {
            return text.equals(label.getText());
         }
      };
      return matcher;
   }

}
