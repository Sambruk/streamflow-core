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
package se.streamsource.streamflow.client.ui.workspace.cases.attachments;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.attachment.AttachmentDTO;
import se.streamsource.streamflow.api.workspace.cases.attachment.UpdateAttachmentDTO;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventStream;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Observable;

import static org.qi4j.api.util.Iterables.filter;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.withNames;

/**
 * JAVADOC
 */
public class AttachmentsModel
   extends Observable
   implements Refreshable
{
   @Service
   EventStream eventStream;

   @Structure
   private Module module;

   @Uses
   private CommandQueryClient client;

   private EventList<AttachmentDTO> eventList = new BasicEventList<AttachmentDTO>();

   public EventList<AttachmentDTO> getEventList()
   {
      return eventList;
   }

   public void createAttachment( final File file, InputStream in) throws IOException
   {
      Representation input = new InputRepresentation(new BufferedInputStream(in));
      Form disposition = new Form();
      disposition.set( Disposition.NAME_FILENAME, file.getName() );
      disposition.set( Disposition.NAME_SIZE, Long.toString(file.length()) );
      disposition.set( Disposition.NAME_CREATION_DATE, DateFunctions.toUtcString( new Date(file.lastModified())) );

      input.setDisposition( new Disposition(Disposition.TYPE_NONE, disposition) );

      // Update with details once file is uploaded
      TransactionListener updateListener = new TransactionListener()
      {
         public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
         {
            for (DomainEvent domainEvent : filter( withNames("createdAttachment" ), Events.events( transactions )))
            {
               ValueBuilder<UpdateAttachmentDTO> builder = module.valueBuilderFactory().newValueBuilder(UpdateAttachmentDTO.class);
               builder.prototype().name().set( file.getName() );
               builder.prototype().size().set( file.length() );

               MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
               MimeType mimeType = MimeUtil.getMostSpecificMimeType( MimeUtil.getMimeTypes( file ));

               builder.prototype().mimeType().set( mimeType.toString() );

               String attachmentId = EventParameters.getParameter( domainEvent, "param1" );
               client.getClient( attachmentId+"/" ).postCommand( "update", builder.newInstance() );
            }
         }
      };
      eventStream.registerListener( updateListener );

      try
      {
         client.postCommand( "createattachment", input);
      } finally
      {
         eventStream.unregisterListener( updateListener );
      }
   }

   public void refresh() throws OperationException
   {
      ResourceValue resource = client.query();
      final LinksValue newRoot = (LinksValue) resource.index().get();
      EventListSynch.synchronize( newRoot.links().get(), eventList );

      setChanged();
      notifyObservers(resource);
   }

   public void removeAttachment( AttachmentDTO attachment )
   {
      client.getClient( attachment ).delete();
   }

   public Representation download( AttachmentDTO attachment ) throws IOException
   {
      return client.getClient( attachment ).query("download", Representation.class);
   }
}
