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

package se.streamsource.streamflow.web.domain.entity.caze;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
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

@Mixins(CaseLabelsQueries.Mixin.class)
public interface CaseLabelsQueries
{
   Map<Label, SelectedLabels> possibleLabels();

   class Mixin
         implements CaseLabelsQueries
   {
      @This
      Ownable.Data ownable;

      @This
      CreatedOn created;

      @This
      TypedCase.Data type;

      @This
      Labelable.Data labelable;

      @Structure
      ValueBuilderFactory vbf;

      public Map<Label, SelectedLabels> possibleLabels()
      {
         Map<Label, SelectedLabels> labels = new LinkedHashMap<Label, SelectedLabels>( );

         if (ownable.owner().get() instanceof SelectedLabels)
         {
            addLabels( labels, (SelectedLabels) ownable.owner().get() );
         }

         if (type.caseType().get() != null)
         {
            addLabels( labels, type.caseType().get());
         }

         if (ownable.owner().get() != null)
         {
            // Add labels from OU
            OwningOrganizationalUnit.Data ownerOU = (OwningOrganizationalUnit.Data) ownable.owner().get();
            OrganizationalUnit ou = ownerOU.organizationalUnit().get();
            addLabels( labels, ou );

            // Add labels from Organization of OU
            OwningOrganization ownerOrg = (OwningOrganization) ou;
            Organization org = ownerOrg.organization().get();
            addLabels( labels, org );
         } else
         {
            // Add labels from Organizations that creator is member of
            Creator creator = created.createdBy().get();
            if (creator instanceof OrganizationParticipations)
            {
               OrganizationParticipations.Data orgs = (OrganizationParticipations.Data) creator;

               for (Organization organization : orgs.organizations())
               {
                  addLabels(labels, organization);
               }
            }
         }

         return labels;
      }

      private void addLabels( Map<Label, SelectedLabels> labels, SelectedLabels from)
      {
         ManyAssociation<Label> selectedLabels = ((SelectedLabels.Data)from).selectedLabels();
         for (Label label : selectedLabels)
         {
            if (!labelable.labels().contains( label ))
            {
               labels.put( label, from );
            }
         }
      }
   }
}
