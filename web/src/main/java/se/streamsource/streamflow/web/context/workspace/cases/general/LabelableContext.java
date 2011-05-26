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

package se.streamsource.streamflow.web.context.workspace.cases.general;

import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.unitofwork.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.context.*;
import se.streamsource.streamflow.web.domain.interaction.gtd.*;
import se.streamsource.streamflow.web.domain.interaction.security.*;
import se.streamsource.streamflow.web.domain.structure.casetype.*;
import se.streamsource.streamflow.web.domain.structure.label.*;

import java.util.*;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class LabelableContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   @RequiresPermission( PermissionType.read )
   public LinksValue index()
   {
      return new LinksBuilder( module.valueBuilderFactory() ).addDescribables( role( Labelable.Data.class ).labels() ).newLinks();
   }

   @RequiresPermission(PermissionType.write)
   public LinksValue possiblelabels()
   {
      // Fetch all labels from set CaseType ---> Organization
      HashSet<Object> labels = new HashSet<Object>();

      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "addlabel" );
      Owner project = RoleMap.role( Ownable.Data.class ).owner().get();

      // label's for selected case type
      SelectedLabels.Data from = (SelectedLabels.Data) RoleMap.role( TypedCase.Data.class ).caseType().get();
      if (from != null)
         labels.addAll( from.selectedLabels().toSet() );

      if( project != null )
      {
         // project's selected labels
         labels.addAll( ((SelectedLabels.Data) project).selectedLabels().toSet() );


         // OU hirarchy labels from bottom up
         Entity entity = (Entity) ((Ownable.Data) project).owner().get();

         while (entity instanceof Ownable)
         {
            labels.addAll( ((SelectedLabels.Data) entity).selectedLabels().toSet() );
            entity = (Entity) ((Ownable.Data) entity).owner().get();
         }
         // Organization's selected labels
         labels.addAll( ((SelectedLabels.Data) entity).selectedLabels().toSet() );
      }

      // omitt already set labels
      Labelable.Data labelable = RoleMap.role( Labelable.Data.class );

      for (Object object : labels)
      {
         Label label = (Label)object;

         if (!labelable.labels().contains( label ))
         {
            builder.addDescribable( label );
         }
      }
      return builder.newLinks();
   }

   @RequiresPermission( PermissionType.write )
   public void addlabel( EntityValue reference )
   {
      UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
      Labelable labelable = role( Labelable.class );
      Label label = uow.get( Label.class, reference.entity().get() );

      labelable.addLabel( label );
   }
}