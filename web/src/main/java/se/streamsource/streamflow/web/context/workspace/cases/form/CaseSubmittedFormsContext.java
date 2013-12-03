/**
 *
 * Copyright 2009-2013 Jayway Products AB
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.io.Input;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormsListDTO;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.task.DoubleSignatureTask;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;
import se.streamsource.streamflow.web.rest.service.mail.MailSenderService;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountSubmittedFormsConcern.class)
@Mixins(CaseSubmittedFormsContext.Mixin.class)
public interface CaseSubmittedFormsContext extends IndexContext<SubmittedFormsListDTO>, Context
{

   SubmittedFormDTO submittedform(@Name("index") int index);

   Input<ByteBuffer, IOException> download(@Name("id") String id) throws IOException, URISyntaxException;

   void resenddoublesignemail(@Name("secondsigntaskref") String secondsigntaskref);

   void read(@Name("index") int index);

   abstract class Mixin implements CaseSubmittedFormsContext
   {

      @Service
      AttachmentStore store;

      @Structure
      Module module;

      @Optional
      @Service
      MailSenderService mailSender;

      public SubmittedFormsListDTO index()
      {
         SubmittedFormsQueries forms = RoleMap.role( SubmittedFormsQueries.class );
         return forms.getSubmittedForms();
      }

      public SubmittedFormDTO submittedform(@Name("index") int index)
      {
         SubmittedFormsQueries forms = RoleMap.role( SubmittedFormsQueries.class );
         return forms.getSubmittedForm( index );
      }

      public Input<ByteBuffer, IOException> download(@Name("id") String id) throws IOException, URISyntaxException
      {
         SubmittedFormsQueries forms = RoleMap.role( SubmittedFormsQueries.class );
         AttachmentFieldSubmission value = forms.getAttachmentFieldValue( id );
         if (value != null)
         {
            AttachedFile.Data data = module.unitOfWorkFactory().currentUnitOfWork().get( AttachedFile.Data.class, id );
            final String fileId = new URI( data.uri().get() ).getSchemeSpecificPart();

            return store.attachment( fileId );
         } else
         {
            // 404
            throw new IllegalArgumentException( "No such attached file:" + id );
         }
      }

      public void resenddoublesignemail(@Name("secondsigntaskref") String secondsigntaskref)
      {
         DoubleSignatureTask task = module.unitOfWorkFactory().currentUnitOfWork()
               .get( DoubleSignatureTask.class, secondsigntaskref );
         mailSender.sentEmail( ((DoubleSignatureTask.Data) task).email().get() );
         task.updateLastReminderSent( new DateTime( DateTimeZone.UTC ) );
      }

      public void read(@Name("index") int index)
      {
         RoleMap.role( SubmittedForms.class ).read( index );
      }
   }
}
