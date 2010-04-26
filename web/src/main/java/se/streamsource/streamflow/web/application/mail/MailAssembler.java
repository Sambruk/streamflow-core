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

package se.streamsource.streamflow.web.application.mail;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

/**
 * Assembler for mail module
 */
public class MailAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {

      if (module.layerAssembly().applicationAssembly().mode() == Application.Mode.production )
      {
         module.addServices( MailService.class ).identifiedBy( "mail" ).instantiateOnStartup().visibleIn( Visibility.application );
      }
   }
}