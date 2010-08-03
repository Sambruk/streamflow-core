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

package se.streamsource.streamflow.client.ui.menu;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Uniform;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.AccountVisitor;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemComparator;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.Observable;
import java.util.Observer;

/**
 * JAVADOC
 */
public class AccountsModel
{
   @Structure
   ValueBuilderFactory vbf;

   @Structure
   ObjectBuilderFactory obf;

   @Service
   IndividualRepository repository;

   @Structure
   UnitOfWorkFactory uowf;

   @Service
   Uniform client;

   TransactionList<ListItemValue> accounts = new TransactionList<ListItemValue>( new SortedList<ListItemValue>( new BasicEventList<ListItemValue>(), new ListItemComparator() ) );

   WeakModelMap<String, AccountModel> models = new WeakModelMap<String, AccountModel>()
   {
      protected AccountModel newModel( String key )
      {
         UnitOfWork uow = uowf.newUnitOfWork();
         Account acc = uow.get( Account.class, key );
         uow.discard();
         AccountModel accountModel = obf.newObjectBuilder( AccountModel.class ).use( acc ).newInstance();
         accountModel.addObserver( new Observer()
         {
            public void update( Observable o, Object arg )
            {
               refresh();
            }
         } );
         return accountModel;
      }
   };

   public void init( @Service IndividualRepository repository )
   {
      refresh();
   }

   public EventList<ListItemValue> getAccounts()
   {
      return accounts;
   }

   public AccountModel accountModel( int index )
   {
      String id = accounts.get( index ).entity().get().identity();
      return models.get( id );
   }

   public void newAccount( AccountSettingsValue accountSettingsValue ) throws UnitOfWorkCompletionException, ResourceException
   {
      UnitOfWork uow = uowf.newUnitOfWork();

      repository.individual().newAccount( accountSettingsValue );

      uow.complete();

      refresh();
   }

   public void removeAccount( int index ) throws UnitOfWorkCompletionException
   {
      accountModel( index ).remove();
      accounts.remove( index );
   }

   public void notifyEvent( DomainEvent event )
   {
      for (AccountModel model : models)
      {
         model.notifyEvent( event );
      }
   }

   private void refresh()
   {
      UnitOfWork uow = uowf.newUnitOfWork();
      final ValueBuilder<ListItemValue> itemBuilder = vbf.newValueBuilder( ListItemValue.class );
      accounts.beginEvent();
      accounts.clear();
      repository.individual().visitAccounts( new AccountVisitor()
      {

         public void visitAccount( Account account )
         {
            itemBuilder.prototype().description().set( account.accountSettings().name().get() );
            itemBuilder.prototype().entity().set( EntityReference.getEntityReference( (account) ) );
            accounts.add( itemBuilder.newInstance() );
         }
      } );
      uow.discard();
      accounts.commitEvent();
   }
}