/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.resource;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * JAVADOC
 */
public class EventsClientResource
    extends ClientResource
{
    public EventsClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public void registerClient( String id, InputStream in ) throws ResourceException
    {
        Reference ref = getRequest().getResourceRef();

        setResourceRef( ref.clone().addSegment(id ));
        try
        {
            put( new InputRepresentation(in, MediaType.APPLICATION_JAVA_OBJECT) );
        } finally
        {
            setResourceRef( ref );
        }
    }

    public void deregisterClient( String id ) throws ResourceException
    {
        Reference ref = getRequest().getResourceRef();

        setResourceRef( ref.clone().addSegment(id ));
        try
        {
            delete();
        } finally
        {
            setResourceRef( ref );
        }
    }

    public Representation getEvents( Date afterTimestamp ) throws ResourceException, IOException
    {
        Reference originalRef = getRequest().getResourceRef();
        Reference ref = originalRef.clone();
        ref.addQueryParameter(  "after", afterTimestamp.getTime()+"");
        setReference( ref );
        try
        {
            return get();
        } finally
        {
            setReference( originalRef );
        }
    }
}
