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

package se.streamsource.streamflow.web.resource;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.rest.Qi4jFinder;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import se.streamsource.streamflow.client.resource.CommandQueryClientResource;

/**
 * JAVADOC
 */
public class CommandQueryServerResourceTest
{
    public SingletonAssembler assembler;

    @Before
    public void startWebServer() throws Exception
    {
        assembler = new SingletonAssembler()
        {
            public void assemble(ModuleAssembly module) throws AssemblyException
            {
                module.addObjects(Qi4jFinder.class, TestCommandQueryResource.class, TestCommandQueryClientResource.class);

                module.addValues(TestQuery.class, TestQueryResult.class, TestCommand.class);
            }
        };

        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8040);
        component.getClients().add(Protocol.CLAP);
        component.getClients().add(Protocol.FILE);
        component.getDefaultHost().attach("/resource", createServerResourceFinder(TestCommandQueryResource.class));
        component.start();
    }

    @Test
    public void testOperationListing()
    {
        Client client = new Client(Protocol.HTTP);
        Response response = client.get(new Reference("http://localhost:8040/resource"));
        System.out.println(response.getEntityAsText());
    }

    @Test
    public void testQueryWithoutParams() throws ResourceException
    {
        Client client = new Client(new Context(), Protocol.HTTP);

        Reference ref = new Reference("http://localhost:8040/resource");
        TestCommandQueryClientResource resource = assembler.objectBuilderFactory().newObjectBuilder(TestCommandQueryClientResource.class).use(client.getContext(), ref).newInstance();

        TestQueryResult result = resource.someQuery();
        System.out.println(result);
        assertThat("query was invoked", TestCommandQueryResource.ok, is(true));
    }


    @Test
    public void testQueryWithParam() throws ResourceException
    {
        Client client = new Client(new Context(), Protocol.HTTP);

        Reference ref = new Reference("http://localhost:8040/resource");
        TestCommandQueryClientResource resource = assembler.objectBuilderFactory().newObjectBuilder(TestCommandQueryClientResource.class).use(client.getContext(), ref).newInstance();
        TestQuery query = assembler.valueBuilderFactory().newValue(TestQuery.class);
        TestQueryResult result = resource.someQuery2(query);
        System.out.println(result);
    }

    @Test
    public void testCommandWithoutParams() throws ResourceException
    {
        Client client = new Client(new Context(), Protocol.HTTP);

        Reference ref = new Reference("http://localhost:8040/resource");
        TestCommandQueryClientResource resource = assembler.objectBuilderFactory().newObjectBuilder(TestCommandQueryClientResource.class).use(client.getContext(), ref).newInstance();

        resource.someCommand();

        assertThat("command was invoked", TestCommandQueryResource.ok, is(true));
    }

    @Test
    public void testCommandWithParam() throws ResourceException
    {
        Client client = new Client(new Context(), Protocol.HTTP);

        Reference ref = new Reference("http://localhost:8040/resource");
        TestCommandQueryClientResource resource = assembler.objectBuilderFactory().newObjectBuilder(TestCommandQueryClientResource.class).use(client.getContext(), ref).newInstance();
        TestCommand command = assembler.valueBuilderFactory().newValue(TestCommand.class);
        resource.someCommand2(command);

        assertThat("command was invoked", TestCommandQueryResource.ok, is(true));
    }


    private Finder createServerResourceFinder(Class<? extends ServerResource> resource)
    {
        Finder finder = assembler.objectBuilderFactory().newObject(Finder.class);
        finder.setTargetClass(resource);
        return finder;
    }

}

class TestCommandQueryClientResource
        extends CommandQueryClientResource
{
    TestCommandQueryClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public TestQueryResult someQuery() throws ResourceException
    {
        return query("someQuery", TestQueryResult.class);
    }

    public TestQueryResult someQuery2(TestQuery query) throws ResourceException
    {
        return query("someQuery2", query, TestQueryResult.class);
    }

    public void someCommand() throws ResourceException
    {
        postCommand("someCommand");
    }

    public void someCommand2(TestCommand command) throws ResourceException
    {
        postCommand("someCommand2", command);
    }

}

class TestCommandQueryResource
        extends CommandQueryServerResource
{
    public static boolean ok = false;

    @Structure
    ValueBuilderFactory vbf;

    public TestQueryResult someQuery()
    {
        ok = true;
        return vbf.newValue(TestQueryResult.class);
    }

    public TestQueryResult someQuery2(TestQuery query)
    {
        ok = true;
        return vbf.newValue(TestQueryResult.class);
    }

    public void someCommand()
    {
        ok = true;
    }

    public void someCommand2(TestCommand command)
    {
        ok = true;
    }
}

interface TestQuery
        extends ValueComposite
{
    @UseDefaults
    Property<String> param1();

    @UseDefaults
    Property<Long> param2();
}

interface TestQueryResult
        extends ValueComposite
{
    @UseDefaults
    Property<String> param1();

    @UseDefaults
    Property<Long> param2();
}

interface TestCommand
        extends ValueComposite
{
    @UseDefaults
    Property<String> param1();

    @UseDefaults
    Property<Long> param2();
}