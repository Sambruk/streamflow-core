package se.streamsource.dci.restlet.server;

import org.restlet.Request;

import java.lang.reflect.Method;

/**
 * A request reader converts the incoming request to one or more arguments to be used when
 * invoking resource/context
 */
public interface RequestReader
{
   Object[] readRequest(Request request, Method method);
}
