/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.restlet.Uniform;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.ui.administration.AdministrationModel;
import se.streamsource.streamflow.client.ui.overview.OverviewModel;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceModel;

import java.io.IOException;
import java.util.Observable;

/**
 * JAVADOC
 */
public class AccountModel extends Observable
{
   @Structure
   Module module;

   @Service
   IndividualRepository individualRepository;

   @Service
   Uniform client;

   @Uses
   Account account;

   public AccountSettingsValue settings()
   {
      UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();
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
      UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();
      uow.get( account ).updateSettings( value );
      uow.complete();
      setChanged();
      notifyObservers();
   }

   public String test() throws IOException, ResourceException
   {
      UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();
      try
      {
         return uow.get( account ).version( client );
      } finally
      {
         uow.discard();
      }
   }

   public ProfileModel newProfileModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(ProfileModel.class).use(serverResource().getSubClient("account").getSubClient("profile")).newInstance();
   }

   public OverviewModel newOverviewModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(OverviewModel.class).use(serverResource().getSubClient("overview")).newInstance();
   }

   public WorkspaceModel newWorkspaceModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(WorkspaceModel.class).use(serverResource().getSubClient("workspace")).newInstance();
   }

   public AdministrationModel newAdministrationModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(AdministrationModel.class).use(serverResource().getSubClient( "administration" )).newInstance();
   }

   private CommandQueryClient serverResource()
   {
      UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();

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
      UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();
      Account acc = uow.get( account );
      individualRepository.individual().removeAccount( acc );
      uow.complete();
   }

   public void changePassword( String oldPassword, String newPassword)
         throws Exception
   {
      UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();

      try
      {
         Account account1 = uow.get( account );
         account1.changePassword( client, oldPassword, newPassword);
         uow.complete();
      } catch (Exception ex)
      {
         uow.discard();
         throw ex;
      }
   }
}
