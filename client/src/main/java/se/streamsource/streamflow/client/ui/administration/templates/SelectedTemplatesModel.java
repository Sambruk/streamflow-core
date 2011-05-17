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

package se.streamsource.streamflow.client.ui.administration.templates;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.surface.SelectedTemplatesDTO;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsModel;
import se.streamsource.streamflow.client.util.Refreshable;

import java.util.Observable;

public class SelectedTemplatesModel extends Observable implements Refreshable
{
   @Structure
   private Module module;

   private CommandQueryClient client;

   private SelectedTemplatesDTO DTO;


   public SelectedTemplatesModel( @Uses CommandQueryClient client )
   {
      this.client = client;
   }

   public void refresh()
   {

      SelectedTemplatesDTO updatedDTO = client.query( "selectedtemplates", SelectedTemplatesDTO.class );
      DTO = (SelectedTemplatesDTO) updatedDTO.buildWith().prototype();
      
      setChanged();
      notifyObservers();
   }

   public AttachmentsModel newAttachmentsModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(AttachmentsModel.class).use(client.getClient("../attachments/")).newInstance();
   }

   public EventList<LinkValue> getPossibleTemplates( String query)
   {
      try
      {
         BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

         ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
         builder.prototype().string().set( "pdf" );
         
         LinksValue listValue = client.query( query,
               builder.newInstance(), LinksValue.class );
         list.addAll( listValue.links().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh,
               e );
      }
   }

   public void setTemplate( LinkValue link )
   {
      client.postLink(link);
   }

   public void removeTemplate( String command )
   {
      ValueBuilder<EntityValue> builder = module.valueBuilderFactory().newValueBuilder( EntityValue.class );
      client.postCommand( command, builder.newInstance() );
   }

   public SelectedTemplatesDTO getSelectedTemplatesValue()
   {
      return DTO;
   }
}
