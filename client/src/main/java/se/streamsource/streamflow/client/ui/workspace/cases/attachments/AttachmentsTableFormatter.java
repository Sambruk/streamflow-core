/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.client.ui.workspace.cases.attachments;

import ca.odell.glazedlists.gui.AdvancedTableFormat;
import se.streamsource.streamflow.api.workspace.cases.attachment.AttachmentDTO;
import se.streamsource.streamflow.client.Icons;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * JAVADOC
 */
public class AttachmentsTableFormatter
      implements AdvancedTableFormat<AttachmentDTO>
{
   protected String[] columnNames = new String[]{
         "",
         text( attachment_name ),
         text( attachment_size ),
         text( created_column_header )
         };
   protected Class[] columnClasses;

   public AttachmentsTableFormatter()
   {
      columnClasses = new Class[] {
            Icons.class,
            String.class,
            String.class,
            Date.class
            };
   }

   public int getColumnCount()
   {
      return columnNames.length;
   }

   public String getColumnName( int i )
   {
      return columnNames[i];
   }

   public Class getColumnClass( int i )
   {
      return columnClasses[i];
   }

   public Comparator getColumnComparator( int i )
   {
      return null;
   }

   public Object getColumnValue( AttachmentDTO attachmentDTO, int i )
   {
      switch (i)
      {
         case 0:
            String rel = attachmentDTO.rel().get();
            if( "attachment".equals( rel ) )
               return Icons.attachments;
            else if( "conversation".equals( rel ) )
               return Icons.conversations;
            else if( "submittedform".equals( rel ) )
               return Icons.formSubmitted;
            break;

         case 1:
         {
            return attachmentDTO.text().get();
         }

         case 2:
            try
            {
               long size = attachmentDTO.size().get();

               if (size > 1000*1024)
               {
                  return size/(1000*1024)+" MB";
               } else if (size > 1024)
               {
                  return size/1024+" KB";
               } else
                  return size+"";
            } catch (Exception e)
            {
               return "";
            }

         case 3:
            return attachmentDTO.modificationDate().get() == null ? "" : DateFormat.getDateInstance( DateFormat.MEDIUM ).format( attachmentDTO.modificationDate().get());
      }

      return null;
   }
}