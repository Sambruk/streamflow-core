/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.task;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXDatePicker;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.MacOsUIExtension;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.NotificationGlassPane;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.conversation.ConversationDetailDTO;
import se.streamsource.streamflow.resource.conversation.MessageDTO;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.FormSubmitEvent;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;

public class TaskConversationView
      extends JPanel
      implements ListEventListener
{

   private ObjectBuilderFactory obf;

   private TaskConversationModel model;
   private RefreshWhenVisible refresher;
   private JTextPane messageArea;
   private ApplicationContext context;
   private JPanel bottomPanel;
   private TaskConversationParticipantsView participantView;
   private ActionMap am;
   private StateBinder newConversationBinder;
   private JTextField defaultFocusField;

   public TaskConversationView( @Service ApplicationContext context, @Structure ObjectBuilderFactory obf )
   {
      super( new CardLayout() );
      this.context = context;
      this.obf = obf;
      am = context.getActionMap( this );
      MacOsUIExtension.convertAccelerators( am );

      add( new JPanel(), "EMPTY" );
      add( initView(), "VIEW" );
      add( initNew(), "NEW" );

      refresher = new RefreshWhenVisible( this );
      addAncestorListener( refresher );
   }

   private JPanel initView()
   {
      JPanel view = new JPanel( new BorderLayout() );
      messageArea = new JTextPane();
      messageArea.setContentType( "text/html" );
      ((HTMLEditorKit) messageArea.getEditorKit()).setAutoFormSubmission( false );
      messageArea.setEditable( false );

      view.add( messageArea, BorderLayout.CENTER );

      bottomPanel = new JPanel( new CardLayout() );

      // INITIAL
      JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
      // with a button for add message and add participant
      JButton newMsg = new JButton( am.get( "showNewMessage" ) );
      JButton newParticipants = new JButton( am.get( "showParticipants" ) );
      buttonPanel.add( newMsg );
      buttonPanel.add( newParticipants );

      bottomPanel.add( buttonPanel, "INITIAL" );

      // NEWPARTICIPANT

      participantView = obf.newObjectBuilder( TaskConversationParticipantsView.class ).use( context ).newInstance();
      //participantView.setModel( model.getParticipantsModel() );
      bottomPanel.add( participantView, "NEW_PARTICIPANT" );


      // NEWMESSAGE
      JPanel messagePanel = new JPanel();
      JButton addMessage = new JButton( am.get( "addMessage" ) );

      messagePanel.add( addMessage );

      bottomPanel.add( messagePanel, "NEW_MESSAGE" );

      view.add( bottomPanel, BorderLayout.SOUTH );

      ((CardLayout) bottomPanel.getLayout()).show( bottomPanel, "INITIAL" );

      return view;
   }

   private JPanel initNew()
   {

      // Layout and form for the left panel
      JPanel newConversation = new JPanel( new BorderLayout() );

      FormLayout leftLayout = new FormLayout( "pref", "pref,pref,pref,pref:grow" );

      JPanel leftForm = new JPanel();
      leftForm.setFocusable( false );
      DefaultFormBuilder leftBuilder = new DefaultFormBuilder( leftLayout,
            leftForm );
      leftBuilder.setBorder( Borders.createEmptyBorder( Sizes.DLUY8,
            Sizes.DLUX8, Sizes.DLUY2, Sizes.DLUX4 ) );

      StateBinder conversationBinder = new StateBinder();
      conversationBinder.addConverter( new StateBinder.Converter()
      {
         public Object toComponent( Object value )
         {
            if (value instanceof ListItemValue)
            {
               return ((ListItemValue) value).description().get();
            } else
               return value;
         }

         public Object fromComponent( Object value )
         {
            return value;
         }
      } );
      conversationBinder.setResourceMap( context.getResourceMap( getClass() ) );
      ConversationDetailDTO template = conversationBinder
            .bindingTemplate( ConversationDetailDTO.class );

      BindingFormBuilder leftBindingBuilder = new BindingFormBuilder(
            leftBuilder, conversationBinder );
      JLabel participation = new JLabel();
      participation.setFont( participation.getFont().deriveFont(
            Font.BOLD ) );
      conversationBinder.bind( participation, template.participants() );

      // Title
      leftBuilder.addLabel( i18n.text( TaskResources.heading_label ) );
      leftBuilder.nextLine();
      leftBuilder.add( conversationBinder.bind( (JTextField) TEXTFIELD.newField(), template.description() ) );
      leftBuilder.nextLine();

      leftBuilder.addLabel( i18n.text( TaskResources.message_label ) );
      leftBuilder.nextLine();

      JScrollPane msgPane = (JScrollPane) TEXTAREA.newField();
      msgPane.setMinimumSize( new Dimension( 10, 50 ) );
      leftBuilder.add( conversationBinder.bind( (JScrollPane) TEXTAREA.newField(), template.messages() ));
      leftBuilder.nextLine();


      // Layout and form for the right panel
      FormLayout rightLayout = new FormLayout( "70dlu, 5dlu, 150:grow", "pref,pref" );

      JPanel rightForm = new JPanel( rightLayout );
      rightForm.setFocusable( false );
      DefaultFormBuilder rightBuilder = new DefaultFormBuilder( rightLayout,
            rightForm );
      rightBuilder.setBorder( Borders.createEmptyBorder( Sizes.DLUY8,
            Sizes.DLUX4, Sizes.DLUY2, Sizes.DLUX8 ) );

      // Select participants
      javax.swing.Action participantsAction = am.get( "participants" );
      JButton participantButton = new JButton( participantsAction );
      participantButton.registerKeyboardAction( participantsAction, (KeyStroke) participantsAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );
      NotificationGlassPane.registerButton( participantButton );
      participantButton.setHorizontalAlignment( SwingConstants.LEFT );
      rightBuilder.setExtent( 1, 1 );
      rightBuilder.add( participantButton );
      rightBuilder.nextLine();
      rightBuilder.setExtent(3,1);
      rightBuilder.add( participation );
      rightBuilder.nextLine();

      JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      bottomPanel.add( new JButton( am.get( "create" ) ) );

      newConversation.add( leftForm, BorderLayout.CENTER );
      newConversation.add( rightForm, BorderLayout.EAST );
      newConversation.add( bottomPanel, BorderLayout.SOUTH );

      return newConversation;
   }

   @Action
   public void showNewMessage()
   {
      ((CardLayout) bottomPanel.getLayout()).show( bottomPanel, "NEW_MESSAGE" );
   }

   @Action
   public void showParticipants()
   {
      participantView.setModel( model.getParticipantsModel() );
      ((CardLayout) bottomPanel.getLayout()).show( bottomPanel, "NEW_PARTICIPANT" );
   }

   @Action
   public void addMessage()
   {

   }

   @Action
   public void participants()
   {

   }

   @Action
   public void create()
   {

   }

   public void setModel( TaskConversationModel taskConversationDetailModel )
   {
      if (model != null)
         model.messages().removeListEventListener( this );

      model = taskConversationDetailModel;
      model.refresh();
      refresher.setRefreshable( model );

      if (model != null)
      {
         taskConversationDetailModel.messages().addListEventListener( this );

         model.messages().addListEventListener( this );
         listChanged( null );
      }

   }

   public void listChanged( ListEvent listEvent )
   {

      EventList<MessageDTO> list = model.messages();
      StringBuffer buf = new StringBuffer();

      buf.append( "<h2>" + model.getDescription() + "</h2>" );

      buf.append( "<table border='NONE' cellpadding='10'" );
      int size = list.size();
      for (int i = 0; i < size; i++)
      {
         MessageDTO messageDTO = list.get( i );

         buf.append( "<tr>" );
         buf.append( "<td width='150' align='left' valign='top'>" );
         buf.append( messageDTO.sender().get() );
         buf.append( "<br>" );
         buf.append( new SimpleDateFormat( i18n.text( WorkspaceResources.date_time_format ) ).format( messageDTO.createdOn().get() ) );
         buf.append( "</td><td width='" + getMessageTableLastColSize() + "' style='WORD-BREAK:BREAK-ALL'>" );
         buf.append( messageDTO.body().get() );
         buf.append( "<hr width='100%' style='border:1px solid #cccccc; padding-top: 15px;'>" );
         buf.append( "</td>" );
         buf.append( "</tr>" );


      }
      buf.append( "</table>" );
      messageArea.setText( buf.toString() );

   }

   private int getMessageTableLastColSize()
   {
      return (int) (messageArea.getVisibleRect().getWidth() < 600 ? 450 : (messageArea.getVisibleRect().getWidth() - 150));
   }

}
