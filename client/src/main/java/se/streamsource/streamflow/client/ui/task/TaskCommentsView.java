/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.comment.CommentDTO;

import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.BorderLayout;
import java.io.IOException;

/**
 * JAVADOC
 */
public class TaskCommentsView
      extends JPanel
      implements ListDataListener
{
   @Service
   StreamFlowApplication app;

   @Service
   DialogService dialogs;

   @Uses
   ObjectBuilder<AddCommentDialog> addCommentDialogs;

   private TaskCommentsModel model;
   public JPanel comments;
   public RefreshWhenVisible refresher;

   public TaskCommentsView( @Service ApplicationContext context )
   {
      super( new BorderLayout() );
      ActionMap am = context.getActionMap( this );
      JButton addComments = new JButton( am.get( "addTaskComment" ) );

      comments = new JPanel();
      comments.setLayout( new BoxLayout( comments, BoxLayout.Y_AXIS ) );

      add( addComments, BorderLayout.NORTH );
      add( new JScrollPane( comments ), BorderLayout.CENTER );

      refresher = new RefreshWhenVisible( this );
      addAncestorListener( refresher );
   }

   @Action
   public void addTaskComment() throws ResourceException, IOException
   {
      AddCommentDialog dialog = addCommentDialogs.use( EntityReference.parseEntityReference( app.getSelectedUser() ) ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog );

      if (dialog.command() != null)
      {
         model.addComment( dialog.command() );
         model.refresh();
      }
   }

   public void setModel( TaskCommentsModel taskCommentsModel )
   {
      if (model != null)
         model.removeListDataListener( this );

      model = taskCommentsModel;

      if (model != null)
      {
         model.addListDataListener( this );

         contentsChanged( null );
      }

      refresher.setRefreshable( model );
   }

   public void intervalAdded( ListDataEvent e )
   {
      contentsChanged( e );
   }

   public void intervalRemoved( ListDataEvent e )
   {
      contentsChanged( e );
   }

   public void contentsChanged( ListDataEvent e )
   {
      comments.removeAll();
      int size = model.getSize();
      for (int i = 0; i < size; i++)
      {
         CommentDTO commentDTO = (CommentDTO) model.getElementAt( i );
         String text = commentDTO.text().get().replace( "\n", "<br/>" );
         JLabel comment = new JLabel( "<html><b>" + commentDTO.commenter().get() + ", " + commentDTO.creationDate().get() + "</b>" + (commentDTO.isPublic().get() ? " (" + i18n.text( WorkspaceResources.public_comment ) + ")" : "") + "<p>" + text + "</p></html>" );
         comments.add( comment );
      }
      TaskCommentsView.this.validate();
   }
}