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

package se.streamsource.streamflow.client.ui.workspace.search;

import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
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

import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.*;

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

   public HandleSearchesDialog( @Service ApplicationContext context, @Structure ValueBuilderFactory vbf, @Structure ObjectBuilderFactory obf, @Uses SavedSearchesModel model )
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
      searches.setModel( new EventListModel<LinkValue>( model.getList() ) );
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
      searchBinder = obf.newObject( StateBinder.class );
      searchBinder.setResourceMap( context.getResourceMap( getClass() ) );
      SearchValue template = searchBinder.bindingTemplate( SearchValue.class );

      builder.add( new JLabel( i18n.text( WorkspaceResources.name_label ) ) );
      builder.nextColumn( 1 );
      JTextField name;
      builder.add( searchBinder.bind( name = (JTextField) TEXTFIELD.newField(), template.name() ) );

      builder.nextLine();
      builder.add( new JLabel( i18n.text( WorkspaceResources.query_label ) ) );
      builder.nextColumn( 1 );
      JTextField query;
      builder.add( searchBinder.bind( query = (JTextField) TEXTFIELD.newField(), template.query() ) );

      right.add( form, BorderLayout.CENTER );

      add( right, BorderLayout.CENTER );

      searchBinder.updateWith( vbf.newValueBuilder( SearchValue.class ).prototype() );
      searchBinder.addObserver( this );
   }

   public void valueChanged( ListSelectionEvent e )
   {
      if (!e.getValueIsAdjusting())
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
   }

   @Action
   public Task remove()
   {
      if (!searches.isSelectionEmpty())
      {
         final LinkValue value = (LinkValue) searches.getSelectedValue();
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.remove( value );
            }
         };
      } else
         return null;
   }

   @Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }

   public void update( Observable o, final Object arg )
   {
      if (!searches.isSelectionEmpty())
      {
         final LinkValue linkValue = (LinkValue) searches.getSelectedValue();
         final Property property = (Property) arg;
         final String prop = (String) property.get();
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               if (property.qualifiedName().name().equals( "name" ))
               {
                  model.changeDescription( linkValue, prop );
               } else if (property.qualifiedName().name().equals( "query" ))
               {
                  model.changeQuery( linkValue, prop );
               }
            }
         }.execute();
      }
   }
}