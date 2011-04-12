/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import ca.odell.glazedlists.*;
import eu.medsea.mimeutil.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.util.*;
import org.qi4j.api.value.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.domain.attachment.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.*;

import java.io.*;
import java.util.*;

import static org.qi4j.api.util.Iterables.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

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
      ResourceValue resource = client.queryResource();
      final LinksValue newRoot = (LinksValue) resource.index().get();
      EventListSynch.synchronize( newRoot.links().get(), eventList );

      setChanged();
      notifyObservers(resource);
   }

   public void removeAttachment( AttachmentValue attachment )
   {
      client.getClient( attachment ).delete();
   }

   public Representation download( AttachmentValue attachment ) throws IOException
   {
      return client.getClient( attachment ).queryRepresentation( "download", null );
   }
}
