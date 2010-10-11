/*
 * Copyright (c) 2010, Mads Enevoldsen. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.administration.casetypes.forms;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.form.FormElementItem;
import se.streamsource.streamflow.domain.form.CreateFieldDTO;
import se.streamsource.streamflow.domain.form.FieldTypes;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.PageListItemValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;

/**
 * JAVADOC
 */
public class FormElementsModel extends Observable
      implements Refreshable, TransactionListener
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   @Structure
   ObjectBuilderFactory obf;

   private EventList<FormElementItem> formElements = new BasicEventList<FormElementItem>();

   public void refresh()
   {
      Collection<FormElementItem> items = new ArrayList<FormElementItem>( );
      for (LinkValue linkValue : client.query( "formelements", LinksValue.class ).links().get())
      {
         FormElementItem formElement = new FormElementItem( linkValue.text().get(), linkValue.rel().get(), client.getClient( linkValue ));
         items.add(formElement);
      }
      EventListSynch.synchronize( items, formElements );
   }

   public EventList<FormElementItem> getFormElementsList()
   {
      return formElements;
   }

   public void addField( FormElementItem pageItem, String name, FieldTypes fieldType )
   {
      ValueBuilder<CreateFieldDTO> builder = vbf.newValueBuilder( CreateFieldDTO.class );
      builder.prototype().name().set( name );
      builder.prototype().fieldType().set( fieldType );

      try
      {
         pageItem.getClient().postCommand( "create", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.description_cannot_be_more_than_50, e );
      }
   }

   public void addPage( String pageName )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( pageName );

      try
      {
         client.postCommand( "create", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.description_cannot_be_more_than_50, e );
      }
   }

   public void removeFormElement( FormElementItem item )
   {
      item.getClient().delete();
   }

   public void move( FormElementItem item, String direction )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( direction );

      item.getClient().putCommand( "move",  builder.newInstance() );
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches(transactions, Events.onEntities( client.getReference().getLastSegment() )))
      {
         refresh();
      }
   }
}