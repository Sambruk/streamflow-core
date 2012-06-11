/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.administration.casesettings;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.FormValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.priority.PriorityValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.EventListSynch;

/**
 * Model behind PriorityOnCaseView
 */
public class PriorityOnCaseModel
   extends ResourceModel<FormValue>
{
   public void changeVisibility( Boolean visible )
   {
      Form form = new Form();
      form.set( "visible", visible.toString() );

      client.postLink(command("updatevisibility"), form);
   }

   public void changeMandate( Boolean mandatory )
   {
      Form form = new Form();
      form.set( "mandatory", mandatory.toString() );

      client.postLink(command("updatemandatory"), form);
   }

   public void priorityDefault( String id )
   {
      if( !id.equals( getIndex().form().get().get( "prioritydefault" ) ) )
      {
         Form form = new Form( );
         form.set( "id", "-1".equals( id ) ? "" : id );

         client.postLink( command("prioritydefault"), form );
      }
   }

   public EventComboBoxModel<PriorityValue> getCasePriorities()
   {
      try
      {
         BasicEventList<PriorityValue> list = new BasicEventList<PriorityValue>();

         LinksValue listValue = client.query( "priorities",
               LinksValue.class );
         EventListSynch.synchronize( listValue.links().get(), list );

         return new EventComboBoxModel<PriorityValue>( list );
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh,
               e );
      }
   }
}
