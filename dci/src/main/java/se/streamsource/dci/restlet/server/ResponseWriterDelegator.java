/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.dci.restlet.server;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.api.service.ServiceReference;
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
public class ResponseWriterDelegator
        implements ResponseWriter
{
   List<ResponseWriter> responseWriters = new ArrayList<ResponseWriter>();

   @Structure
   Module module;

   public void init(@Service Iterable<ServiceReference<ResponseWriter>> resultWriters) throws InitializationException
   {
      Logger logger = LoggerFactory.getLogger(getClass());

      // Add custom writers first
      for (ServiceReference<ResponseWriter> resultWriter : resultWriters)
      {
         if (!resultWriter.identity().equals("responsewriterdelegator"))
         {
            logger.info("Registered result writer:" + resultWriter.identity());
            registerResultWriter(resultWriter.get());
         }
      }

      // Add defaults
      ResourceBundle defaultResultWriters = ResourceBundle.getBundle("commandquery");

      String resultWriterClasses = defaultResultWriters.getString("responsewriters");
      logger.info("Using response writers:" + resultWriterClasses);
      for (String className : resultWriterClasses.split(","))
      {
         try
         {
            Class writerClass = module.classLoader().loadClass(className.trim());
            ResponseWriter writer = (ResponseWriter) module.objectBuilderFactory().newObject(writerClass);
            registerResultWriter(writer);
         } catch (ClassNotFoundException e)
         {
            logger.warn("Could not register response writer " + className, e);
         }
      }
   }

   public void registerResultWriter(ResponseWriter writer)
   {
      responseWriters.add(writer);
   }

   public boolean write(Object result, Response response)
   {
      for (ResponseWriter responseWriter : responseWriters)
      {
         if (responseWriter.write(result, response))
         {
            return true;
         }
      }
      return false;
   }
}
