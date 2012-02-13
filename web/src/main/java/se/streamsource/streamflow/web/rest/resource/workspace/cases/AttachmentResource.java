/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.rest.resource.workspace.cases;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.util.DateFunctions;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.streamflow.web.context.workspace.cases.attachment.AttachmentContext;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import static se.streamsource.dci.api.RoleMap.role;

/**
 * TODO
 */
public class AttachmentResource
   extends CommandQueryResource
{
   @Service
   AttachmentStore store;

   public AttachmentResource()
   {
      super(AttachmentContext.class);
   }

   public Representation download() throws IOException, URISyntaxException
   {
      AttachedFile.Data fileData = role( AttachedFile.Data.class );

      final String id = new URI( fileData.uri().get() ).getSchemeSpecificPart();

      final Input<ByteBuffer,IOException> download = context(AttachmentContext.class).download();

      OutputRepresentation outputRepresentation = new OutputRepresentation( new MediaType( fileData.mimeType().get() ), store.getAttachmentSize(id) )
      {
         @Override
         public void write(OutputStream outputStream) throws IOException
         {
            try
            {
               download.transferTo(Outputs.<Object>byteBuffer(outputStream));
            } catch (Exception e)
            {
               throw new IOException(e);
            }
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
         downloadParams.set( Disposition.NAME_CREATION_DATE, DateFunctions.toUtcString(fileData.modificationDate().get()) );
         outputRepresentation.setModificationDate(fileData.modificationDate().get());
      }

      outputRepresentation.setDisposition(new Disposition(Disposition.TYPE_ATTACHMENT, downloadParams));
      return outputRepresentation;
   }}
