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

package se.streamsource.streamflow.client.ui.caze.attachments;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import org.json.JSONObject;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Response;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.restlet.client.ResponseHandler;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.attachment.AttachmentValue;
import se.streamsource.streamflow.domain.attachment.UpdateAttachmentValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventQuery;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * JAVADOC
 */
public class AttachmentsModel
   implements Refreshable, EventListener
{
   @Structure
   private ValueBuilderFactory vbf;

   @Uses
   private CommandQueryClient client;

   private EventList<AttachmentValue> eventList = new BasicEventList<AttachmentValue>();
   private LinksValue attachments;

   public EventList<AttachmentValue> getEventList()
   {
      return eventList;
   }

   public void createAttachment( final File file) throws IOException
   {
      FileInputStream fin = new FileInputStream(file);

      Representation input = new InputRepresentation(new BufferedInputStream(fin));
      Form disposition = new Form();
      disposition.set( Disposition.NAME_FILENAME, file.getName() );
      disposition.set( Disposition.NAME_SIZE, Long.toString(file.length()) );
      disposition.set( Disposition.NAME_CREATION_DATE, DateFunctions.toUtcString( new Date(file.lastModified())) );

      input.setDisposition( new Disposition(Disposition.TYPE_NONE, disposition) );



      client.postCommand( "createattachment", input, new ResponseHandler()
      {
         public void handleResponse( Response response ) throws ResourceException
         {
            if (response.getStatus().isSuccess() &&
                  (response.getRequest().getMethod().equals( Method.POST ) ||
                        response.getRequest().getMethod().equals( Method.DELETE ) ||
                        response.getRequest().getMethod().equals( Method.PUT )))
            {
               try
               {
                  Representation entity = response.getEntity();
                  if (entity != null && !(entity instanceof EmptyRepresentation))
                  {
                     String source = entity.getText();

                     final TransactionEvents transactionEvents = vbf.newValueFromJSON( TransactionEvents.class, source );
                     EventQuery query = new EventQuery().withNames( "createdAttachment" );
                     for (DomainEvent domainEvent : transactionEvents.events().get())
                     {
                        if (query.accept( domainEvent ))
                        {
                           String parameterJson = domainEvent.parameters().get();

                           ValueBuilder<UpdateAttachmentValue> builder = vbf.newValueBuilder( UpdateAttachmentValue.class );
                           builder.prototype().name().set( file.getName() );
                           builder.prototype().size().set( file.length() );

                           MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
                           MimeType mimeType = MimeUtil.getMostSpecificMimeType( MimeUtil.getMimeTypes( file ));

                           builder.prototype().mimeType().set( mimeType.toString() );

                           String attachmentId = new JSONObject(parameterJson).getString( "param1" );
                           client.getClient( attachmentId+"/" ).postCommand( "update", builder.newInstance() );
                        }
                     }
                  }
               } catch (Exception e)
               {
                  throw new RuntimeException( "Could not process events", e );
               }
            }
         }
      });

      fin.close();
   }

   public void refresh() throws OperationException
   {
      try
      {
         final LinksValue newRoot = client.query( "index", LinksValue.class );
         boolean same = newRoot.equals( attachments );
         if (!same)
         {
               EventListSynch.synchronize( newRoot.links().get(), eventList );
               attachments = newRoot;
         }
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_perform_operation, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
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
