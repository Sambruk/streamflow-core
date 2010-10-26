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

package se.streamsource.streamflow.client.ui.administration.templates;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.organization.SelectedTemplateValue;

import java.util.Observable;

public class SelectedTemplateModel extends Observable implements Refreshable
{
   @Structure
   private ValueBuilderFactory vbf;

   private CommandQueryClient client;

   private SelectedTemplateValue value;


   public SelectedTemplateModel( @Uses CommandQueryClient client )
   {
      this.client = client;
   }

   public void refresh()
   {

      SelectedTemplateValue updatedValue = client.query( "selectedtemplate", SelectedTemplateValue.class );
      value = (SelectedTemplateValue) updatedValue.buildWith().prototype();
      
      setChanged();
      notifyObservers();
   }

   public EventList<LinkValue> getPossibleTemplates()
   {
      try
      {
         BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( "pdf" );
         
         LinksValue listValue = client.query( "possibletemplates",
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

   public void removeTemplate()
   {
      ValueBuilder<EntityValue> builder = vbf.newValueBuilder( EntityValue.class );
      client.postCommand( "settemplate", builder.newInstance() );
   }

   public SelectedTemplateValue getSelectedTemplateValue()
   {
      return value;
   }
}
