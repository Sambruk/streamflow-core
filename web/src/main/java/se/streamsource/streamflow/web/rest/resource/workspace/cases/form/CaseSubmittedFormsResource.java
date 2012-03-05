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
package se.streamsource.streamflow.web.rest.resource.workspace.cases.form;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Outputs;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.streamflow.web.context.workspace.cases.form.CaseSubmittedFormsContext;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 * TODO
 */
public class CaseSubmittedFormsResource
   extends CommandQueryResource
{
   @Service
   AttachmentStore store;

   public CaseSubmittedFormsResource()
   {
      super(CaseSubmittedFormsContext.class);
   }

   public Representation download( @Name("id") String id ) throws IOException, URISyntaxException
   {
      final Input<ByteBuffer, IOException> file = context(CaseSubmittedFormsContext.class).download(id);

      AttachedFile.Data data = module.unitOfWorkFactory().currentUnitOfWork().get( AttachedFile.Data.class, id );
      final String fileId = new URI( data.uri().get() ).getSchemeSpecificPart();

      OutputRepresentation outputRepresentation = new OutputRepresentation(  new MediaType( data.mimeType().get() ), store.getAttachmentSize(fileId) )
      {
         @Override
         public void write(OutputStream outputStream) throws IOException
         {
            file.transferTo(Outputs.<Object>byteBuffer(outputStream));
         }
      };
      Form downloadParams = new Form();
      downloadParams.set( Disposition.NAME_FILENAME, data.name().get() );

      outputRepresentation.setDisposition(new Disposition(Disposition.TYPE_ATTACHMENT, downloadParams));
      return outputRepresentation;
   }
}
