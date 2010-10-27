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

package se.streamsource.streamflow.client.ui.administration.forms;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.domain.form.FormValue;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;

import java.util.Observable;

import static se.streamsource.streamflow.infrastructure.event.source.helper.Events.*;

/**
 * JAVADOC
 */
public class FormModel
      extends Observable
      implements Refreshable, TransactionListener

{
   @Structure
   ObjectBuilderFactory obf;

   @Uses
   CommandQueryClient client;

   private FormValue formValue;

   public void refresh() throws OperationException
   {
      formValue = client.query( "index", FormValue.class );
      setChanged();
      notifyObservers( this );
   }

   public String getNote()
   {
      return formValue.note().get();
   }

   public FormValue getFormValue()
   {
      return formValue;
   }

   public void changeDescription( StringValue description ) throws ResourceException
   {
      client.putCommand( "changedescription", description );
   }

   public void changeNote( StringValue note ) throws ResourceException
   {
      client.putCommand( "changenote", note );
   }

   public void changeFormId( StringValue id )
   {
      client.putCommand( "changeformid", id );
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      // Refresh if either the owner of the list has changed, or if any of the entities in the list has changed
      if (matches( onEntities( client.getReference().getLastSegment() ), transactions ))
         refresh();
   }
}