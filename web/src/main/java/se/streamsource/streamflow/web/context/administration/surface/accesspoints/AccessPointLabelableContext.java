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
package se.streamsource.streamflow.web.context.administration.surface.accesspoints;

import static se.streamsource.dci.api.RoleMap.role;

import java.util.List;
import java.util.Map;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.project.ProjectLabelsQueries;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPointSettings;
import se.streamsource.streamflow.web.domain.structure.project.Project;

/**
 * JAVADOC
 */
public class AccessPointLabelableContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      return new LinksBuilder( module.valueBuilderFactory() ).addDescribables( role( Labelable.Data.class ).labels() ).newLinks();
   }

   public LinksValue possiblelabels()
   {
      AccessPointSettings.Data accessPoint = RoleMap.role( AccessPointSettings.Data.class );
      Labelable.Data labelsData = RoleMap.role( Labelable.Data.class );
      Project project = accessPoint.project().get();
      CaseType caseType = accessPoint.caseType().get();

      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "addlabel" );
      if ( project != null )
      {
         ProjectLabelsQueries labelsQueries = (ProjectLabelsQueries) project;

         Map<Label, SelectedLabels> map = labelsQueries.possibleLabels( caseType );
         try
         {
            List<Label> labels = labelsData.labels().toList();
            for (Label label : map.keySet())
            {
               if (!labels.contains( label ))
               {
                  linksBuilder.addDescribable( label, ((Describable) map.get( label )).getDescription() );
               }
            }
         } catch (IllegalArgumentException e)
         {
            linksBuilder.addDescribables( map.keySet() );
         }
      }
      return linksBuilder.newLinks();
   }

   public void addlabel( @Name("entity") Label label)
   {
      Labelable labelable = role( Labelable.class );
      labelable.addLabel( label );
   }
}