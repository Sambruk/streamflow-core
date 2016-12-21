/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.account;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.ValueBinder;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * JAVADOC
 */
public class ProfileView
      extends JScrollPane
      implements TransactionListener, Refreshable
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private ProfileModel model;

   private ValueBinder profileBinder;
   private ValueBinder phoneNumberBinder;
   private ValueBinder emailBinder;

   private ActionBinder actionBinder;

   private JPanel profileForm;
   private JRadioButton noneButton;
   private JRadioButton emailButton;

   private JTextField name;
   private JTextField emailAddress;
   private JTextField phoneNumber;

   private JTextField markReadTimeout;
   private JTextArea mailFooter;


   public ProfileView(@Service ApplicationContext context, @Uses ProfileModel model, @Structure Module module)
   {
      ApplicationActionMap am = context.getActionMap(this);
      setActionMap(am);

      this.model = model;

      actionBinder = module.objectBuilderFactory().newObjectBuilder( ActionBinder.class ).use( am ).newInstance();
      actionBinder.setResourceMap( context.getResourceMap( getClass() ) );

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

      profileForm = new JPanel();
      panel.add( profileForm, BorderLayout.CENTER);
      FormLayout profileLayout = new FormLayout("85dlu, 5dlu, 120dlu:grow",
            "pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref");

      profileBinder = module.objectBuilderFactory().newObject( ValueBinder.class );
      phoneNumberBinder = module.objectBuilderFactory().newObject( ValueBinder.class );
      emailBinder = module.objectBuilderFactory().newObject( ValueBinder.class );

      DefaultFormBuilder profileBuilder = new DefaultFormBuilder(profileLayout,
            profileForm );

      JLabel title = new JLabel(i18n
            .text( AccountResources.contact_info_for_user_separator));
      title.setFont( title.getFont().deriveFont( Font.BOLD ) );
      profileBuilder.append( title, 3 );
      profileBuilder.nextLine();

      profileBuilder.add( new JLabel( i18n.text( WorkspaceResources.name_label ) ) );
      profileBuilder.nextColumn( 2 );
      profileBuilder.add( profileBinder.bind( "name", actionBinder.bind( "changeName", name = new JTextField() ) ) );
      profileBuilder.nextLine();

      profileBuilder.add( new JLabel( i18n.text( WorkspaceResources.email_label ) ) );
      profileBuilder.nextColumn( 2 );
      profileBuilder.add( emailBinder.bind( "emailAddress", actionBinder.bind( "changeEmailAddress", emailAddress = new JTextField() ) ) );
      profileBuilder.nextLine();

      profileBuilder.add( new JLabel( i18n.text( WorkspaceResources.phone_label ) ) );
      profileBuilder.nextColumn( 2 );
      profileBuilder.add( phoneNumberBinder.bind( "phoneNumber", actionBinder.bind( "changePhoneNumber", phoneNumber = new JTextField() ) ) );
      profileBuilder.nextLine( 2 );

      profileBuilder.add( new JLabel( i18n
            .text( WorkspaceResources.choose_message_delivery_type ) ) );
      profileBuilder.nextColumn( 2 );
      profileBuilder.add( profileBinder.bind( "messageDeliveryType", actionBinder.bind( "messageDeliveryTypeNone", noneButton = new JRadioButton( ) ) ) );
      profileBuilder.nextLine();
      profileBuilder.nextColumn( 2 );
      profileBuilder.add( profileBinder.bind( "messageDeliveryType", actionBinder.bind( "messageDeliveryTypeEmail", emailButton = new JRadioButton(  ) ) ) );

      noneButton.setAction( am.get( "messageDeliveryTypeNone" ) );
      emailButton.setAction( am.get( "messageDeliveryTypeEmail" ) );
      noneButton.setActionCommand( "none" );
      emailButton.setActionCommand( "email" );
      // Group the radio buttons.
      ButtonGroup group = new ButtonGroup();
      group.add(noneButton);
      group.add( emailButton );

      profileBuilder.nextLine( 2 );

      profileBuilder.add( new JLabel( i18n.text( WorkspaceResources.mark_read_timeout ) ) );
      profileBuilder.nextColumn( 2 );
      profileBuilder.add( profileBinder.bind( "markReadTimeout", actionBinder.bind( "changeMarkReadTimeout", markReadTimeout = new JTextField() ) ) );
      profileBuilder.nextLine( 2 );

      profileBuilder.add( new JLabel( i18n.text( WorkspaceResources.mail_footer ) ) );
      profileBuilder.nextColumn( 2 );

      mailFooter = new JTextArea(10,30);
      profileBuilder.add( profileBinder.bind( "mailFooter", actionBinder.bind( "changeMailFooter", mailFooter )) );

      profileBuilder.nextLine();

      setViewportView(panel);

      new RefreshWhenShowing(this, this);
   }

   @Action
   public Task messageDeliveryTypeNone() throws Exception
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.changeMessageDeliveryType("none");
         }
      };
   }

   @Action
   public Task messageDeliveryTypeEmail() throws Exception
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.changeMessageDeliveryType("email");
         }
      };
   }


   @Action
   public Task changeName()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.changeName( name.getText() );
         }
      };
   }

   @Action
   public Task changeEmailAddress()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.changeEmailAddress( emailAddress.getText() );
         }
      };
   }

   @Action
   public Task changePhoneNumber()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.changePhoneNumber( phoneNumber.getText() );
         }
      };
   }

   @Action
   public Task changeMarkReadTimeout()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.changeMarkReadTimeout( markReadTimeout.getText() );
         }
      };
   }

   @Action
   public Task changeMailFooter()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.changeMailFooter( mailFooter.getText( ) );
         }
      };
   }

   public void refresh()
   {
      model.refresh();

      profileBinder.update( model.getIndex() );
      emailBinder.update( model.getEmailAddress() );
      phoneNumberBinder.update( model.getPhoneNumber() );

   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (matches( withNames( "changedTimeout", "updatedContact", "changedMessageDeliveryType", "changedDescription", "changedMailFooter"),
            transactions ))
      {
         refresh();
      }
   }
}