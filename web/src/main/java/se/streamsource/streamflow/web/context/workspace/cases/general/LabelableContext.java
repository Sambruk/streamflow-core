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
package se.streamsource.streamflow.web.context.workspace.cases.general;

import static se.streamsource.dci.api.RoleMap.role;
import static se.streamsource.streamflow.api.workspace.cases.CaseStates.DRAFT;
import static se.streamsource.streamflow.api.workspace.cases.CaseStates.OPEN;

import java.util.HashSet;

import org.qi4j.api.entity.Entity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.domain.entity.RequiresRemoved;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;

/**
 * JAVADOC
 */
@RequiresPermission( PermissionType.read )
public class LabelableContext
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

      // omit already set labels
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

   @RequiresStatus( {DRAFT, OPEN} )
   @RequiresRemoved(false)
   @RequiresPermission( PermissionType.write )
   public void addlabel( EntityValue reference )
   {
      UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
      Labelable labelable = role( Labelable.class );
      Label label = uow.get( Label.class, reference.entity().get() );

      labelable.addLabel( label );
   }
}