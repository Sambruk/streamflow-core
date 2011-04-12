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

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.structure.*;
import org.restlet.*;
import org.slf4j.*;

import java.util.*;

/**
 * Delegates to a list of potential readers. Register readers on startup.
 */
public class ResponseReaderDelegator
   implements ResponseReader, Initializable
{
   List<ResponseReader> responseReaders = new ArrayList<ResponseReader>( );

   @Structure
   Module module;

   public void initialize() throws InitializationException
   {
      Logger logger = LoggerFactory.getLogger( getClass() );

      ResourceBundle defaultResponseReaders = ResourceBundle.getBundle( "responsereaders" );

      String responseReaderClasses = defaultResponseReaders.getString( "responsereaders" );
      logger.info( "Using responsereaders:"+responseReaderClasses );
      for (String className : responseReaderClasses.split( "," ))
      {
         try
         {
            Class readerClass = module.classLoader().loadClass( className.trim() );
            ResponseReader reader = (ResponseReader) module.objectBuilderFactory().newObject( readerClass );
            registerResponseReader( reader );
         } catch (ClassNotFoundException e)
         {
            logger.warn( "Could not register response reader "+className, e );
         }
      }
   }

   public void registerResponseReader( ResponseReader reader )
   {
      responseReaders.add( reader );
   }

   public <T> T readResponse( Response response, Class<T> resultType )
   {
      for (ResponseReader responseReader : responseReaders)
      {
         T result = responseReader.readResponse( response, resultType );
         if (result != null)
            return result;
      }

      return null;
   }
}