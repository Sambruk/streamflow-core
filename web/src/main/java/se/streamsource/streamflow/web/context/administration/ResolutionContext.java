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
package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolutions;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedResolutions;

/**
 * JAVADOC
 */
public class ResolutionContext
      implements DeleteContext
{
   @Structure
   Module module;

   public LinksValue usages()
   {
      Query<SelectedResolutions> usageQuery = RoleMap.role( Resolutions.class ).usages( RoleMap.role( Resolution.class ) );
      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ); // TODO What to use for path here?
      for (SelectedResolutions selectedResolutions : usageQuery)
      {
         builder.addDescribable( (Describable) selectedResolutions, ((Describable)((Ownable.Data)selectedResolutions).owner().get()).getDescription() );
      }

      return builder.newLinks();
   }

   public void delete()
   {
      Resolutions resolutions = RoleMap.role( Resolutions.class );
      Resolution resolution = RoleMap.role( Resolution.class );

      resolutions.removeResolution( resolution );
   }
}