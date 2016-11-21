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
package se.streamsource.streamflow.reference.assembler;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.reference.contact.StreamflowContactLookupPlugin;
import se.streamsource.streamflow.reference.contact.StreamflowContactLookupPluginConfiguration;

/**
 * Register reference plugins in the plugin application
 */
public class PluginAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.entities( StreamflowContactLookupPluginConfiguration.class ).visibleIn( Visibility.application );
      
      module.services( StreamflowContactLookupPlugin.class ).
            identifiedBy( "streamflowcontactlookup" ).
            visibleIn( Visibility.application ).
            instantiateOnStartup();


   }
}
