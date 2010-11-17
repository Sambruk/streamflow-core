/*
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

package se.streamsource.streamflow.web.context.workspace.cases.form;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.domain.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.domain.form.AttachmentFieldValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.resource.caze.EffectiveFieldsDTO;
import se.streamsource.streamflow.resource.caze.FieldDTO;
import se.streamsource.streamflow.resource.caze.SubmittedFormsListDTO;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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

   public Representation download( StringValue id ) throws IOException, URISyntaxException
   {
      AttachmentFieldSubmission value = getAttachmentFieldValue( id.string().get() );
      if ( value != null )
      {
         AttachedFile.Data data = uowf.currentUnitOfWork().get( AttachedFile.Data.class, id.string().get() );
         String fileId = new URI( data.uri().get() ).getSchemeSpecificPart();

         InputRepresentation inputRepresentation = new InputRepresentation( store.getAttachment( fileId ) );
         Form downloadParams = new Form();
         downloadParams.set( Disposition.NAME_FILENAME, value.name().get() );

         inputRepresentation.setDisposition( new Disposition( Disposition.TYPE_ATTACHMENT, downloadParams ) );
         return inputRepresentation;
      } else
      {
         // 404
         return null;
      }

   }

   // find the attachment in all fields every submitted on this case
   private AttachmentFieldSubmission getAttachmentFieldValue( String id )
   {
      SubmittedFormsQueries forms = RoleMap.role( SubmittedFormsQueries.class );
      for ( int i=0; i<forms.getSubmittedForms().forms().get().size(); i++ )
      {
         for (FieldDTO fieldDTO : forms.getSubmittedForm( i ).values().get())
         {
            if ( fieldDTO.fieldType().get().equals( AttachmentFieldValue.class.getName() ) )
            {
               if ( Strings.notEmpty( fieldDTO.value().get() ) )
               {
                  AttachmentFieldSubmission submission = vbf.newValueFromJSON( AttachmentFieldSubmission.class, fieldDTO.value().get() );
                  if ( submission.attachment().get().identity().equals( id )) return submission;
               }
            }
         }
      }
      return null;
   }


}
