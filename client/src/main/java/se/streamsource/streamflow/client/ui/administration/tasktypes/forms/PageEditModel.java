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

package se.streamsource.streamflow.client.ui.administration.tasktypes.forms;

import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractListModel;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.entity.EntityReference;
import org.restlet.resource.ResourceException;

import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.domain.form.CreateFieldDTO;
import se.streamsource.streamflow.domain.form.FieldTypes;
import se.streamsource.streamflow.domain.form.PageDefinitionValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.PageListItemValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

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
      ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
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
      ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
      builder.prototype().string().set( direction );


      client.putCommand( "move", builder.newInstance() );
   }

   public void remove() throws ResourceException
   {
      client.deleteCommand();
   }
}