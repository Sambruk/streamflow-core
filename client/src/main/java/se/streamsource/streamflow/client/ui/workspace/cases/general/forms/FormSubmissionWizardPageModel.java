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

package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import eu.medsea.mimeutil.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.util.*;
import org.qi4j.api.value.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.streamflow.domain.attachment.*;
import se.streamsource.streamflow.domain.form.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.*;

import java.io.*;
import java.util.*;

import static org.qi4j.api.util.Iterables.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

public class FormSubmissionWizardPageModel
{
   @Service
   EventStream eventStream;
   
   @Structure
   ValueBuilderFactory vbf;

   @Uses
   CommandQueryClient client;

   public void updateField( EntityReference reference, String value ) throws ResourceException
   {
      ValueBuilder<FieldValueDTO> builder = vbf.newValueBuilder( FieldValueDTO.class );
      builder.prototype().field().set( reference );
      builder.prototype().value().set( value );

      client.putCommand( "updatefield", builder.newInstance() );
   }

   public void createAttachment( final EntityReference field, final File file, InputStream in) throws IOException
   {
      Representation input = new InputRepresentation(new BufferedInputStream(in));
      Form disposition = new Form();
      disposition.set( Disposition.NAME_FILENAME, file.getName() );
      disposition.set( Disposition.NAME_SIZE, Long.toString(file.length()) );
      disposition.set( Disposition.NAME_CREATION_DATE, DateFunctions.toUtcString( new Date(file.lastModified())) );

      input.setDisposition( new Disposition(Disposition.TYPE_NONE, disposition) );

      // Update with details once file is uploaded
      TransactionListener updateListener = new TransactionListener()
      {
         public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
         {
            for (DomainEvent domainEvent : filter( withNames("createdFormAttachment" ), Events.events( transactions )))
            {
               ValueBuilder<UpdateAttachmentValue> builder = vbf.newValueBuilder( UpdateAttachmentValue.class );
               builder.prototype().name().set( file.getName() );
               builder.prototype().size().set( file.length() );

               MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
               MimeType mimeType = MimeUtil.getMostSpecificMimeType( MimeUtil.getMimeTypes( file ));

               builder.prototype().mimeType().set( mimeType.toString() );

               String attachmentId = EventParameters.getParameter( domainEvent, "param1" );
               client.getClient( "formattachments/" + attachmentId +"/" ).postCommand( "update", builder.newInstance() );

               ValueBuilder<AttachmentFieldDTO> valueBuilder = vbf.newValueBuilder( AttachmentFieldDTO.class );
               valueBuilder.prototype().field().set( field );
               valueBuilder.prototype().name().set( file.getName() );
               valueBuilder.prototype().attachment().set( EntityReference.parseEntityReference( attachmentId ) );

               // must update lastModified before new update
               client.queryResource();
               client.putCommand( "updateattachmentfield",  valueBuilder.newInstance() );
            }
         }
      };
      eventStream.registerListener( updateListener );

      try
      {
         client.getClient( "formattachments/" ).postCommand( "createformattachment", input);
      } finally
      {
         eventStream.unregisterListener( updateListener );
      }
   }
}