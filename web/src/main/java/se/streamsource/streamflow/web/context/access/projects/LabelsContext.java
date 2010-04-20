/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.access.projects;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.infrastructure.application.TitledLinksBuilder;
import se.streamsource.streamflow.web.domain.entity.project.ProjectLabelsQueries;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;
import se.streamsource.streamflow.web.domain.structure.project.Project;

import java.util.List;
import java.util.Map;

/**
 * JAVADOC
 */
@Mixins(LabelsContext.Mixin.class)
public interface LabelsContext
   extends SubContexts<LabelsContext>, IndexInteraction<LinksValue>, Interactions
{
   void createaccesspoint( StringValue name );

   abstract class Mixin
      extends InteractionsMixin
      implements LabelsContext
   {
      public LinksValue index()
      {
         ProjectLabelsQueries labelsQueries = context.get( ProjectLabelsQueries.class );

         TitledLinksBuilder linksBuilder = new TitledLinksBuilder( module.valueBuilderFactory() );

         Map<Label,SelectedLabels> map = labelsQueries.possibleLabels( context.get( CaseType.class ) );
         CaseType type = context.get( CaseType.class );
         StringBuilder title = new StringBuilder( type.getDescription() + ": " );
         boolean firstLabel = true;

         try
         {
            List<Label> labels = context.getAll( Label.class );
            for (Label label : map.keySet())
            {
               if ( !labels.contains( label ) )
               {
                  linksBuilder.addDescribable( label );
               } else
               {
                  if (firstLabel)
                  {
                     firstLabel = false;
                  } else
                  {
                     title.append( ", " );
                  }
                  title.append( label.getDescription() );
               }
            }
         } catch (IllegalArgumentException e)
         {
            linksBuilder.addDescribables( map.keySet() );
         }
         linksBuilder.addTitle( title.toString() );
         return linksBuilder.newLinks();
      }

      public void createaccesspoint( StringValue name )
      {
         Project project = context.get( Project.class );
         CaseType caseType = context.get( CaseType.class );
         List<Label> labels = context.getAll( Label.class );

         AccessPoints accessPoints = context.get( AccessPoints.class );
         accessPoints.createAccessPoint( name.string().get(), project, caseType, labels );
      }

      public LabelsContext context( String id )
      {
         context.set( module.unitOfWorkFactory().currentUnitOfWork().get( Label.class, id ) );

         return subContext( LabelsContext.class);
      }
   }
}