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

package se.streamsource.streamflow.web.context.workspace.cases.general;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.created.Creator;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;

import java.util.LinkedHashMap;
import java.util.Map;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
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
      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "addlabel" );
      for (Map.Entry<Label, SelectedLabels> labelSelectedLabelsEntry : possibleLabels().entrySet())
      {
         builder.addDescribable( labelSelectedLabelsEntry.getKey(), (Describable) labelSelectedLabelsEntry.getValue() );
      }
      return builder.newLinks();
   }

   public void addlabel( EntityValue reference )
   {
      UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
      Labelable labelable = role( Labelable.class );
      Label label = uow.get( Label.class, reference.entity().get() );

      labelable.addLabel( label );
   }

   private Map<Label, SelectedLabels> possibleLabels()
   {
      Map<Label, SelectedLabels> labels = new LinkedHashMap<Label, SelectedLabels>();

      Owner owner = RoleMap.role( Ownable.Data.class ).owner().get();
      if (owner instanceof SelectedLabels)
      {
         addLabels( labels, (SelectedLabels) owner );
      }

      SelectedLabels from = RoleMap.role( TypedCase.Data.class ).caseType().get();
      if (from != null)
      {
         addLabels( labels, from );
      }

      if (owner != null)
      {
         // Add labels from OU
         OwningOrganizationalUnit.Data ownerOU = (OwningOrganizationalUnit.Data) owner;
         OrganizationalUnit ou = ownerOU.organizationalUnit().get();
         addLabels( labels, ou );

         // Add labels from Organization of OU
         OwningOrganization ownerOrg = (OwningOrganization) ou;
         Organization org = ownerOrg.organization().get();
         addLabels( labels, org );
      } else
      {
         // Add labels from Organizations that creator is member of
         Creator creator = RoleMap.role( CreatedOn.class ).createdBy().get();
         if (creator instanceof OrganizationParticipations)
         {
            OrganizationParticipations.Data orgs = (OrganizationParticipations.Data) creator;

            for (Organization organization : orgs.organizations())
            {
               addLabels( labels, organization );
            }
         }
      }

      return labels;
   }

   private void addLabels( Map<Label, SelectedLabels> labels, SelectedLabels from )
   {
      Labelable.Data labelable = RoleMap.role( Labelable.Data.class );
      ManyAssociation<Label> selectedLabels = ((SelectedLabels.Data) from).selectedLabels();
      for (Label label : selectedLabels)
      {
         if (!labelable.labels().contains( label ))
         {
            labels.put( label, from );
         }
      }
   }
}