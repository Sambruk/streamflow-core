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
package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXLabel;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageType;
import se.streamsource.streamflow.client.ui.DateFormats;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.ValueBinder;
import se.streamsource.streamflow.client.util.WrapLayout;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Locale;

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import static se.streamsource.streamflow.client.util.i18n.*;

/**
 *
 */
public class MessageView extends JPanel
   implements Refreshable, TransactionListener
{

   @Structure
   Module module;

   private JTextPane showMessage;
   private JXLabel authorLabelValue;
   private JXLabel createdOnLabelValue;
   private MessageAttachmentsView attachmentsView;

   private ValueBinder valueBinder;
   private MessageModel model;

   //private StyledDocument doc;

   public MessageView( @Service final ApplicationContext context,
                            @Structure Module module,
                            @Uses MessageModel model)
   {
      super( new BorderLayout() );
      this.model = model;
      setActionMap( context.getActionMap( this ) );

      setPreferredSize( new Dimension( 100, 250 ) );
      setBorder( BorderFactory.createEmptyBorder( 5, 0, 0, 0 ) );
      JScrollPane messageShowScroll = new JScrollPane();

      valueBinder = module.objectBuilderFactory().newObject( ValueBinder.class );

      JPanel messageDetailButtonPanel = new JPanel(new BorderLayout());
      messageDetailButtonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,3,0));
      javax.swing.Action closeAction = context.getActionMap().get("closeMessageDetails");

      StreamflowButton closeButton = new StreamflowButton(closeAction);
      JPanel closeButtonPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
      closeButtonPanel.setBorder( BorderFactory.createEmptyBorder( 7, 0, 0, 0 ) );
      closeButtonPanel.add( closeButton );

      FormLayout detailLabelLayout = new FormLayout("35dlu, 2dlu, pref:grow", "pref, pref");
      JPanel messageDetailsLabelPanel = new JPanel();
      DefaultFormBuilder formBuilder = new DefaultFormBuilder(detailLabelLayout, messageDetailsLabelPanel);

      JXLabel authorLabel = new JXLabel(text(sender_column_header));
      JXLabel createdOnLabel = new JXLabel(text(created_column_header));
      authorLabel.setForeground( Color.GRAY);
      createdOnLabel.setForeground(Color.GRAY);

      attachmentsView = module.objectBuilderFactory()
            .newObjectBuilder( MessageAttachmentsView.class )
            .use( model.newMessageAttachmentsModel() )
            .newInstance();
      attachmentsView.setLayout( new WrapLayout( FlowLayout.LEFT ) );

      authorLabelValue = new JXLabel();
      createdOnLabelValue = new JXLabel();

      formBuilder.setExtent(1, 1);
      formBuilder.add( authorLabel );
      formBuilder.nextColumn( 2 );
      formBuilder.add( valueBinder.bind( "sender", authorLabelValue ) );
      formBuilder.nextLine();
      formBuilder.add(createdOnLabel);
      formBuilder.nextColumn(2);
      formBuilder.add( createdOnLabelValue );

      messageDetailButtonPanel.add(closeButtonPanel, BorderLayout.EAST);
      JScrollPane pane = new JScrollPane( attachmentsView );
      pane.setPreferredSize( new Dimension( 500, 40 ) );
      pane.setBorder( BorderFactory.createEmptyBorder() );
      messageDetailButtonPanel.add( pane, BorderLayout.CENTER );
      messageDetailButtonPanel.add(messageDetailsLabelPanel, BorderLayout.WEST);

      showMessage = new JTextPane();
      valueBinder.bind( "text", showMessage );
      showMessage.setEditable(false);
      messageShowScroll.getViewport().add(showMessage);

      add( messageShowScroll, BorderLayout.CENTER );
      add( messageDetailButtonPanel, BorderLayout.NORTH );

      new RefreshWhenShowing( this, this );
      }

   public void refresh()
   {
      model.refresh();
      if(MessageType.HTML.equals( model.getMessageDTO().messageType().get() ) )
      {
         showMessage.setContentType( "text/html" );
         showMessage.getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);
      } else
      {
         showMessage.setContentType( "text/plain" );
      }
      valueBinder.update( model.getMessageDTO() );

      createdOnLabelValue.setText( DateFormats.getFullDateTimeValue(
            model.getMessageDTO().createdOn().get(), Locale.getDefault() ) );

   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if (Events.matches( Events.withNames( "createdMessage" ), transactions ))
      {
         model.refresh();
      }
   }

   public void read()
   {
      new CommandTask(){

         @Override
         protected void command() throws Exception
         {
            model.read();
         }
      }.execute();
   }
}