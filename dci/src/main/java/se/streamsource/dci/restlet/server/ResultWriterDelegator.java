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

package se.streamsource.dci.restlet.server;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.api.structure.Module;
import org.restlet.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Delegates to a list of potential writers. Register writers on startup.
 */
public class ResultWriterDelegator
   implements ResultWriter, Initializable
{
   List<ResultWriter> responseWriters = new ArrayList<ResultWriter>( );

   @Structure
   Module module;

   public void initialize() throws InitializationException
   {
      Logger logger = LoggerFactory.getLogger( getClass() );

      ResourceBundle defaultResultWriters = ResourceBundle.getBundle( "resultwriters" );

      String resultWriterClasses = defaultResultWriters.getString( "resultwriters" );
      logger.info( "Using resultwriters:"+resultWriterClasses );
      for (String className : resultWriterClasses.split( "," ))
      {
         try
         {
            Class writerClass = module.classLoader().loadClass( className.trim() );
            ResultWriter writer = (ResultWriter) module.objectBuilderFactory().newObject( writerClass );
            registerResultWriter( writer );
         } catch (ClassNotFoundException e)
         {
            logger.warn( "Could not register result writer "+className, e );
         }
      }
   }

   public void registerResultWriter(ResultWriter writer)
   {
      responseWriters.add( writer );
   }

   public boolean write( Object result, Response response )
   {
      for (ResultWriter responseWriter : responseWriters)
      {
         if (responseWriter.write( result, response ))
            return true;
      }
      return false;
   }
}
