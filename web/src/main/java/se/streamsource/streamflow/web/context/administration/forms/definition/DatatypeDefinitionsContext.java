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
package se.streamsource.streamflow.web.context.administration.forms.definition;

import static se.streamsource.dci.api.RoleMap.role;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.CreateContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.web.domain.structure.form.DatatypeDefinition;
import se.streamsource.streamflow.web.domain.structure.form.DatatypeDefinitions;

/**
 * JAVADOC
 */
public class DatatypeDefinitionsContext
      implements IndexContext<Iterable<DatatypeDefinition>>, CreateContext<String, DatatypeDefinition>
{
   @Structure
   Module module;

   public Iterable<DatatypeDefinition> index()
   {
      return role( DatatypeDefinitions.Data.class ).datatypeDefinitions();
   }

   public DatatypeDefinition create( @Name("url") String url )
   {
      DatatypeDefinitions fieldTypeDefinitions = role( DatatypeDefinitions.class );

      return fieldTypeDefinitions.createDatatypeDefinition( url );
   }
}