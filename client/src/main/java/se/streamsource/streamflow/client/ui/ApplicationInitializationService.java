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
package se.streamsource.streamflow.client.ui;

import org.jdesktop.application.SingleFrameApplication;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.client.ui.account.AccountsModel;

/**
 * JAVADOC
 */
@Mixins(ApplicationInitializationService.Mixin.class)
public interface ApplicationInitializationService
      extends ServiceComposite, Activatable
{
   class Mixin
         implements Activatable
   {
      @Structure
      private Module module;

      @Service
      private SingleFrameApplication main;

      public void activate() throws Exception
      {
         module.objectBuilderFactory().newObjectBuilder(SingleFrameApplication.class).use(module.objectBuilderFactory().newObject(AccountsModel.class)).injectTo(main);
      }

      public void passivate() throws Exception
      {
      }

   }

}
