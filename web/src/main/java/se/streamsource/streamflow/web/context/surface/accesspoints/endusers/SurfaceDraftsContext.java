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

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers;

import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.query.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.domain.contact.*;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.caze.*;
import se.streamsource.streamflow.web.domain.entity.caze.*;
import se.streamsource.streamflow.web.domain.entity.gtd.*;
import se.streamsource.streamflow.web.domain.structure.caze.*;
import se.streamsource.streamflow.web.domain.structure.form.*;
import se.streamsource.streamflow.web.domain.structure.user.*;

/**
 * JAVADOC
 */
public class SurfaceDraftsContext
        implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      DraftsQueries draftsQueries = RoleMap.role(DraftsQueries.class);
      LinksBuilder linksBuilder = new LinksBuilder(module.valueBuilderFactory());
      linksBuilder.addDescribables(draftsQueries.drafts(null).newQuery(module.unitOfWorkFactory().currentUnitOfWork()));
      return linksBuilder.newLinks();
   }

   public void createcase()
   {
      EndUser endUser = RoleMap.role(EndUser.class);
      EndUserCases endUserCases = RoleMap.role(EndUserCases.class);
      CaseEntity caze = endUserCases.createCase(endUser);

      // Add contact info to case
      ContactValue contact = endUser.getContact();
      if (contact != null)
         caze.updateContact(0, contact);
   }

   public void createcasewithform()
   {
      EndUser endUser = RoleMap.role(EndUser.class);
      EndUserCases endUserCases = RoleMap.role(EndUserCases.class);
      CaseEntity caze = endUserCases.createCaseWithForm(endUser);


      // Add contact info to case
      ContactValue contact = endUser.getContact();
      if (contact != null)
         caze.updateContact(0, contact);
   }

   public CaseFormDTO findcasewithform()
   {
      ValueBuilder<CaseFormDTO> builder = module.valueBuilderFactory().newValueBuilder(CaseFormDTO.class);
      DraftsQueries queries = RoleMap.role(DraftsQueries.class);
      Query<Case> query = queries.drafts(null).newQuery(module.unitOfWorkFactory().currentUnitOfWork());
      for (Case aCase : query)
      {
         SelectedForms.Data data = RoleMap.role(SelectedForms.Data.class);
         Form form = data.selectedForms().get(0);
         FormDraft formSubmission = aCase.getFormDraft(form);
         if (formSubmission != null)
         {
            builder.prototype().caze().set(EntityReference.getEntityReference(aCase));
            builder.prototype().form().set(EntityReference.getEntityReference(formSubmission));
            return builder.newInstance();
         }
      }

      return builder.newInstance();
   }
}