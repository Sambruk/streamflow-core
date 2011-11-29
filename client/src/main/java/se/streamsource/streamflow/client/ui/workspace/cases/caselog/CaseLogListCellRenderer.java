/**
 *
 * Copyright 2009-2011 Streamsource AB
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
package se.streamsource.streamflow.client.ui.workspace.cases.caselog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import se.streamsource.streamflow.api.workspace.cases.general.CaseLogEntryDTO;
import se.streamsource.streamflow.client.ui.DateFormats;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class CaseLogListCellRenderer implements ListCellRenderer
{

   public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
         boolean cellHasFocus)
   {
      if (list instanceof JXList)
      {
         ((JXList) list).addHighlighter( HighlighterFactory.createAlternateStriping() );
      }

      CaseLogEntryDTO entry = (CaseLogEntryDTO) value;

      JPanel renderer = new JPanel( new BorderLayout() );
      FormLayout rowLayout = new FormLayout( "30dlu, pref:grow, pref", "pref, pref:grow" );
      renderer.setLayout( rowLayout );
      DefaultFormBuilder formBuilder = new DefaultFormBuilder( rowLayout, renderer );
      renderer.setBorder( new EmptyBorder( 3, 3, 6, 3 ) );

      // User
      JLabel user = new JLabel( entry.creator().get() );
      user.setForeground( Color.GRAY );
      formBuilder.add( user, new CellConstraints( 2, 1, 1, 1, CellConstraints.LEFT, CellConstraints.TOP ) );

      // Date
      JLabel date = new JLabel( DateFormats.getProgressiveDateTimeValue( entry.creationDate().get(),
            Locale.getDefault() ) );
      date.setForeground( Color.GRAY );
      formBuilder.add( date, new CellConstraints( 3, 1, 1, 1, CellConstraints.RIGHT, CellConstraints.TOP ) );

      // Message
      JLabel message = new JLabel( entry.message().get() );
      message.setForeground( Color.BLACK );
      formBuilder.add( message, new CellConstraints( 2, 2, 2, 1, CellConstraints.LEFT, CellConstraints.TOP ) );

      // Participants
      // JLabel participants = new
      // JLabel(String.valueOf(conversations.participants().get()), i18n.icon(
      // Icons.participants, 16), JLabel.LEADING);
      // headerBuilder.add(participants);
      // headerBuilder.nextColumn(2);

      renderer.setBackground( Color.WHITE );

      return renderer;
   }

}
