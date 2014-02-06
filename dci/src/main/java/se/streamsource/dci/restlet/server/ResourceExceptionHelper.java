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
package se.streamsource.dci.restlet.server;

import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.api.RoleMap;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

/**
 * Helper class for converstion of Exceptions to ResourceExceptions
 */
public class ResourceExceptionHelper
{
   public static void handleException(final Class clazz, Response response, Throwable ex)
   {
      while (ex instanceof InvocationTargetException)
      {
         ex = ex.getCause();
      }

      try
      {
         throw ex;
      } catch (ResourceException e)
      {
         // IAE (or subclasses) are considered client faults
         response.setEntity(new StringRepresentation(e.getMessage()));
         response.setStatus(e.getStatus());
      } catch (ConstraintViolationException e)
      {
         try
         {
            ConstraintViolationMessages cvm = new ConstraintViolationMessages();

            // CVE are considered client faults
            String messages = "";
            Locale locale = RoleMap.role( Locale.class );
            for (ConstraintViolation constraintViolation : e.constraintViolations())
            {
               if (!messages.equals(""))
                  messages += "\n";
               messages += cvm.getMessage(constraintViolation, locale);
            }

            response.setEntity(new StringRepresentation(messages));
            response.setStatus( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
         } catch (Exception e1)
         {
            response.setEntity(new StringRepresentation(e.getMessage()));
            response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
         }
      } catch (IllegalArgumentException e)
      {
         // IAE (or subclasses) are considered client faults
         response.setEntity(new StringRepresentation(e.getMessage()));
         response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
      } catch (RuntimeException e)
      {
         // RuntimeExceptions are considered server faults
         LoggerFactory.getLogger( clazz ).warn("Exception thrown during processing", e);
         response.setEntity(new StringRepresentation(e.getMessage()));
         response.setStatus(Status.SERVER_ERROR_INTERNAL);
      } catch (Exception e)
      {
         // Checked exceptions are considered client faults
         String s = e.getMessage();
         if (s == null)
            s = e.getClass().getSimpleName();
         response.setEntity(new StringRepresentation(s));
         response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
      } catch (Throwable e)
      {
         // Anything else are considered server faults
         LoggerFactory.getLogger(clazz).error("Exception thrown during processing", e);
         response.setEntity(new StringRepresentation(e.getMessage()));
         response.setStatus(Status.SERVER_ERROR_INTERNAL);
      }
   }
}
