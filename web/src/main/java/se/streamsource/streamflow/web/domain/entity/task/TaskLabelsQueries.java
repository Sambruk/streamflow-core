/*
 * Copyright (c) 2009, Henrik Bernstr&ouml;m. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package se.streamsource.streamflow.web.domain.entity.task;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.ListValueBuilder;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.tasktype.TypedTask;

@Mixins(TaskLabelsQueries.Mixin.class)
public interface TaskLabelsQueries
{
   ListValue possibleLabels();

   class Mixin
         implements TaskLabelsQueries
   {
      @This
      Ownable.Data ownable;

      @This
      TypedTask.Data type;

      @This
      Labelable.Data labelable;

      @Structure
      ValueBuilderFactory vbf;

      public ListValue possibleLabels()
      {
         ListValueBuilder listBuilder = new ListValueBuilder( vbf );

         if (ownable.owner().get() instanceof SelectedLabels)
         {
            addLabels( listBuilder, ((SelectedLabels.Data) ownable.owner().get()).selectedLabels() );
         }

         if (type.taskType().get() != null)
         {
            SelectedLabels.Data taskType = (SelectedLabels.Data) type.taskType().get();
            addLabels( listBuilder, taskType.selectedLabels() );
         }

         if (ownable.owner().get() instanceof OwningOrganizationalUnit.Data)
         {
            // Add labels from OU
            OwningOrganizationalUnit.Data ownerOU = (OwningOrganizationalUnit.Data) ownable.owner().get();
            OrganizationalUnit ou = ownerOU.organizationalUnit().get();
            SelectedLabels.Data ouLabels = (SelectedLabels.Data) ou;
            addLabels( listBuilder, ouLabels.selectedLabels() );

            // Add labels from Organization of OU
            OwningOrganization ownerOrg = (OwningOrganization) ou;
            Organization org = ownerOrg.organization().get();
            SelectedLabels.Data orgLabels = (SelectedLabels.Data) org;
            addLabels( listBuilder, orgLabels.selectedLabels() );
         }

         return listBuilder.newList();
      }

      private void addLabels( ListValueBuilder listBuilder, ManyAssociation<Label> selectedLabels )
      {
         for (Label label : selectedLabels)
         {
            if (!labelable.labels().contains( label ))
            {
               listBuilder.addDescribable( label );
            }
         }
      }
   }
}
