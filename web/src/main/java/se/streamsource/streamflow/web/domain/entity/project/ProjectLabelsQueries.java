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

package se.streamsource.streamflow.web.domain.entity.project;

import org.qi4j.api.entity.association.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import se.streamsource.streamflow.web.domain.structure.casetype.*;
import se.streamsource.streamflow.web.domain.structure.label.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;

import java.util.*;

@Mixins(ProjectLabelsQueries.Mixin.class)
public interface ProjectLabelsQueries
{
   Map<Label, SelectedLabels> possibleLabels( CaseType caseType );

   class Mixin
         implements ProjectLabelsQueries
   {
      @This
      SelectedLabels selectedLabels;

      @This
      OwningOrganizationalUnit.Data ownerOU;


      public Map<Label, SelectedLabels> possibleLabels( CaseType caseType )
      {
         Map<Label, SelectedLabels> labels = new LinkedHashMap<Label, SelectedLabels>( );

         addLabels( labels, selectedLabels );

         addLabels( labels, caseType );

         // Add labels from OU
         OrganizationalUnit ou = ownerOU.organizationalUnit().get();
         addLabels( labels, ou );

         // Add labels from Organization of OU
         OwningOrganization ownerOrg = (OwningOrganization) ou;
         Organization org = ownerOrg.organization().get();
         addLabels( labels, org );

         return labels;
      }

      private void addLabels( Map<Label, SelectedLabels> labels, SelectedLabels from)
      {
         ManyAssociation<Label> selectedLabels = ((SelectedLabels.Data)from).selectedLabels();
         for (Label label : selectedLabels)
         {
            labels.put( label, from );
         }
      }
   }
}