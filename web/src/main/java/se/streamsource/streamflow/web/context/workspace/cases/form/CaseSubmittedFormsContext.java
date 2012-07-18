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
package se.streamsource.streamflow.web.context.workspace.cases.form;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.io.Input;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.administration.form.AttachmentFieldValue;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.api.workspace.cases.form.FieldDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormsListDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedPageDTO;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 * JAVADOC
 */
public class CaseSubmittedFormsContext
      implements IndexContext<SubmittedFormsListDTO>
{
   @Service
   AttachmentStore store;

   @Structure
   Module module;

   public SubmittedFormsListDTO index()
   {
      SubmittedFormsQueries forms = RoleMap.role( SubmittedFormsQueries.class );
      return forms.getSubmittedForms();
   }

   public SubmittedFormDTO submittedform( @Name("index") int index )
   {
      SubmittedFormsQueries forms = RoleMap.role( SubmittedFormsQueries.class );
      return forms.getSubmittedForm( index );
   }


   public Input<ByteBuffer, IOException> download( @Name("id") String id ) throws IOException, URISyntaxException
   {
      AttachmentFieldSubmission value = getAttachmentFieldValue( id );
      if ( value != null )
      {
         AttachedFile.Data data = module.unitOfWorkFactory().currentUnitOfWork().get( AttachedFile.Data.class, id );
         final String fileId = new URI( data.uri().get() ).getSchemeSpecificPart();

         return store.attachment(fileId);
      } else
      {
         // 404
         throw new IllegalArgumentException("No such attached file:"+id);
      }
   }

   // find the attachment in all fields every submitted form on this case
   private AttachmentFieldSubmission getAttachmentFieldValue(String id)
   {
      SubmittedFormsQueries forms = RoleMap.role(SubmittedFormsQueries.class);
      for (int i = 0; i < forms.getSubmittedForms().forms().get().size(); i++)
      {
         for (SubmittedPageDTO submittedPageDTO : forms.getSubmittedForm(i).pages().get())
         {
            for (FieldDTO fieldDTO : submittedPageDTO.fields().get())
            {
               if (fieldDTO.fieldType().get().equals(AttachmentFieldValue.class.getName()))
               {
                  if (!Strings.empty(fieldDTO.value().get()))
                  {
                     AttachmentFieldSubmission submission = module.valueBuilderFactory().newValueFromJSON(AttachmentFieldSubmission.class, fieldDTO.value().get());
                     if (submission.attachment().get().identity().equals(id)) return submission;
                  }
               }
            }
         }
      }
      return null;
   }


}
