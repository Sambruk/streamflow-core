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

package se.streamsource.streamflow.web.domain.structure.attachment;

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * List of attached files
 */
@Mixins(Attachments.Mixin.class)
public interface Attachments
{
   Attachment createAttachment( String uri ) throws URISyntaxException;

   void deleteAttachment( Attachment attachment );

   boolean hasAttachments();

   interface Data
   {
      @Aggregated
      @Queryable(false)
      ManyAssociation<Attachment> attachments();

      Attachment createdAttachment( DomainEvent event, String id );

      void addedAttachment( DomainEvent event, Attachment attachment );

      void removedAttachment( DomainEvent event, Attachment attachment );
   }

   abstract class Mixin
         implements Attachments, Data
   {
      @State
      ManyAssociation<Attachment> attachments;

      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      public Attachment createAttachment( String uri ) throws URISyntaxException
      {
         // Check if URI is valid
         new URI( uri );

         Attachment attachment = createdAttachment( DomainEvent.CREATE, idGen.generate( Identity.class ) );
         attachment.changeUri( uri );
         attachment.changeName( "New attachment" );

         addedAttachment( DomainEvent.CREATE, attachment );

         return attachment;
      }

      public void deleteAttachment( Attachment attachment )
      {
         String uriString = ((AttachedFile.Data) attachment).uri().get();

         // Delete the attachment entity
         removedAttachment( DomainEvent.CREATE, attachment );

         attachment.removeEntity();
      }


      public boolean hasAttachments()
      {
         return !attachments().toList().isEmpty();
      }
   }
}
