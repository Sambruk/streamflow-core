/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.context.administration.surface;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

/**
 * Attachments on the organization. These have differing permission requirements compared to the one on cases
 */
public class OrganizationAttachmentsContext
     implements IndexContext<Iterable<Attachment>>
{
   @Structure
   Module module;

   @Service
   AttachmentStore store;

   public Iterable<Attachment> index()
   {
      return RoleMap.role(Attachments.Data.class).attachments();
   }

   /**
    * Marker method. Implementation is in Resource
    */
   public void createattachment()
   {
   }

   public Attachment createAttachment( InputStream inputStream ) throws IOException, URISyntaxException
   {
      String id = store.storeAttachment( Inputs.byteBuffer(inputStream, 4096));

      String url = "store:" + id;

      Attachments attachments = RoleMap.role( Attachments.class );
      return attachments.createAttachment( url );
   }}
