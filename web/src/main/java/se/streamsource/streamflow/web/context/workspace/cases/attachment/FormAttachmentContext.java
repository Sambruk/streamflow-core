/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import static se.streamsource.dci.api.RoleMap.role;
import static se.streamsource.streamflow.util.Strings.empty;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.io.Input;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.UpdateContext;
import se.streamsource.streamflow.api.workspace.cases.attachment.UpdateAttachmentDTO;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.FormAttachments;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

/**
 * JAVADOC
 */
public class FormAttachmentContext
      implements UpdateContext<UpdateAttachmentDTO>, DeleteContext
{
   @Structure
   Module module;

   @Service
   AttachmentStore store;

   @RequiresPermission(PermissionType.write)
   public void delete() //throws IOException
   {
      FormAttachments attachments = role( FormAttachments.class );
      Attachment attachment = role( Attachment.class );

      attachments.removeFormAttachment( attachment );
   }

   @RequiresPermission(PermissionType.write)
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

   public Input<ByteBuffer, IOException> download() throws IOException, URISyntaxException
   {
      AttachedFile.Data fileData = role( AttachedFile.Data.class );

      final String id = new URI( fileData.uri().get() ).getSchemeSpecificPart();

      return store.attachment(id);
   }
}