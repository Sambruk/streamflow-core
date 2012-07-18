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
package se.streamsource.streamflow.client.ui.workspace.cases.caselog;


import static se.streamsource.streamflow.client.util.i18n.ICON_16;
import static se.streamsource.streamflow.client.util.i18n.icon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryDTO;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.DateFormats;
import se.streamsource.streamflow.util.Strings;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class CaseLogListCellRenderer implements ListCellRenderer
{

   private JLabel conversationIcon;
   private JLabel myPagesPublishedIcon;
   private JLabel systemIcon;
   private JLabel customIcon;
   private JLabel contactIcon;
   private JLabel formIcon;
   private JLabel attachmentIcon;

   public CaseLogListCellRenderer()
   {
      myPagesPublishedIcon = new JLabel( icon( Icons.published, ICON_16 ) );
      myPagesPublishedIcon.setText( " " );
      
      systemIcon = new JLabel(icon( Icons.history, ICON_16));
      customIcon = new JLabel(icon( Icons.message_add, ICON_16));
      contactIcon = new JLabel(icon( Icons.projects, ICON_16));
      formIcon = new JLabel(icon( Icons.forms, ICON_16));
      conversationIcon = new JLabel(icon( Icons.conversations, ICON_16));
      attachmentIcon = new JLabel(icon( Icons.attachments, ICON_16));
   }
   
   public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
         boolean cellHasFocus)
   {
      CaseLogEntryDTO entry = (CaseLogEntryDTO) value;

      JPanel renderer = new JPanel( new BorderLayout() );
      FormLayout rowLayout = new FormLayout( "30dlu, pref:grow, pref", "pref, fill:pref:grow" );
      renderer.setLayout( rowLayout );
      DefaultFormBuilder formBuilder = new DefaultFormBuilder( rowLayout, renderer );
      renderer.setBorder( new EmptyBorder( 3, 3, 6, 3 ) );

      // Icons
      JPanel icons = new JPanel(new BorderLayout());
      icons.setOpaque( false );
      if (entry.myPagesVisibility().get()){
         icons.add( myPagesPublishedIcon, BorderLayout.WEST);
      }
      switch (entry.caseLogType().get())
      {
      case system:
         icons.add( systemIcon, BorderLayout.EAST );
         break;
      case custom:
         icons.add( customIcon, BorderLayout.EAST );
         break;
      case contact:
         icons.add( contactIcon, BorderLayout.EAST );
         break;
      case form:
         icons.add( formIcon, BorderLayout.EAST );
         break;
      case conversation:
         icons.add( conversationIcon, BorderLayout.EAST );
         break;
      case attachment:
         icons.add( attachmentIcon, BorderLayout.EAST );
         break;
      default:
         break;
      }
      
      formBuilder.add( icons, new CellConstraints( 1,1,1,2,CellConstraints.RIGHT, CellConstraints.TOP, new Insets( 0, 0, 0, 10 ) ) );
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
      String entryMessage = entry.message().get();
      if (!Strings.empty( entryMessage ))
      {
         entryMessage = String.format("<html><div WIDTH=%d>%s</div><html>", 500, entryMessage.trim().replace( "\n", "<br>" ) );
      }
      JLabel message = new JLabel( entryMessage );
      message.setForeground( Color.BLACK );
      formBuilder.add( message, new CellConstraints( 2, 2, 2, 1, CellConstraints.LEFT, CellConstraints.TOP ) );

      return renderer;
   }

}
