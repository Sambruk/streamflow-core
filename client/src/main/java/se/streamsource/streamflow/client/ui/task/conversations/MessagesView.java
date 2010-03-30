/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.task.conversations;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.conversation.MessageDTO;

import javax.swing.JTextPane;
import java.text.SimpleDateFormat;

public class MessagesView extends JTextPane
      implements ListEventListener
{

   private MessagesModel model;

   private String topic;
   private int lastSize = -1;

   public void setModel( String topic, MessagesModel messagesModel )
   {
      this.topic = topic;
      if (model != null)
         model.messages().removeListEventListener( this );

      model = messagesModel;
      lastSize = -1;
      model.refresh();

      if (model != null)
      {
         model.messages().addListEventListener( this );
         listChanged( null );
      }

   }

   public void listChanged( ListEvent listEvent )
   {
      EventList<MessageDTO> list = model.messages();

      if (list.size() > lastSize)
      {
         StringBuffer buf = new StringBuffer();

         buf.append( "<html><head></head><body>" );
         buf.append( "<strong>" + topic + "</strong>" );

         int size = list.size();
         if (size > 0)
         {
            buf.append( "<table border='NONE' cellpadding='10'>" );
            for (int i = 0; i < size; i++)
            {
               MessageDTO messageDTO = list.get( i );

               buf.append( "<tr>" );
               buf.append( "<td width='150' align='left' valign='top'>" );
               buf.append( "<p>" );
               buf.append( messageDTO.sender().get() );
               buf.append( "</p><p>" );
               buf.append( new SimpleDateFormat( i18n
                     .text( WorkspaceResources.date_time_format ) ).format( messageDTO
                     .createdOn().get() ) );
               buf.append( "</p></td><td width='" + getMessageTableLastColSize()
                     + "' style=''>" );
               buf.append( messageDTO.text().get() );
               buf
                     .append( "<hr width='100%' style='border:1px solid #cccccc; padding-top: 15px;'>" );
               buf.append( "</td>" );
               buf.append( "</tr>" );

            }
            buf.append( "</table>" );
         }
         buf.append( "</body></html>" );
         setText( buf.toString() );
         lastSize = list.size();
      }
   }

   private int getMessageTableLastColSize()
   {
      return (int) (getVisibleRect().getWidth() < 600 ? 450
            : (getVisibleRect().getWidth() - 150));
   }
}