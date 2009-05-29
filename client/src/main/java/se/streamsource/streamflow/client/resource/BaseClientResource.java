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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.Value;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.ext.xml.NodeSet;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Node;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.xpath.XPathConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for client-side resources.
 */
public class BaseClientResource
        extends ClientResource
{
    @Structure
    protected ObjectBuilderFactory obf;

    @Structure
    protected ValueBuilderFactory vbf;

    public BaseClientResource(Context context, Reference reference)
    {
        super(context, reference);
    }

    protected Reference postCommand(ValueComposite command) throws ResourceException
    {
        post(new StringRepresentation(command.toJSON()));
        return getLocationRef();
    }

    protected <T extends Value> T getQuery(Class<T> resultValue) throws IOException, ResourceException
    {
        Representation result = get(MediaType.APPLICATION_JSON);

        return vbf.newValueFromJSON(resultValue, result.getText());
    }

    protected <T extends ClientResource> T getSubResource(String pathSegment, Class<T> clientResource)
    {
        return getResource(getReference().clone().addSegment(pathSegment), clientResource);
    }

    protected <T extends ClientResource> T getResource(Reference ref, Class<T> clientResource)
    {
        return obf.newObjectBuilder(clientResource).use(getContext(), ref).newInstance();
    }

    /**
     * Assume that the resource produces XHTML. Get the resolved reference
     * for the link with the "rel" attribute set to the given value.
     *
     * @param rel the rel name
     * @return the resolved reference
     * @throws ResourceException
     */
    protected Reference getLink(String rel) throws ResourceException
    {
        get();

        DomRepresentation dom = getResponseEntityAsDom();
        try
        {
            String link = (String) dom.evaluate("//a[@rel=\"" + rel + "\"]/@href", XPathConstants.STRING);
            return new Reference(getReference(), link);
        } catch (Exception e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not find link", e);
        }
    }

    /**
     * Assume that the resource produces XHTML. Get the resolved references
     * for the links with the "rel" attribute set to the given value.
     *
     * @param rel the rel name
     * @return the resolved reference
     * @throws ResourceException
     */
    protected Iterable<Reference> getLinks(String rel) throws ResourceException
    {
        get();

        DomRepresentation dom = getResponseEntityAsDom();
        try
        {
            NodeSet links = (NodeSet) dom.evaluate("//a[@rel=\"" + rel + "\"]/@href", XPathConstants.NODESET);
            List<Reference> linkList = new ArrayList<Reference>();
            for (Node link : links)
            {
                linkList.add(new Reference(getReference(), link.getTextContent()));
            }

            return linkList;
        } catch (Exception e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not findValues link", e);
        }
    }

    /**
     * Assume that the resource produces XHTML. Get the resolved references
     * for the links with the "rel" attribute set to the given value, and
     * convert them to resources by instantiating the given resource class.
     *
     * @param rel           the rel name
     * @param resourceClass resource class that extends ClientResource
     * @return the resolved references
     * @throws ResourceException
     */
    protected <T extends ClientResource> Iterable<T> getLinkedResources(String rel, Class<T> resourceClass) throws ResourceException
    {
        Iterable<Reference> references = getLinks(rel);
        List<T> resources = new ArrayList<T>();
        for (Reference reference : references)
        {
            resources.add(getResource(reference, resourceClass));
        }
        return resources;
    }

    protected <T extends ClientResource> T getResource(String ref, Class<T> resourceClass)
    {
        return obf.newObjectBuilder(resourceClass).use(getContext(), new Reference(getReference(), ref)).newInstance();
    }

    public DomRepresentation getResponseEntityAsDom()
    {
        DomRepresentation dom = new DomRepresentation(getResponse().getEntity());
        dom.setValidating(false);
        dom.setEntityResolver(new DefaultHandler());
        return dom;
    }
}
