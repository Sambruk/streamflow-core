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
package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import static javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.api.administration.form.ListBoxFieldValue;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.StreamflowButton;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import se.streamsource.streamflow.util.MultiFieldHelper;

public class ListBoxPanel
      extends AbstractFieldPanel
{
   private StreamflowButton left;
   private StreamflowButton right;
   private JList selectedElements;
   private JList possibleElements;

   public ListBoxPanel( @Uses FieldSubmissionDTO field, @Uses ListBoxFieldValue fieldValue, @Service ApplicationContext context )
   {
      super( field );

      FormLayout formLayout = new FormLayout(
            "80dlu, 40dlu, 80dlu",
            "20dlu, 20dlu, 20dlu, 20dlu" );

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, this );

      DefaultListModel listModel = new DefaultListModel();
      for (String element : fieldValue.values().get() )
      {
         listModel.addElement( element );
      }
      possibleElements = new JList( listModel );
      selectedElements = new JList( new DefaultListModel() );

      ApplicationActionMap map = context.getActionMap( this );
      right = new StreamflowButton( map.get("left") );
      left = new StreamflowButton( map.get("right") );

      possibleElements.setSelectionMode( MULTIPLE_INTERVAL_SELECTION );
      possibleElements.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( map.get("left") ) );
      selectedElements.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( map.get("right") ) );

      //possibleElements.setPreferredSize( new Dimension( 80, 200) );
      formBuilder.add( new JScrollPane( possibleElements ), "1,1,1,4" );
      formBuilder.add( right, "2,2");
      formBuilder.add( left, "2,3" );
      formBuilder.add( new JScrollPane( selectedElements ), "3,1,1,4");
   }

   @Action
   public void left()
   {
      DefaultListModel model = (DefaultListModel) selectedElements.getModel();
      DefaultListModel possibleModel = (DefaultListModel) possibleElements.getModel();
      Object[] selectedValues = possibleElements.getSelectedValues();

      for (Object value : selectedValues)
      {
         model.addElement( value );
         possibleModel.removeElement( value );
      }
      binding.updateProperty( getValue() );
   }

   @Action
   public void right()
   {
      DefaultListModel model = (DefaultListModel) selectedElements.getModel();
      DefaultListModel possibleModel = (DefaultListModel) possibleElements.getModel();

      Object[] objects = selectedElements.getSelectedValues();
      for (Object value : objects)
      {
         possibleModel.addElement( value );
         model.removeElement( value );
      }
      binding.updateProperty( getValue() );
   }

   @Override
   public String getValue()
   {
      StringBuilder sb = new StringBuilder();
      boolean first = true;

      ListModel model = selectedElements.getModel();
      for ( int i=0; i<model.getSize(); i++ )
      {
         if (!first) sb.append( ", " );
         String elm = (String) model.getElementAt( i );
         if ( elm.contains( "," ))
         {
            sb.append( "[" ).append( elm ).append( "]" );
         } else
         {
            sb.append( elm );
         }
         first = false;
      }
      return sb.toString();
   }

   @Override
   public void setValue( String newValue )
   {
      if ( newValue == null || newValue.equals( "" )) return;
      DefaultListModel model = (DefaultListModel) selectedElements.getModel();
      DefaultListModel possibleModel = (DefaultListModel) possibleElements.getModel();

      for ( String element : MultiFieldHelper.options( newValue ) )
      {
         model.addElement( element );
         possibleModel.removeElement( element );
      }
   }

   @Override
   public void setBinding( StateBinder.Binding binding )
   {
      this.binding = binding;
   }
}