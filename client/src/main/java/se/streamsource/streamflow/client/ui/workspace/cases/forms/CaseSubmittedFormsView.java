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
package se.streamsource.streamflow.client.ui.workspace.cases.forms;

import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.util.Iterables;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormListDTO;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static se.streamsource.streamflow.client.util.i18n.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * JAVADOC
 */
public class CaseSubmittedFormsView
      extends JPanel
   implements TransactionListener, Refreshable
{
   private CaseSubmittedFormsModel model;
   private JTree submittedForms;
   private JScrollPane scroll;

   public CaseSubmittedFormsView( @Service ApplicationContext context, @Uses final CaseSubmittedFormsModel model)
   {
      super( new BorderLayout() );

      this.model = model;

      ActionMap am = context.getActionMap( this );
      setActionMap( am );
      setMinimumSize( new Dimension( 150, 0 ) );
      this.setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      scroll = new JScrollPane();
      submittedForms = new JTree(new DefaultMutableTreeNode( "Root" ));
      submittedForms.setRootVisible( false );
      submittedForms.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
      submittedForms.setShowsRootHandles( true );

      scroll.setViewportView( submittedForms );

      submittedForms.setCellRenderer( new DefaultTreeRenderer()
      {
         @Override
         public Component getTreeCellRendererComponent( JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus )
         {

            if (((DefaultMutableTreeNode) value).getUserObject() instanceof SubmittedFormListDTO )
            {
               final SubmittedFormListDTO listDTO = (SubmittedFormListDTO) ((DefaultMutableTreeNode) value).getUserObject();
               List<SubmittedFormListDTO> modelSubmittedForms = (ArrayList) Iterables.addAll(new ArrayList<SubmittedFormListDTO>(), model.getSubmittedForms());
               Collections.reverse( modelSubmittedForms );

               SubmittedFormListDTO lastSubmitted = Iterables.first(
                     Iterables.filter( new Specification<SubmittedFormListDTO>()
                     {

                        public boolean satisfiedBy( SubmittedFormListDTO item )
                        {
                           return listDTO.form().get().equals( item.form().get() );
                        }
                     }, modelSubmittedForms ) );

               if (listDTO == lastSubmitted)
               {
                  Component c = super.getTreeCellRendererComponent( tree, listDTO.form().get(), sel, expanded, leaf, row, hasFocus );

                  Map attributes = c.getFont().getAttributes();
                  if ( listDTO.unread().get() )
                  {
                     attributes.put( TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD );
                  }
                  c.setFont( new Font(attributes) );

                  return c;
               }

               String dateString = new SimpleDateFormat( text( WorkspaceResources.date_time_format ) ).format( listDTO.submissionDate().get() );
               return super.getTreeCellRendererComponent( tree, dateString, sel, expanded, leaf, row, hasFocus );
               
            } else
               return super.getTreeCellRendererComponent( tree, "DummyNode", sel, expanded, leaf, row, hasFocus );
         }
      } );

      
      JScrollPane submittedFormsScollPane = new JScrollPane();
      submittedFormsScollPane.setViewportView( submittedForms );

      add( submittedFormsScollPane, BorderLayout.CENTER );

      new RefreshWhenShowing(this, this);
   }

   public JTree getSubmittedFormsTree()
   {
      return submittedForms;
   }

   public CaseSubmittedFormsModel getModel()
   {
      return model;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (matches( withNames("submittedForm", "setUnread" ), transactions ))
      {
        TreePath path = submittedForms.getSelectionPath();

        refresh();
        if( path != null )
        {

           String form = ((SubmittedFormListDTO)((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject()).form().get();

           Enumeration<DefaultMutableTreeNode> e = ((DefaultMutableTreeNode)submittedForms.getModel().getRoot()).children();

           TreePath newPath = null;

           while (e.hasMoreElements()) {
              DefaultMutableTreeNode node = e.nextElement();
              if ( ((SubmittedFormListDTO)node.getUserObject()).form().get().equalsIgnoreCase(form)) {
                 newPath = new TreePath(node.getPath());
                 break;
              }
           }

           if( newPath != null )
           {
              submittedForms.setSelectionPath( newPath );
           }
        }
      }
   }

   public void refresh()
   {
      model.refresh();

      DefaultMutableTreeNode root = new DefaultMutableTreeNode( "Root" , true );

      Set<String> formNames = new HashSet<String>();
      for( SubmittedFormListDTO submittedForm : model.getSubmittedForms() )
      {
         formNames.add( submittedForm.form().get() );
      }

      final List<SubmittedFormListDTO> modelSubmittedForms = (ArrayList) Iterables.addAll( new ArrayList<SubmittedFormListDTO>(), model.getSubmittedForms() );
      Collections.reverse( modelSubmittedForms );

      final Iterator<String> nameIter = formNames.iterator();
      while( nameIter.hasNext() )
      {
         final String name = nameIter.next();
         ArrayList<SubmittedFormListDTO> filtered =
               (ArrayList<SubmittedFormListDTO>) Iterables.addAll( new ArrayList<SubmittedFormListDTO>(),
                     Iterables.filter( new Specification<SubmittedFormListDTO>()
         {

            public boolean satisfiedBy( SubmittedFormListDTO item )
            {
               return name.equals( item.form().get() );
            }
         }, modelSubmittedForms ) );

         DefaultMutableTreeNode firstLevel = null;
         boolean first = true;
         for( SubmittedFormListDTO form : filtered)
         {

            if( first )
            {
               firstLevel = new DefaultMutableTreeNode( form, true );
               root.add( firstLevel );
               first = false;
            } else
            {
               DefaultMutableTreeNode secondLevel = new DefaultMutableTreeNode( form );
               firstLevel.add( secondLevel );
            }
         }
      }

      submittedForms.setModel( new DefaultTreeModel( root ) );
   }
}