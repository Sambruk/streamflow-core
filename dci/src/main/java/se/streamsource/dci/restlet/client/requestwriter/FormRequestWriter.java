package se.streamsource.dci.restlet.client.requestwriter;

import org.restlet.Request;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.RequestWriter;

/**
 * Request writer for Form. Transfers form state to request reference as query parameters
 */
public class FormRequestWriter
   implements RequestWriter
{
   public boolean writeRequest(Object requestObject, Request request) throws ResourceException
   {
      if (requestObject instanceof Form)
      {
         // Form as query parameters
         if (request.getMethod().equals(Method.GET))
            request.getResourceRef().setQuery(((Form)requestObject).getQueryString());
         else
            request.setEntity(((Form)requestObject).getWebRepresentation(CharacterSet.UTF_8));

         return true;
      }

      return false;
   }
}
