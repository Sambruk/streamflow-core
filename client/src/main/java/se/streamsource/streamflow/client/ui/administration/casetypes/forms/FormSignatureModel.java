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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.domain.form.FormValue;
import se.streamsource.streamflow.domain.form.RequiredSignatureValue;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;

import java.util.Observable;

import static se.streamsource.streamflow.infrastructure.event.source.helper.Events.*;

/**
 * JAVADOC
 */
public class FormSignatureModel
      extends Observable
      implements Refreshable, TransactionListener

{
   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   @Uses
   CommandQueryClient client;

   private RequiredSignatureValue formSignature;

   public void refresh() throws OperationException
   {
      formSignature = client.query( "index", RequiredSignatureValue.class );
      setChanged();
      notifyObservers( this );
   }

   public String getName()
   {
      return formSignature.name().get();
   }

   public RequiredSignatureValue getFormSignature()
   {
      return formSignature;
   }

   public void changeDescription( String description ) throws ResourceException
   {
      ValueBuilder<RequiredSignatureValue> builder = vbf.newValueBuilder( RequiredSignatureValue.class ).withPrototype( formSignature );
      builder.prototype().description().set( description );

      client.putCommand( "update", builder.newInstance() );

   }

   public void changeName( String name ) throws ResourceException
   {
      ValueBuilder<RequiredSignatureValue> builder = vbf.newValueBuilder( RequiredSignatureValue.class ).withPrototype( formSignature );
      builder.prototype().name().set( name );

      client.putCommand( "update", builder.newInstance() );
   }


   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      // Refresh if either the owner of the list has changed, or if any of the entities in the list has changed
      if (matches( transactions, onEntities( client.getReference().getLastSegment() )))
         refresh();
   }
}