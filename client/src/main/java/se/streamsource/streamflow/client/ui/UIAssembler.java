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

package se.streamsource.streamflow.client.ui;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers;
import se.streamsource.streamflow.client.ui.administration.UsersAndGroupsModel;

import static org.qi4j.api.common.Visibility.*;

/**
 * JAVADOC
 */
public class UIAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.addObjects(
            StreamflowApplication.class,
            AccountSelector.class
      );

      // SAF objects
      module.importServices( StreamflowApplication.class, ApplicationContext.class, AccountSelector.class ).visibleIn( layer );


      module.addServices( DummyDataService.class ).instantiateOnStartup();
      module.addServices( ApplicationInitializationService.class ).instantiateOnStartup();

      UIAssemblers.addDialogs( module, NameDialog.class,
            SelectUsersAndGroupsDialog.class,
            CreateUserDialog.class,
            CreateProxyUserDialog.class,
            ConfirmationDialog.class,
            ResetPasswordDialog.class );

      UIAssemblers.addModels( module,
            UsersAndGroupsModel.class );

      module.addObjects( DebugWindow.class );

   }
}
