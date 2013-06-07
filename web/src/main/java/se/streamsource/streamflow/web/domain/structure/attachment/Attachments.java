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
@Mixins(Attachments.Mixin.class)
public interface Attachments
{
   Attachment createAttachment( String uri ) throws URISyntaxException;

   Attachment getAttachment( String id );

   void addAttachment( Attachment attachment );

   void removeAttachment( Attachment attachment );

   boolean hasAttachments();

   interface Data
   {
      @Aggregated
      @Queryable(false)
      ManyAssociation<Attachment> attachments();

      Attachment createdAttachment( @Optional DomainEvent event, String id );

      void addedAttachment( @Optional DomainEvent event, Attachment attachment );

      void removedAttachment( @Optional DomainEvent event, Attachment attachment );
   }

   abstract class Mixin
         implements Attachments, Data
   {
      @Service
      IdentityGenerator idGen;

      public Attachment createAttachment( String uri ) throws URISyntaxException
      {
         // Check if URI is valid
         new URI( uri );

         Attachment attachment = createdAttachment( null, idGen.generate( Identity.class ) );
         attachment.changeUri( uri );
         attachment.changeName( "New attachment" );

         addedAttachment( null, attachment );

         return attachment;
      }

      public void addAttachment( Attachment attachment )
      {
         for (Attachment anAttachment : attachments().toList())
         {
            if ( attachment.toString().equals( anAttachment.toString() ))
            {
               // already present so ignore
               return;
            }
         }
         addedAttachment( null, attachment );
      }

      public void removeAttachment( Attachment attachment )
      {
         // Remove the attachment entity
         removedAttachment( null, attachment );

         attachment.deleteEntity();
      }

      public Attachment getAttachment( String id )
      {
         for (Attachment attachment : attachments())
         {
            if ( attachment.toString().equals( id ) )
            {
               return attachment;
            }
         }
         return null;
      }

      public boolean hasAttachments()
      {
         return attachments().count() != 0;
      }
   }
}
