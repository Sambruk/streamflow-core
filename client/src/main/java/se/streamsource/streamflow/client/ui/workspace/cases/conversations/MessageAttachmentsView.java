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
package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.api.workspace.cases.attachment.AttachmentDTO;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsModel;
import se.streamsource.streamflow.client.util.OpenAttachmentTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MessageAttachmentsView
   extends JPanel
   implements Refreshable
{
   @Structure
   Module module;

   @Service
   DialogService dialogs;

   Map<StreamflowButton,AttachmentDTO> openFileMap = new HashMap<StreamflowButton,AttachmentDTO>(  );

   private AttachmentsModel model;

   ActionMap am;

   public MessageAttachmentsView( @Service ApplicationContext context, @Uses AttachmentsModel model )
   {
      this.model = model;
      am = context.getActionMap( this );
      new RefreshWhenShowing( this, this );
   }

   public void refresh()
   {
      this.removeAll();
      openFileMap.clear();

      model.refresh();
      for( AttachmentDTO attachmentIn : model.getEventList() )
      {
         final AttachmentDTO attachment = attachmentIn;

         StreamflowButton attachmentButton = new StreamflowButton( attachment.text().get(), i18n.icon( Icons.attachments, 14 ) );
         attachmentButton.setBorder( BorderFactory.createEmptyBorder() );

         openFileMap.put( attachmentButton, attachment );

         attachmentButton.addActionListener( am.get( "open" ));
         add( attachmentButton );
      }
   }

   @Action(block = Task.BlockingScope.APPLICATION)
   public Task open( ActionEvent e ) throws IOException
   {
      AttachmentDTO attachment = openFileMap.get( e.getSource() );
      return new OpenAttachmentTask( attachment.text().get(), attachment.href().get(), MessageAttachmentsView.this, model, dialogs );
   }
}
