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

package se.streamsource.streamflow.web.context.workspace.cases.attachment;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.service.MetadataService;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.domain.attachment.AttachmentValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.FormAttachments;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * JAVADOC
 */
public class FormAttachmentsContext
   implements IndexContext<LinksValue>
{

   @Structure
   Module module;

   @Service
   AttachmentStore store;

   @Service
   FileItemFactory factory;

   @Service
   MetadataService metadata;


   public LinksValue index()
   {
      FormAttachments.Data attachments = RoleMap.role( FormAttachments.Data.class );

      LinksBuilder links = new LinksBuilder( module.valueBuilderFactory() ).rel( "attachment" );
      ValueBuilder<AttachmentValue> builder = module.valueBuilderFactory().newValueBuilder( AttachmentValue.class );
      for (Attachment attachment : attachments.formAttachments())
      {
         AttachedFile.Data data = (AttachedFile.Data) attachment;
         builder.prototype().text().set( data.name().get() );
         builder.prototype().href().set( ((Identity) attachment).identity().get() + "/" );
         builder.prototype().id().set( ((Identity) attachment).identity().get() );
         builder.prototype().size().set( data.size().get() );
         builder.prototype().modificationDate().set( data.modificationDate().get() );
         builder.prototype().mimeType().set( data.mimeType().get() );

         links.addLink( builder.newInstance() );
      }

      return links.newLinks();
   }

   public void createformattachment( Response response ) throws IOException, URISyntaxException
   {
      Request request = response.getRequest();
      Representation representation = request.getEntity();

      if (MediaType.MULTIPART_FORM_DATA.equals( representation.getMediaType(), true ))
      {

         // The Apache FileUpload project parses HTTP requests which
         // conform to RFC 1867, "Form-based File Upload in HTML". That
         // is, if an HTTP request is submitted using the POST method,
         // and with a content type of "multipart/form-data", then
         // FileUpload can parse that request, and get all uploaded files
         // as FileItem.

         // 2/ Create a new file upload handler based on the Restlet
         // FileUpload extension that will parse Restlet requests and
         // generates FileItems.
         RestletFileUpload upload = new RestletFileUpload( factory );


         // 3/ Request is parsed by the handler which generates a
         // list of FileItems
         try
         {
            List items = upload.parseRequest( request );

            // Process only the uploaded item called "file" and
            // save it in the store
            boolean found = false;
            for (final Iterator it = items.iterator(); it
                  .hasNext()
                  && !found;)
            {
               FileItem fi = (FileItem) it.next();
               if (fi.getFieldName().equals( "file" ))
               {
                  found = true;
                  Attachment attachment = createFormAttachment( fi.getInputStream() );

                  // Set name
                  attachment.changeName( fi.getName() );

                  // Set modification date
                  attachment.changeModificationDate( new Date() );

                  // Set size
                  attachment.changeSize( fi.getSize() );

                  // Try to set mimetype
                  //MediaType mediaType = RoleMap.role( Application.class ).getMetadataService().getMediaType( fi.getName().split( "\\." )[1] );
                  MediaType mediaType = metadata.getMediaType( fi.getName().split( "\\." )[1] );
                  if (mediaType != null)
                     attachment.changeMimeType( mediaType.getName() );
               }
            }
         } catch (FileUploadException e)
         {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Could not upload file", e );
         }
      } else
      {
         InputStream in = representation.getStream();

         Attachment attachment = createFormAttachment( in );
         attachment.changeName( "New attachment" );
         attachment.changeModificationDate( new Date() );
      }
   }
   
   private Attachment createFormAttachment( InputStream inputStream ) throws IOException, URISyntaxException
   {
      String id = store.storeAttachment( Inputs.byteBuffer(inputStream, 4096) );

      String url = "store:" + id;

      FormAttachments attachments = RoleMap.role( FormAttachments.class );
      return attachments.createFormAttachment( url );
   }
}