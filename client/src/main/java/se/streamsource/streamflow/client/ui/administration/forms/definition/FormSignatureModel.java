/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.administration.forms.definition;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.api.administration.form.RequiredSignatureValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import java.util.Observable;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.matches;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.onEntities;

/**
 * JAVADOC
 */
public class FormSignatureModel
      extends Observable
      implements Refreshable, TransactionListener

{
   @Uses
   CommandQueryClient client;

   private RequiredSignatureValue formSignature;

   public void refresh() throws OperationException
   {
      formSignature = client.query( "index", RequiredSignatureValue.class );
      setChanged();
      notifyObservers( this );
   }

   public RequiredSignatureValue getFormSignature()
   {
      return formSignature;
   }

   public void update( RequiredSignatureValue signature )
   {
      client.putCommand( "update", signature );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      // Refresh if the owner of the list has changed
      if (matches( onEntities( client.getReference().getParentRef().getParentRef().getLastSegment() ), transactions ))
         refresh();
   }
}