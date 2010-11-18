/*
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

package se.streamsource.streamflow.client.ui.workspace.cases.attachments;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.attachment.AttachmentValue;
import se.streamsource.streamflow.domain.attachment.UpdateAttachmentValue;
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

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;
import static org.qi4j.api.util.Iterables.*;

/**
 * JAVADOC
 */
public class AttachmentsModel
   implements Refreshable
{
   @Service
   EventStream eventStream;

   @Structure
   private ValueBuilderFactory vbf;

   @Uses
   private CommandQueryClient client;

   private EventList<AttachmentValue> eventList = new BasicEventList<AttachmentValue>();

   public EventList<AttachmentValue> getEventList()
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
               ValueBuilder<UpdateAttachmentValue> builder = vbf.newValueBuilder( UpdateAttachmentValue.class );
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
      try
      {
         final LinksValue newRoot = client.query( "index", LinksValue.class );
         EventListSynch.synchronize( newRoot.links().get(), eventList );
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_perform_operation, e );
      }
   }

   public void removeAttachment( AttachmentValue attachment )
   {
      client.getClient( attachment ).delete();
   }

   public InputStream download( AttachmentValue attachment ) throws IOException
   {
      return client.getClient( attachment ).queryStream( "download", null );
   }
}
