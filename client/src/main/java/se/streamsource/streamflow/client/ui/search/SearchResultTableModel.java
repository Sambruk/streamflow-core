/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.search;

import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;

import javax.swing.SwingUtilities;

/**
 * JAVADOC
 */
public class SearchResultTableModel
      extends TaskTableModel
{
   private String searchString;

   public void search( String text )
   {
      searchString = SearchTerms.translate( text );

      refresh();
   }

   @Override
   public void refresh()
   {
      try
      {
         ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
         builder.prototype().string().set( searchString );

         final TaskListDTO newRoot = client.query( "search", builder.newInstance(), TaskListDTO.class );
         boolean same = newRoot.equals( tasks );
         if (!same)
         {
            SwingUtilities.invokeLater( new Runnable()
            {
               public void run()
               {
                  EventListSynch.synchronize( newRoot.tasks().get(), eventList );
                  tasks = newRoot;
               }
            });
         }
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_perform_operation, e );
      }
   }
}