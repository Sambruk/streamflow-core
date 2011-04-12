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

package se.streamsource.dci.restlet.server;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.service.*;
import org.qi4j.api.structure.*;
import org.restlet.*;
import org.slf4j.*;

import java.util.*;

/**
 * Delegates to a list of potential writers. Register writers on startup.
 */
public class ResultWriterDelegator
        implements ResultWriter
{
   List<ResultWriter> responseWriters = new ArrayList<ResultWriter>();

   @Structure
   Module module;

   public void init(@Service Iterable<ServiceReference<ResultWriter>> resultWriters) throws InitializationException
   {
      Logger logger = LoggerFactory.getLogger(getClass());

      // Add custom writers first
      for (ServiceReference<ResultWriter> resultWriter : resultWriters)
      {
         if (!resultWriter.identity().equals("resultwriterdelegator"))
         {
            logger.info("Registered result writer:" + resultWriter.identity());
            registerResultWriter(resultWriter.get());
         }
      }

      // Add defaults
      ResourceBundle defaultResultWriters = ResourceBundle.getBundle("resultwriters");

      String resultWriterClasses = defaultResultWriters.getString("resultwriters");
      logger.info("Using resultwriters:" + resultWriterClasses);
      for (String className : resultWriterClasses.split(","))
      {
         try
         {
            Class writerClass = module.classLoader().loadClass(className.trim());
            ResultWriter writer = (ResultWriter) module.objectBuilderFactory().newObject(writerClass);
            registerResultWriter(writer);
         } catch (ClassNotFoundException e)
         {
            logger.warn("Could not register result writer " + className, e);
         }
      }
   }

   public void registerResultWriter(ResultWriter writer)
   {
      responseWriters.add(writer);
   }

   public boolean write(Object result, Response response)
   {
      for (ResultWriter responseWriter : responseWriters)
      {
         if (responseWriter.write(result, response))
            return true;
      }
      return false;
   }
}
