package se.streamsource.streamflow.web.rest;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Filter;
import org.slf4j.LoggerFactory;

/**
 * This class is here to catch and log any exceptions that the application can't handle.
 * 
 * @author henrikreinhold
 *
 */
public class LoggerFilter extends Filter
{

   @Override
   protected int doHandle(Request request, Response response)
   {
      try
      {
         return super.doHandle(request, response);
      } catch (Throwable t)
      {
         LoggerFactory.getLogger(getClass()).error("Unhandled exception occured:", t);
         return STOP;
      }
   }

}
