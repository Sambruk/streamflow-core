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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import se.streamsource.dci.api.DeleteInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.streamflow.domain.attachment.UpdateAttachmentValue;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import static se.streamsource.streamflow.util.Strings.notEmpty;

/**
 * JAVADOC
 */
@Mixins(AttachmentContext.Mixin.class)
public interface AttachmentContext
   extends DeleteInteraction, Interactions
{
   public void update( UpdateAttachmentValue updateValue);

   public Representation download() throws IOException, URISyntaxException;

   abstract class Mixin
      extends InteractionsMixin
      implements AttachmentContext
   {
      @Structure
      ValueBuilderFactory vbf;

      @Service
      AttachmentStore store;

      public void delete() throws IOException
      {
         Attachments attachments = context.get( Attachments.class);
         Attachment attachment = context.get(Attachment.class);

         AttachedFile.Data fileData = (AttachedFile.Data) attachment;
         
         String uri = fileData.uri().get();
         
         attachments.deleteAttachment( attachment );

         if (uri.startsWith( "store:" ))
         {
            String id = uri.substring( "store:".length() );
            store.deleteAttachment( id );
         } else
         {
            // Handle external storage of file
         }
      }

      public void update( UpdateAttachmentValue updateValue )
      {
         AttachedFile.Data fileData = context.get(AttachedFile.Data.class );
         AttachedFile file = context.get(AttachedFile.class );

         String name = updateValue.name().get();
         if (notEmpty( name ) && !fileData.name().get().equals(name))
            file.changeName( name );

         String mimeType = updateValue.mimeType().get();
         if (notEmpty( mimeType ) && !fileData.mimeType().get().equals(mimeType))
            file.changeMimeType( mimeType );
         
         Long size = updateValue.size().get();
         if (size != null)
            file.changeSize( size );

         String uri = updateValue.uri().get();
         if (notEmpty( uri ) && !fileData.uri().get().equals(uri))
            file.changeUri( uri );
      }

      public Representation download() throws IOException, URISyntaxException
      {
         AttachedFile.Data fileData = context.get(AttachedFile.Data.class );

         String id = new URI(fileData.uri().get()).getSchemeSpecificPart();

         InputRepresentation inputRepresentation = new InputRepresentation( store.getAttachment( id ), new MediaType( fileData.mimeType().get() ) );
         Form downloadParams = new Form();
         downloadParams.set( Disposition.NAME_FILENAME, fileData.name().get() );

         if (fileData.size().get() != null)
         {
            downloadParams.set( Disposition.NAME_SIZE, Long.toString(fileData.size().get()) );
            inputRepresentation.setSize( fileData.size().get() );
         }
         if (fileData.modificationDate().get() != null)
         {
            downloadParams.set( Disposition.NAME_CREATION_DATE, DateFunctions.toUtcString( fileData.modificationDate().get()) );
            inputRepresentation.setModificationDate( fileData.modificationDate().get() );
         }

         inputRepresentation.setDisposition( new Disposition(Disposition.TYPE_ATTACHMENT, downloadParams) );
         return inputRepresentation;
      }
   }
}