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

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.io.*;
import org.qi4j.api.util.*;
import org.qi4j.api.value.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import se.streamsource.dci.api.*;
import se.streamsource.streamflow.domain.attachment.*;
import se.streamsource.streamflow.web.domain.structure.attachment.*;
import se.streamsource.streamflow.web.infrastructure.attachment.*;

import java.io.*;
import java.net.*;

import static se.streamsource.dci.api.RoleMap.*;
import static se.streamsource.streamflow.util.Strings.*;

/**
 * JAVADOC
 */
public class AttachmentContext
      implements UpdateContext<UpdateAttachmentValue>, DeleteContext
{
   @Structure
   ValueBuilderFactory vbf;

   @Service
   AttachmentStore store;

   public void delete() //throws IOException
   {
      Attachments attachments = role( Attachments.class );
      Attachment attachment = role( Attachment.class );

      attachments.removeAttachment( attachment );
   }

   public void update( UpdateAttachmentValue updateValue )
   {
      AttachedFile.Data fileData = role( AttachedFile.Data.class );
      AttachedFile file = role( AttachedFile.class );

      String name = updateValue.name().get();
      if (!empty( name ) && !fileData.name().get().equals( name ))
         file.changeName( name );

      String mimeType = updateValue.mimeType().get();
      if (!empty( mimeType ) && !fileData.mimeType().get().equals( mimeType ))
         file.changeMimeType( mimeType );

      Long size = updateValue.size().get();
      if (size != null)
         file.changeSize( size );

      String uri = updateValue.uri().get();
      if (!empty( uri ) && !fileData.uri().get().equals( uri ))
         file.changeUri( uri );
   }

   public Representation download() throws IOException, URISyntaxException
   {
      AttachedFile.Data fileData = role( AttachedFile.Data.class );

      final String id = new URI( fileData.uri().get() ).getSchemeSpecificPart();

      OutputRepresentation outputRepresentation = new OutputRepresentation( new MediaType( fileData.mimeType().get() ), store.getAttachmentSize(id) )
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
         outputRepresentation.setSize(fileData.size().get());
      }
      if (fileData.modificationDate().get() != null)
      {
         downloadParams.set( Disposition.NAME_CREATION_DATE, DateFunctions.toUtcString( fileData.modificationDate().get() ) );
         outputRepresentation.setModificationDate(fileData.modificationDate().get());
      }

      outputRepresentation.setDisposition(new Disposition(Disposition.TYPE_ATTACHMENT, downloadParams));
      return outputRepresentation;
   }
}