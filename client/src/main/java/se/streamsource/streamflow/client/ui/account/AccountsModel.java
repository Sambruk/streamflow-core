/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.account;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.AccountVisitor;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.util.LinkComparator;
import se.streamsource.streamflow.client.util.WeakModelMap;

import java.util.Observable;
import java.util.Observer;

/**
 * JAVADOC
 */
public class AccountsModel
{
   @Structure
   Module module;

   @Service
   IndividualRepository repository;

   TransactionList<LinkValue> accounts = new TransactionList<LinkValue>( new SortedList<LinkValue>( new BasicEventList<LinkValue>(), new LinkComparator() ) );

   WeakModelMap<String, AccountModel> models = new WeakModelMap<String, AccountModel>()
   {
      protected AccountModel newModel( String key )
      {
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();
         Account acc = uow.get( Account.class, key );
         uow.discard();
         AccountModel accountModel = module.objectBuilderFactory().newObjectBuilder(AccountModel.class).use( acc ).newInstance();
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

   public EventList<LinkValue> getAccounts()
   {
      return accounts;
   }

   public AccountModel accountModel( LinkValue accountLink )
   {
      return models.get( accountLink.id().get() );
   }

   public void newAccount( AccountSettingsValue accountSettingsValue ) throws UnitOfWorkCompletionException, ResourceException
   {
      UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();

      repository.individual().newAccount( accountSettingsValue );

      uow.complete();

      refresh();
   }

   public void removeAccount( LinkValue account ) throws UnitOfWorkCompletionException
   {
      accountModel( account ).remove();
      accounts.remove( account );
   }

   private void refresh()
   {
      UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();
      final ValueBuilder<LinkValue> itemBuilder = module.valueBuilderFactory().newValueBuilder(LinkValue.class);
      accounts.beginEvent();
      accounts.clear();
      repository.individual().visitAccounts( new AccountVisitor()
      {

         public void visitAccount( Account account )
         {
            itemBuilder.prototype().text().set( account.accountSettings().name().get() );
            itemBuilder.prototype().href().set("");
            itemBuilder.prototype().id().set(account.toString());
            accounts.add( itemBuilder.newInstance() );
         }
      } );
      uow.discard();
      accounts.commitEvent();
   }
}