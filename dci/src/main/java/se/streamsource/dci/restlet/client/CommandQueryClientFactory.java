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

package se.streamsource.dci.restlet.client;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.ClientInfo;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Reference;

import java.util.Collections;
import java.util.Locale;

/**
 * Builder for CommandQueryClient
 */
public class CommandQueryClientFactory
{
   @Structure
   private Module module;

   @Uses @Optional
   private ClientCache cache ;

   @Uses
   @Optional
   private ResponseHandler handler = new NullResponseHandler();

   @Uses
   private ResponseReaderDelegator readerDelegator;

   @Uses
   private RequestWriterDelegator requestWriterDelegator;

   @Uses
   private Uniform client;

   public CommandQueryClient newClient( Reference reference )
   {
      return module.objectBuilderFactory().newObjectBuilder( CommandQueryClient.class ).use( this, reference ).newInstance();
   }

   ResponseHandler getHandler()
   {
      return handler;
   }

   void updateCommandRequest( Request request)
   {
      ClientInfo info = new ClientInfo();
      info.setAgent( "Streamflow" ); // TODO Set versions correctly
      info.setAcceptedMediaTypes( Collections.singletonList( new Preference<MediaType>( MediaType.APPLICATION_JSON ) ) );
      info.setAcceptedLanguages( Collections.singletonList( new Preference<Language>(new Language( Locale.getDefault().getLanguage()) )));
      request.setClientInfo( info );

      if (cache != null)
         cache.updateCommandConditions( request );
   }

   void updateQueryRequest( Request request)
   {
      ClientInfo info = new ClientInfo();
      info.setAcceptedMediaTypes( Collections.singletonList( new Preference<MediaType>( MediaType.APPLICATION_JSON ) ) );
      info.setAcceptedLanguages( Collections.singletonList( new Preference<Language>( new Language( Locale.getDefault().getLanguage() ) ) ) );
      request.setClientInfo( info );
   }

   void updateCache( Response response)
   {
      if (cache != null)
         cache.updateCache( response );
   }

   Uniform getClient()
   {
      return client;
   }

   <T> T readResponse( Response response, Class<T> queryResult)
   {
      return (T) readerDelegator.readResponse( response, queryResult );
   }

   public void writeRequest( Request request, Object queryRequest )
   {
      if (!requestWriterDelegator.writeRequest(queryRequest, request))
         throw new IllegalArgumentException("Illegal query request type:"+queryRequest.getClass().getName());
   }
}
