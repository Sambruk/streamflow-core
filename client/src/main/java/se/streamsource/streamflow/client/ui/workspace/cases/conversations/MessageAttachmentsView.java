package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import org.jdesktop.application.ApplicationContext;
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
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 */
public class MessageAttachmentsView
   extends JPanel
   implements Refreshable
{
   @Structure
   Module module;

   private AttachmentsModel model;

   public MessageAttachmentsView( @Service ApplicationContext context, @Uses AttachmentsModel model )
   {
      this.model = model;
      new RefreshWhenShowing( this, this );
   }

   public void refresh()
   {
      this.removeAll();

      model.refresh();
      for( AttachmentDTO attachmentIn : model.getEventList() )
      {
         final AttachmentDTO attachment = attachmentIn;

         StreamflowButton attachmentButton = new StreamflowButton( attachment.text().get(), i18n.icon( Icons.attachments, 14 ) );
         attachmentButton.addActionListener( new ActionListener()
         {
            public void actionPerformed( ActionEvent e )
            {
               new OpenAttachmentTask( attachment.text().get(), attachment.href().get(), MessageAttachmentsView.this, model ).execute();
            }
         } );
         add( attachmentButton );
      }
   }
}
