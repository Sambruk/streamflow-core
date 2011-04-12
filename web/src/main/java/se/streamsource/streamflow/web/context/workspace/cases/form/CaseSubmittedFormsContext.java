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

package se.streamsource.streamflow.web.context.workspace.cases.form;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.io.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.value.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.form.*;
import se.streamsource.streamflow.resource.caze.*;
import se.streamsource.streamflow.resource.roles.*;
import se.streamsource.streamflow.util.*;
import se.streamsource.streamflow.web.domain.entity.form.*;
import se.streamsource.streamflow.web.domain.structure.attachment.*;
import se.streamsource.streamflow.web.infrastructure.attachment.*;

import java.io.*;
import java.net.*;

/**
 * JAVADOC
 */
public class CaseSubmittedFormsContext
      implements IndexContext<SubmittedFormsListDTO>
{
   @Service
   AttachmentStore store;

   @Structure
   ValueBuilderFactory vbf;

   @Structure
   UnitOfWorkFactory uowf;

   public SubmittedFormsListDTO index()
   {
      SubmittedFormsQueries forms = RoleMap.role( SubmittedFormsQueries.class );
      return forms.getSubmittedForms();
   }

   public EffectiveFieldsDTO effectivefields()
   {
      SubmittedFormsQueries fields = RoleMap.role( SubmittedFormsQueries.class );

      return fields.effectiveFields();
   }

   public SubmittedFormDTO submittedform( IntegerDTO index )
   {
      SubmittedFormsQueries forms = RoleMap.role( SubmittedFormsQueries.class );
      return forms.getSubmittedForm( index.integer().get() );
   }


   public Representation download( StringValue id ) throws IOException, URISyntaxException
   {
      AttachmentFieldSubmission value = getAttachmentFieldValue( id.string().get() );
      if ( value != null )
      {
         AttachedFile.Data data = uowf.currentUnitOfWork().get( AttachedFile.Data.class, id.string().get() );
         final String fileId = new URI( data.uri().get() ).getSchemeSpecificPart();

         OutputRepresentation outputRepresentation = new OutputRepresentation(  new MediaType( data.mimeType().get() ), store.getAttachmentSize(fileId) )
         {
            @Override
            public void write(OutputStream outputStream) throws IOException
            {
               store.attachment(fileId).transferTo(Outputs.<Object>byteBuffer(outputStream));
            }
         };
         Form downloadParams = new Form();
         downloadParams.set( Disposition.NAME_FILENAME, value.name().get() );

         outputRepresentation.setDisposition(new Disposition(Disposition.TYPE_ATTACHMENT, downloadParams));
         return outputRepresentation;
      } else
      {
         // 404
         return null;
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
                     AttachmentFieldSubmission submission = vbf.newValueFromJSON(AttachmentFieldSubmission.class, fieldDTO.value().get());
                     if (submission.attachment().get().identity().equals(id)) return submission;
                  }
               }
            }
         }
      }
      return null;
   }


}
