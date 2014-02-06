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
package se.streamsource.dci.restlet.client;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.api.structure.Module;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Delegates to a list of potential writers. Register writers on startup.
 */
public class RequestWriterDelegator
   implements RequestWriter, Initializable
{
   List<RequestWriter> requestWriters = new ArrayList<RequestWriter>( );

   @Structure
   Module module;

   public void initialize() throws InitializationException
   {
      Logger logger = LoggerFactory.getLogger( getClass() );

      ResourceBundle defaultRequestWriters = ResourceBundle.getBundle( "commandquery" );

      String requestWriterClasses = defaultRequestWriters.getString( "requestwriters" );
      logger.info( "Using request writers:"+requestWriterClasses );
      for (String className : requestWriterClasses.split( "," ))
      {
         try
         {
            Class writerClass = module.classLoader().loadClass( className.trim() );
            RequestWriter requestWriter = (RequestWriter) module.objectBuilderFactory().newObject( writerClass );
            registerRequestWriter(requestWriter);
         } catch (ClassNotFoundException e)
         {
            logger.warn( "Could not register request writer "+className, e );
         }
      }
   }

   public void registerRequestWriter(RequestWriter writer)
   {
      requestWriters.add( writer );
   }

   public boolean writeRequest(Object requestObject, Request request) throws ResourceException
   {
      if (requestObject == null)
      {
         if (!Method.GET.equals(request.getMethod()))
            request.setEntity(new EmptyRepresentation());

         return true;
      }

      if (requestObject instanceof Representation)
      {
         request.setEntity((Representation) requestObject);
         return true;
      }

      for (RequestWriter requestWriter : requestWriters)
      {
         if (requestWriter.writeRequest(requestObject, request))
            return true;
      }

      return false;
   }
}