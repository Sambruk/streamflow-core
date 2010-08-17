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

package se.streamsource.streamflow.client.ui.caze;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.calendar.DateSelectionModel;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.ui.administration.form.SelectionModel;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import static javax.swing.ListSelectionModel.*;

public class ListBoxPanel extends JPanel
{
   private JButton left;
   private JButton right;
   private JList selectedElements;
   private ChangeListener listener;
   private JList possibleElements;
   private Boolean multiple;

   public ListBoxPanel( ApplicationContext context, List<String> elements, Boolean multiple )
   {
      super( new BorderLayout( ) );

      FormLayout formLayout = new FormLayout(
         "80dlu, 30dlu, 80dlu",
         "20dlu, 20dlu, 20dlu, 20dlu" );

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, this );

      this.multiple = multiple;
      DefaultListModel listModel = new DefaultListModel();
      for (String element : elements)
      {
         listModel.addElement( element );
      }
      possibleElements = new JList( listModel );
      selectedElements = new JList( new DefaultListModel() );

      ApplicationActionMap map = context.getActionMap( this );
      right = new JButton( map.get("left") );
      left = new JButton( map.get("right") );

      possibleElements.setSelectionMode( multiple ? MULTIPLE_INTERVAL_SELECTION : SINGLE_SELECTION );
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

      if ( !multiple )
      {
         if ( model.size() == 1 )
         {
            possibleModel.addElement( model.remove( 0 ) );
         }
         model.addElement( selectedValues[0] );
         possibleModel.removeElement( selectedValues[0] );
      } else 
      {
         for (Object value : selectedValues)
         {
            model.addElement( value );
            possibleModel.removeElement( value );
         }
      }
      listener.stateChanged( new ChangeEvent( this ) );
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
      listener.stateChanged( new ChangeEvent( this ) );
   }

   public void addChangeListener( ChangeListener listener )
   {
      this.listener = listener;
   }

   public String getSelected()
   {
      StringBuilder sb = new StringBuilder();
      boolean first = true;

      ListModel model = selectedElements.getModel();
      for ( int i=0; i<model.getSize(); i++ )
      {
         if (!first) sb.append( ", " );
         sb.append( model.getElementAt( i ) );
         first = false;
      }
      return sb.toString();
   }

   public void addItems( String items )
   {
      if ( items == null || items.equals( "" )) return;
      String[] elements = items.split( ", " );
      DefaultListModel model = (DefaultListModel) selectedElements.getModel();
      DefaultListModel possibleModel = (DefaultListModel) possibleElements.getModel();

      for ( String element : elements )
      {
         model.addElement( element );
         possibleModel.removeElement( element );
      }
   }
}