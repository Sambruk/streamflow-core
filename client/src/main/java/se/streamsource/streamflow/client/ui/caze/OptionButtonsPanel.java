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
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptionButtonsPanel extends JPanel
{

   private ButtonGroup group;

   public OptionButtonsPanel( List<String> elements )
   {
      JPanel panel = new JPanel( new BorderLayout( ));
      FormLayout formLayout = new FormLayout( "200dlu", "" );
      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, panel );

      group = new ButtonGroup();
      for (String element : elements)
      {
         JRadioButton button = new JRadioButton( element );
         group.add( button );
         formBuilder.append( button );
         formBuilder.nextLine();
      }

      add( panel, BorderLayout.CENTER );
   }

   public void addActionPerformedListener( ActionListener listener )
   {

      Enumeration<AbstractButton> buttonEnumeration = group.getElements();
      while ( buttonEnumeration.hasMoreElements() )
      {
         buttonEnumeration.nextElement().addActionListener( listener );
      }
   }

   public void setSelected( String name )
   {
      Enumeration<AbstractButton> buttonEnumeration = group.getElements();
      while ( buttonEnumeration.hasMoreElements() )
      {
         AbstractButton button = buttonEnumeration.nextElement();
         if ( button.getText().equals( name ))
         {
            group.setSelected( button.getModel(), true );
         }
      }
   }
}