/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;

/**
 * JAVADOC
 */
public class CasePossibleFormContext
{
   @Structure
   Module module;

   public void create( )
   {
      FormDrafts formDrafts = RoleMap.role( FormDrafts.class );
      Form form = RoleMap.role( Form.class );

      formDrafts.createFormDraft( form );
   }

   public LinkValue formdraft(  )
   {
      Form form = RoleMap.role( Form.class );

      FormDrafts formDrafts = RoleMap.role( FormDrafts.class );

      FormDraft formDraft = formDrafts.getFormDraft( form );
      if (formDraft == null)
         throw new IllegalStateException("No form draft available");

      ValueBuilder<LinkValue> builder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
      builder.prototype().id().set( formDraft.toString() );
      builder.prototype().text().set(formDraft.toString());
      builder.prototype().rel().set( "formdraft" );
      builder.prototype().href().set( "../formdrafts/"+formDraft.toString()+"/" );
      return builder.newInstance();
   }
}
