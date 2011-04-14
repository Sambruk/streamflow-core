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

package se.streamsource.streamflow.web.context.workspace.cases.attachment;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.UpdateContext;
import se.streamsource.streamflow.api.workspace.cases.attachment.UpdateAttachmentDTO;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.FormAttachments;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import java.io.*;
import java.net.*;

import static se.streamsource.dci.api.RoleMap.*;
import static se.streamsource.streamflow.util.Strings.*;

/**
 * JAVADOC
 */
public class FormAttachmentContext
      implements UpdateContext<UpdateAttachmentDTO>, DeleteContext
{
      @Structure
   ValueBuilderFactory vbf;

   @Service
   AttachmentStore store;

   public void delete() //throws IOException
   {
      FormAttachments attachments = role( FormAttachments.class );
      Attachment attachment = role( Attachment.class );

      attachments.removeFormAttachment( attachment );
   }

   public void update( UpdateAttachmentDTO updateDTO)
   {
      AttachedFile.Data fileData = role( AttachedFile.Data.class );
      AttachedFile file = role( AttachedFile.class );

      String name = updateDTO.name().get();
      if (!empty( name ) && !fileData.name().get().equals( name ))
         file.changeName( name );

      String mimeType = updateDTO.mimeType().get();
      if (!empty( mimeType ) && !fileData.mimeType().get().equals( mimeType ))
         file.changeMimeType( mimeType );

      Long size = updateDTO.size().get();
      if (size != null)
         file.changeSize( size );

      String uri = updateDTO.uri().get();
      if (!empty( uri ) && !fileData.uri().get().equals( uri ))
         file.changeUri( uri );
   }

   public Representation download() throws IOException, URISyntaxException
   {
      AttachedFile.Data fileData = role( AttachedFile.Data.class );

      final String id = new URI( fileData.uri().get() ).getSchemeSpecificPart();

      OutputRepresentation inputRepresentation = new OutputRepresentation( new MediaType( fileData.mimeType().get() ), store.getAttachmentSize(id) )
      {
         @Override
         public void write(OutputStream outputStream) throws IOException
         {
            store.attachment(id).transferTo(Outputs.<Object>byteBuffer(outputStream));
         }
      };
      Form downloadParams = new Form();
      downloadParams.set( Disposition.NAME_FILENAME, fileData.name().get() );

      if (fileData.size().get() != null)
      {
         downloadParams.set( Disposition.NAME_SIZE, Long.toString( fileData.size().get() ) );
         inputRepresentation.setSize( fileData.size().get() );
      }
      if (fileData.modificationDate().get() != null)
      {
         downloadParams.set( Disposition.NAME_CREATION_DATE, DateFunctions.toUtcString( fileData.modificationDate().get() ) );
         inputRepresentation.setModificationDate( fileData.modificationDate().get() );
      }

      inputRepresentation.setDisposition( new Disposition( Disposition.TYPE_ATTACHMENT, downloadParams ) );
      return inputRepresentation;
   }
}