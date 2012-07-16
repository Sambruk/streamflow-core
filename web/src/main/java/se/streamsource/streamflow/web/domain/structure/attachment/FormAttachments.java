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
package se.streamsource.streamflow.web.domain.structure.attachment;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * List of attached files
 */
@Mixins(FormAttachments.Mixin.class)
public interface FormAttachments
{
   Attachment createFormAttachment( String uri ) throws URISyntaxException;

   Attachment getFormAttachment( String id );

   void addFormAttachment( Attachment attachment );

   void removeFormAttachment( Attachment attachment );

   boolean hasFormAttachments();

   void moveAttachments( FormAttachments toAttachments );

   void moveAttachment( FormAttachments toAttachments, Attachment fromAttachment );

   interface Data
   {
      @Aggregated
      @Queryable(false)
      ManyAssociation<Attachment> formAttachments();

      Attachment createdFormAttachment( @Optional DomainEvent event, String id );

      void addedFormAttachment( @Optional DomainEvent event, Attachment attachment );

      void removedFormAttachment( @Optional DomainEvent event, Attachment attachment );
   }

   abstract class Mixin
         implements FormAttachments, Data
   {
      @Service
      IdentityGenerator idGen;

      public Attachment createFormAttachment( String uri ) throws URISyntaxException
      {
         // Check if URI is valid
         new URI( uri );

         Attachment attachment = createdFormAttachment( null, idGen.generate( Identity.class ) );
         attachment.changeUri( uri );
         attachment.changeName( "New attachment" );

         addedFormAttachment( null, attachment );

         return attachment;
      }

      public void addFormAttachment( Attachment attachment )
      {
         for (Attachment anAttachment : formAttachments().toList())
         {
            if (attachment.toString().equals( anAttachment.toString() ))
            {
               // already present so ignore
               return;
            }
         }
         addedFormAttachment( null, attachment );
      }

      public void removeFormAttachment( Attachment attachment )
      {
         // Delete the attachment entity
         removedFormAttachment( null, attachment );

         attachment.deleteEntity();
      }

      public Attachment getFormAttachment( String id )
      {
         for (Attachment attachment : formAttachments())
         {
            if (attachment.toString().equals( id ))
            {
               return attachment;
            }
         }
         return null;
      }

      public boolean hasFormAttachments()
      {
         return !formAttachments().toList().isEmpty();
      }

      public void moveAttachments( FormAttachments toAttachments )
      {
         for (Attachment fromAttachment : formAttachments())
         {
            toAttachments.addFormAttachment( fromAttachment );
            removedFormAttachment( null, fromAttachment );
         }
      }

      public void moveAttachment( FormAttachments toAttachments, Attachment fromAttachment )
      {
         toAttachments.addFormAttachment( fromAttachment );
         removedFormAttachment( null, fromAttachment );
      }
   }
}
