/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.api.workspace.cases.attachment.AttachmentDTO;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsModel;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.OpenAttachmentTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.WrapLayout;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 *
 */
public class MessageDraftAttachmentsView
   extends JPanel
   implements Refreshable, TransactionListener
{
   @Structure
   Module module;

   @Service
   DialogService dialogs;

   AttachmentsModel model;

   JPanel attachmentsPanel;

   public MessageDraftAttachmentsView( @Service ApplicationContext context, @Uses AttachmentsModel model )
   {
      this.model = model;
      ActionMap am = context.getActionMap( this );

      setLayout( new FlowLayout( FlowLayout.LEFT ) );

      StreamflowButton addButton = new StreamflowButton(am.get("add"));
      add( addButton );

      attachmentsPanel = new JPanel(new WrapLayout( FlowLayout.LEADING ));
      //attachmentsPanel.setBorder( BorderFactory.createLineBorder( Color.BLUE ) );

      JScrollPane scroll = new JScrollPane(  );
      scroll.setBorder( BorderFactory.createEmptyBorder() );
      scroll.setPreferredSize( new Dimension( 800, 40 ) );
      scroll.setViewportView( attachmentsPanel );

      add( scroll );

      new RefreshWhenShowing( this, this );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if( matches(onEntityTypes( "se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity" ), transactions ))
      {
         // on usecase delete no update necessary
         if( matches( withUsecases( "delete" ), transactions ))
         {
            if( matches(  withNames( "removedAttachment" ), transactions ))
               refresh();
            else
               return;
         }

         else if ( matches( withNames( "addedAttachment" ), transactions ))
            refresh();
      }
   }

   public void refresh()
   {
      attachmentsPanel.removeAll();

      model.refresh();
      for( AttachmentDTO attachmentIn : model.getEventList() )
      {
         final AttachmentDTO attachment = attachmentIn;

         JPanel attachmentPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
         StreamflowButton openButton = new StreamflowButton( attachment.text().get(), i18n.icon( Icons.attachments, 14 ) );
         openButton.setBorder( BorderFactory.createEmptyBorder() );

         openButton.addActionListener( new ActionListener()
         {
            public void actionPerformed( ActionEvent e )
            {
               new OpenAttachmentTask( attachment.text().get(), attachment.href().get(), MessageDraftAttachmentsView.this, model, dialogs ).execute();
            }
         } );
         attachmentPanel.add( openButton );

         StreamflowButton removeButton = new StreamflowButton( i18n.icon( Icons.drop, 14 ) );
         removeButton.setBorder( BorderFactory.createEmptyBorder() );
         removeButton.addActionListener( new ActionListener()
         {
            public void actionPerformed( ActionEvent e )
            {
               ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
               dialog.setRemovalMessage(i18n.text( WorkspaceResources.attachment ) );
               dialogs.showOkCancelHelpDialog(MessageDraftAttachmentsView.this, dialog, i18n.text( StreamflowResources.confirmation));

               if (dialog.isConfirmed())
               {
                  new CommandTask()
                  {
                     @Override
                     public void command()
                           throws Exception
                     {
                        try
                        {
                           model.removeAttachment( attachment );
                        } catch (Throwable e)
                        {
                           e.printStackTrace();
                        }
                     }
                  }.execute();
               }
            }
         } );

         attachmentPanel.add( removeButton );
         //attachmentPanel.add( new Label( " " ) );

         //attachmentPanel.setBorder( BorderFactory.createLineBorder( Color.BLUE ) );
         attachmentsPanel.add( attachmentPanel );
      }

      SwingUtilities.invokeLater( new Runnable()
      {
         public void run()
         {
            attachmentsPanel.revalidate();
         }
      } );

   }

   @Action(block = Task.BlockingScope.APPLICATION)
   public Task add() throws IOException
   {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setMultiSelectionEnabled(true);

      if (fileChooser.showDialog(this, i18n.text(WorkspaceResources.create_attachment)) == JFileChooser.APPROVE_OPTION)
      {
         final File[] selectedFiles = fileChooser.getSelectedFiles();

         return new AddAttachmentTask(selectedFiles);
      } else
         return null;
   }

   private class AddAttachmentTask
         extends CommandTask
   {
      File[] selectedFiles;

      private AddAttachmentTask(File[] selectedFiles)
      {
         this.selectedFiles = selectedFiles;
      }

      @Override
      public void command()
            throws Exception
      {
         setMessage(getResourceMap().getString("description"));

         for (File file : selectedFiles)
         {
            FileInputStream fin = new FileInputStream(file);
            model.createAttachment(file, fin);
         }
      }
   }

}
