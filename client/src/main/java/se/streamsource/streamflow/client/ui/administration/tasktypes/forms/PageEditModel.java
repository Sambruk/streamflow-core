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

package se.streamsource.streamflow.client.ui.administration.tasktypes.forms;

import java.util.logging.Logger;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.domain.form.PageDefinitionValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;

/**
 * JAVADOC
 */
public class PageEditModel
      implements Refreshable, EventListener, EventVisitor
{
   @Uses
   private CommandQueryClient client;

   @Structure
   private ValueBuilderFactory vbf;

   EventVisitorFilter eventFilter = new EventVisitorFilter( this, "changedDescription" );

   private PageDefinitionValue page;

   public void refresh()
   {
      try
      {
         page = (PageDefinitionValue) client.query( "page", PageDefinitionValue.class ).buildWith().prototype();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh_list_of_form_pages_and_fields, e );
      }
   }

   public void changeDesctiption( String pageName ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( pageName );

      client.putCommand( "changedescription", builder.newInstance() );
   }

   public PageDefinitionValue getPageDefinition()
   {
      return page;
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
   }

   public boolean visit( DomainEvent event )
   {
      if (event.entity().get().equals( page.page().get().identity() ))
      {
         Logger.getLogger( "adminitration" ).info( "Refresh page name" );
         refresh();
      }

      return false;
   }

   public void move( String direction ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( direction );


      client.putCommand( "move", builder.newInstance() );
   }

   public void remove() throws ResourceException
   {
      client.delete();
   }
}