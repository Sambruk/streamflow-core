/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.client.ui.workspace;

import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.resource.user.profile.SearchValue;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;

/**
 * JAVADOC
 */
public class HandleSearchesDialog
      extends JPanel
      implements ListSelectionListener, Observer
{
   @Structure
   ValueBuilderFactory vbf;

   @Service
   DialogService dialogs;

   private SavedSearchesModel model;

   private StateBinder searchBinder;

   private JList searches;
   private JButton remove;
   private JTextField name;
   private JTextField query;

   private RefreshWhenVisible refresher;

   public HandleSearchesDialog( @Service ApplicationContext context, @Structure ValueBuilderFactory vbf, @Uses SavedSearchesModel model )
   {
      super( new BorderLayout() );
      setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
      ActionMap am;
      setActionMap( am = context.getActionMap( this ) );

      this.model = model;
      this.vbf = vbf;

      JPanel left = new JPanel( new BorderLayout() );
      left.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );

      searches = new JList();
      searches.setCellRenderer( new LinkListCellRenderer() );
      searches.addListSelectionListener( this );
      searches.setModel( new EventListModel<LinkValue>( model.getEventList() ) );
      JScrollPane scroll = new JScrollPane( searches );

      left.add( scroll, BorderLayout.CENTER );

      JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );

      remove = new JButton( am.get( "remove" ) );
      remove.setEnabled( false );
      buttonPanel.add( remove );

      left.add( buttonPanel, BorderLayout.SOUTH );

      add( left, BorderLayout.WEST );
      FormLayout layout = new FormLayout( "40dlu, 120dlu:grow", "pref, pref" );

      JPanel right = new JPanel( new BorderLayout() );
      right.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
      JPanel form = new JPanel( layout );
      form.setFocusable( false );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout,
            form );
      searchBinder = new StateBinder();
      searchBinder.setResourceMap( context.getResourceMap( getClass() ) );
      SearchValue template = searchBinder.bindingTemplate( SearchValue.class );

      builder.add( new JLabel( i18n.text( WorkspaceResources.name_label ) ) );
      builder.nextColumn( 1 );
      builder.add( searchBinder.bind( name = (JTextField) TEXTFIELD.newField(), template.name() ) );

      builder.nextLine();
      builder.add( new JLabel( i18n.text( WorkspaceResources.query_label ) ) );
      builder.nextColumn( 1 );
      builder.add( searchBinder.bind( query = (JTextField) TEXTFIELD.newField(), template.query() ) );

      right.add( form, BorderLayout.CENTER );

      add( right, BorderLayout.CENTER );

      searchBinder.updateWith( vbf.newValueBuilder( SearchValue.class ).prototype() );
      searchBinder.addObserver( this );
   }

   public void valueChanged( ListSelectionEvent e )
   {
      if (!searches.isSelectionEmpty())
      {
         TitledLinkValue search = (TitledLinkValue) searches.getSelectedValue();
         ValueBuilder<SearchValue> builder = vbf.newValueBuilder( SearchValue.class );
         builder.prototype().name().set( search.text().get() );
         builder.prototype().query().set( search.title().get() );

         searchBinder.updateWith( builder.prototype() );

         remove.setEnabled( true );
      } else
      {
         remove.setEnabled( false );
      }
   }

   @Action
   public void remove()
   {
      if (!searches.isSelectionEmpty())
      {
         model.remove( (LinkValue) searches.getSelectedValue() );
      }
   }

   @Action
   public void execute()
   {
      WindowUtils.findWindow( this ).dispose();
   }

   public void update( Observable o, Object arg )
   {
      if (!searches.isSelectionEmpty())
      {
         Property property = (Property) arg;
         if (property.qualifiedName().name().equals( "name" ))
         {
            try
            {
               model.changeDescription( (LinkValue) searches.getSelectedValue(), (String) property.get() );
            } catch (ResourceException e)
            {
               throw new OperationException( WorkspaceResources.could_not_change_description, e );
            }
         } else if (property.qualifiedName().name().equals( "query" ))
         {
            try
            {
               model.changeQuery( (LinkValue) searches.getSelectedValue(), (String) property.get() );
            } catch (ResourceException e)
            {
               throw new OperationException( WorkspaceResources.could_not_change_query, e );
            }
         }
      }
   }
}