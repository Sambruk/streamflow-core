/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Uniform;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.api.administration.ChangePasswordDTO;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;

import java.io.*;
import java.util.*;

/**
 * JAVADOC
 */
public class AccountModel extends Observable
{
   @Structure
   ObjectBuilderFactory obf;

   @Structure
   UnitOfWorkFactory uowf;

   @Service
   IndividualRepository individualRepository;

   @Structure
   ValueBuilderFactory vbf;

   @Service
   Uniform client;

   @Uses
   Account account;

   public AccountSettingsValue settings()
   {
      UnitOfWork uow = uowf.newUnitOfWork();
      Account acc = uow.get( account );
      try
      {
         return acc.accountSettings();
      } finally
      {
         uow.discard();
      }
   }

   public void updateSettings( AccountSettingsValue value )
         throws UnitOfWorkCompletionException
   {
      UnitOfWork uow = uowf.newUnitOfWork();
      uow.get( account ).updateSettings( value );
      uow.complete();
      setChanged();
      notifyObservers();
   }

   public String test() throws IOException, ResourceException
   {
      UnitOfWork uow = uowf.newUnitOfWork();
      try
      {
         return uow.get( account ).version( client );
      } finally
      {
         uow.discard();
      }
   }

   public CommandQueryClient serverResource()
   {
      UnitOfWork uow = uowf.newUnitOfWork();

      try
      {
         return uow.get( account ).server( client );
      } finally
      {
         uow.discard();
      }
   }

   public void remove() throws UnitOfWorkCompletionException
   {
      UnitOfWork uow = uowf.newUnitOfWork();
      Account acc = uow.get( account );
      individualRepository.individual().removeAccount( acc );
      uow.complete();
   }

   public void changePassword( ChangePasswordDTO changePasswordDTO)
         throws Exception
   {
      UnitOfWork uow = uowf.newUnitOfWork();

      try
      {
         Account account1 = uow.get( account );
         account1.changePassword( client, changePasswordDTO);
         uow.complete();
      } catch (Exception ex)
      {
         uow.discard();
         throw ex;
      }
   }
}
