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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.web.context.workspace.cases.HasFormOnDelete;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;
import se.streamsource.streamflow.web.domain.structure.organization.FormOnRemove;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;

/**
 * JAVADOC
 */
public class CaseFormOnDeleteContext
{
   @Structure
   Module module;

   @HasFormOnDelete()
   public void create( )
   {
      Organizations.Data orgs = module.unitOfWorkFactory().currentUnitOfWork().get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
      FormOnRemove.Data data = (FormOnRemove.Data) orgs.organization().get();
      Form form = data.formOnRemove().get();

      FormDrafts formDrafts = RoleMap.role( FormDrafts.class );
      formDrafts.createFormDraft( form );
   }

   @HasFormOnDelete()
   public LinkValue formdraft(  )
   {
      Organizations.Data orgs = module.unitOfWorkFactory().currentUnitOfWork().get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
      FormOnRemove.Data data = (FormOnRemove.Data) orgs.organization().get();

      Form form = data.formOnRemove().get();

      FormDrafts formDrafts = RoleMap.role( FormDrafts.class );

      FormDraft formDraft = formDrafts.getFormDraft( form );
      if (formDraft == null)
         throw new IllegalStateException("No form draft available");

      ValueBuilder<LinkValue> builder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
      builder.prototype().id().set( formDraft.toString() );
      builder.prototype().text().set(formDraft.toString());
      builder.prototype().rel().set( "formdraft" );
      builder.prototype().href().set( "formdrafts/"+formDraft.toString()+"/" );
      return builder.newInstance();
   }
}
