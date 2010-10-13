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
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.LinkValueListModel;
import se.streamsource.streamflow.domain.form.CreateFieldDTO;
import se.streamsource.streamflow.domain.form.FieldTypes;
import se.streamsource.streamflow.domain.form.FormSignatureValue;
import se.streamsource.streamflow.domain.form.RequiredSignatureValue;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;

/**
 * JAVADOC
 */
public class FormSignaturesModel
      extends LinkValueListModel
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   @Structure
   ObjectBuilderFactory obf;

   private EventList<LinkValue> formSignatures = new BasicEventList<LinkValue>();

   public void refresh()
   {
      EventListSynch.synchronize( client.query( "index", LinksValue.class ).links().get(), formSignatures );
   }

   public EventList<LinkValue> getFormSignatures()
   {
      return formSignatures;
   }

   public void create( String name )
   {
      ValueBuilder<RequiredSignatureValue> builder = vbf.newValueBuilder( RequiredSignatureValue.class );
      builder.prototype().name().set( name );

      try
      {
         client.postCommand( "create", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.description_cannot_be_more_than_50, e );
      }
   }

   public void removeFormSignature( LinkValue item )
   {
      client.getClient( item ).delete();
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches(transactions, Events.onEntities( client.getReference().getLastSegment() )))
      {
         refresh();
      }
   }
}