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

package se.streamsource.streamflow.web.context.administration.surface.accesspoints;

import org.qi4j.api.constraint.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.structure.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.project.*;
import se.streamsource.streamflow.web.domain.structure.casetype.*;
import se.streamsource.streamflow.web.domain.structure.label.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.project.*;

import java.util.*;

import static se.streamsource.dci.api.RoleMap.*;

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
      if (project != null && caseType != null)
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