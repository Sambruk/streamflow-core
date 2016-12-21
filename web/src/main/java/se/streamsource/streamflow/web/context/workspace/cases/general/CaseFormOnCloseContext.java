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
import se.streamsource.streamflow.web.context.workspace.cases.HasFormOnClose;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.FormOnClose;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;

/**
 * JAVADOC
 */
public class CaseFormOnCloseContext
{
   @Structure
   Module module;

   @HasFormOnClose(true)
   public void create( )
   {
      FormDrafts formDrafts = RoleMap.role( FormDrafts.class );
      CaseType caseType = RoleMap.role( TypedCase.Data.class ).caseType().get();
      Form form = ((FormOnClose.Data)caseType).formOnClose().get();

      formDrafts.createFormDraft( form );
   }

   @HasFormOnClose(true)
   public LinkValue formdraft(  )
   {
      CaseType caseType = RoleMap.role( TypedCase.Data.class ).caseType().get();
      Form form = ((FormOnClose.Data)caseType).formOnClose().get();

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
