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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.ValueBinder;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 *
 */
public class MessageDraftView
   extends JPanel
   implements Refreshable
{
   MessageDraftModel model;

   private JTextPane newMessage;

   ValueBinder valueBinder;

   MessageDraftAttachmentsView attachmentsView;

   public MessageDraftView(@Service final ApplicationContext context,
                           @Structure Module module,
                           @Uses MessageDraftModel model)
   {
      this.model = model;
      setActionMap( context.getActionMap(this) );
      setLayout( new BorderLayout(  ) );
      setPreferredSize( new Dimension( 100, 250 ) );

      // NEWMESSAGE
      JScrollPane messageScroll = new JScrollPane();

      newMessage = new JTextPane();
      newMessage.setContentType("text/plain");
      newMessage.setEditable(true);
      messageScroll.getViewport().add(newMessage);

      valueBinder = module.objectBuilderFactory().newObject( ValueBinder.class );
      valueBinder.bind( "string", newMessage );

      FormLayout formLayout = new FormLayout(  "fill:pref:grow, 4dlu, min", "pref" );
      PanelBuilder panelBuilder = new PanelBuilder( formLayout );

      attachmentsView = module.objectBuilderFactory()
            .newObjectBuilder( MessageDraftAttachmentsView.class )
            .use( model.newMessageDraftAttachmentsModel() ).newInstance();

      panelBuilder.add( attachmentsView );
      panelBuilder.nextColumn(2);

      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      // fetch the send action as proxy action from a parent container
      javax.swing.Action createMessageAction = context.getActionMap().get("createMessage");
      StreamflowButton createMessage = new StreamflowButton(createMessageAction);
      createMessage.registerKeyboardAction( createMessageAction,
            (KeyStroke) createMessageAction.getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      javax.swing.Action cancelAction = context.getActionMap().get("cancelNewMessage");
      StreamflowButton cancel = new StreamflowButton(cancelAction);

      buttonPanel.add( createMessage );
      buttonPanel.add( cancel );

      panelBuilder.add( buttonPanel );

      add( messageScroll, BorderLayout.CENTER );
      add( panelBuilder.getPanel(), BorderLayout.SOUTH );

      new ActionBinder( getActionMap() ).bind( "changeMessage", newMessage );

      new RefreshWhenShowing( this, this );
   }

   public void refresh()
   {
      model.refresh();
      valueBinder.update( model.getDraftMessage() );
      newMessage.requestFocusInWindow();
   }

   @Action
   public Task changeMessage()
   {
      final String message = newMessage.getText();
      return new CommandTask()
      {
         @Override
         public void command() throws Exception
         {
            model.changeMessage( message );
         }
      };
   }

}
