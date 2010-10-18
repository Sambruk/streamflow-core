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

package se.streamsource.streamflow.web.context.caze;

import org.qi4j.api.mixin.Mixins;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.domain.form.FieldValueDTO;
import se.streamsource.streamflow.domain.form.FormDraftValue;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.form.Submitter;

import java.io.IOException;

/**
 * JAVADOC
 */
@Mixins(CaseFormDraftContext.Mixin.class)
public interface CaseFormDraftContext
   extends Context, DeleteContext, IndexContext<FormDraftValue>
{
   public void updatefield( FieldValueDTO field );

   public void submit();

   abstract class Mixin
      extends ContextMixin
      implements CaseFormDraftContext
   {
      public FormDraftValue index()
      {
         FormDraft formDraft = roleMap.get( FormDraft.class);

         return formDraft.getFormDraft();
      }

      public void updatefield( FieldValueDTO field )
      {
         FormDraft formDraft = roleMap.get( FormDraft.class);
         formDraft.changeFieldValue( field.field().get(), field.value().get() );
      }

      public void submit()
      {
         FormDraft formDraft = roleMap.get( FormDraft.class);

         Submitter submitter = roleMap.get( Submitter.class );

         roleMap.get( SubmittedForms.class ).submitForm( formDraft, submitter );
      }

      public void delete() throws ResourceException, IOException
      {
         FormDrafts formDrafts = roleMap.get( FormDrafts.class );
         FormDraft formDraft = roleMap.get( FormDraft.class);
         formDrafts.discardFormDraft( formDraft );
      }
   }
}
