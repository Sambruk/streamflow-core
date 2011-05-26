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

package se.streamsource.dci.restlet.client;

import org.hamcrest.*;
import org.junit.*;
import org.qi4j.api.common.*;
import org.qi4j.api.composite.*;
import org.qi4j.api.constraint.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.property.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.util.*;
import org.qi4j.api.value.*;
import org.qi4j.bootstrap.*;
import org.qi4j.spi.service.importer.*;
import org.qi4j.spi.structure.*;
import org.qi4j.test.*;
import org.restlet.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;
import org.restlet.service.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.qi4j.*;
import se.streamsource.dci.restlet.server.*;
import se.streamsource.dci.restlet.server.api.*;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.*;

import java.io.*;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.*;

/**
 * Test for CommandQueryClient
 */
public class CommandQueryClientTest
      extends AbstractQi4jTest
{
   static public Server server;
   public CommandQueryClient cqc;

   public static String command = null; // Commands will set this

   protected ApplicationModelSPI newApplication()
         throws AssemblyException
   {
      ApplicationAssembler assembler = new ApplicationAssembler()
      {
         public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
               throws AssemblyException
         {
            ApplicationAssembly assembly = applicationFactory.newApplicationAssembly( CommandQueryClientTest.this );
            assembly.setMetaInfo( new RoleInjectionProviderFactory() );
            assembly.setMetaInfo( new MetadataService() );
            return assembly;
         }
      };
      try
      {
         return qi4j.newApplicationModel( assembler );
      }
      catch (AssemblyException e)
      {
         assemblyException( e );
         return null;
      }
   }

   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.objects( RootContext.class, SubContext.class, SubContext2.class, RootResource.class, SubResource1.class );

      new ClientAssembler().assemble( module );

      module.values( TestQuery.class, TestResult.class, TestCommand.class );
      module.forMixin( TestQuery.class ).declareDefaults().abc().set( "def" );


      new ValueAssembler().assemble( module );
      new DCIAssembler().assemble( module );

      module.objects(NullCommandResult.class );
      module.importedServices(CommandResult.class).importedBy( NEW_OBJECT );
      module.objects(RootRestlet.class );

      module.importedServices( InteractionConstraintsService.class ).
            importedBy( NewObjectImporter.class ).
            visibleIn( Visibility.application );
      module.objects( InteractionConstraintsService.class );

      module.importedServices( MetadataService.class ).importedBy( NEW_OBJECT );
      module.objects( MetadataService.class );

      module.objects( DescribableContext.class );
      module.transients( TestComposite.class );
   }

   @Before
   public void startWebServer() throws Exception
   {

      server = new Server( Protocol.HTTP, 8888 );
      CommandQueryRestlet2 restlet = objectBuilderFactory.newObjectBuilder( CommandQueryRestlet2.class ).use( new org.restlet.Context() ).newInstance();
      server.setNext(restlet);
      server.start();

      Client client = new Client( Protocol.HTTP );
      Reference ref = new Reference( "http://localhost:8888/" );
      cqc = objectBuilderFactory.newObjectBuilder( CommandQueryClientFactory.class ).use(client, new NullResponseHandler() ).newInstance().newClient( ref );
   }

   @After
   public void stopWebServer() throws Exception
   {
      server.stop();
   }

   @Test
   public void testQueryWithValue()
   {
      TestResult result = cqc.query( "querywithvalue", valueBuilderFactory.newValueFromJSON( TestQuery.class, "{'abc':'foo'}" ), TestResult.class );

      assertThat(result.toJSON(), equalTo("{\"xyz\":\"bar\"}"));
   }

   @Test
   public void testQueryWithValueGetDefaults()
   {
      TestQuery result = cqc.query( "querywithvalue", TestQuery.class );

      assertThat( result.toJSON(), equalTo( "{\"abc\":\"def\"}" ) );
   }

   @Test
   public void testCommandWithValueGetDefaults()
   {
      TestCommand result = cqc.query( "commandwithvalue", TestCommand.class );

      assertThat( result.toJSON(), equalTo( "{\"abc\":\"\"}" ) );
   }

   @Test
   public void testQueryWithoutValue()
   {
      TestResult result = cqc.query( "querywithoutvalue", TestResult.class );

      assertThat( result.toJSON(), equalTo( "{\"xyz\":\"bar\"}" ) );
   }

   @Test
   public void testPostCommandWithWrongValue()
   {
      try
      {
         cqc.postCommand( "commandwithvalue", valueBuilderFactory.newValueFromJSON( TestCommand.class, "{'abc':'wrong'}" ) );
      } catch (ResourceException e)
      {
         assertThat( e.getStatus().getDescription(), equalTo( "Wrong argument" ) );
      }
   }

   @Test
   public void testPostCommandWithRightValue()
   {
      cqc.postCommand( "commandwithvalue", valueBuilderFactory.newValueFromJSON( TestCommand.class, "{'abc':'right'}" ) );
   }

   @Test
   public void testPutCommandWithRightValue()
   {
      cqc.putCommand( "idempotentcommandwithvalue", valueBuilderFactory.newValueFromJSON( TestCommand.class, "{'abc':'right'}" ) );
   }

   @Test
   public void testDelete()
   {
      cqc.delete();

      assertThat( command, equalTo( "delete" ) );
   }

   @Test
   public void testSubResourceQueryWithValue()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      TestResult result = cqc2.query( "querywithvalue", valueBuilderFactory.newValueFromJSON( TestQuery.class, "{'abc':'foo'}" ), TestResult.class );

      assertThat( result.toJSON(), equalTo( "{\"xyz\":\"bar\"}" ) );
   }

   @Test
   public void testInteractionValidation()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );

      LinkValue xyzLink;
      {
         ResourceValue result = cqc2.queryResource();
         xyzLink = Iterables.first( Iterables.filter( Links.withId( "xyz" ), result.commands().get() ) );
         assertThat( xyzLink, CoreMatchers.<Object>notNullValue());
         Form form = new Form();
         form.set( "valid", "false" );
         cqc2.postCommand( xyzLink.rel().get(), form.getWebRepresentation());
      }

      {
         ResourceValue result = cqc2.queryResource();
         LinkValue nullLink = Iterables.first( Iterables.filter( Links.withId( "xyz" ), result.commands().get() ) );
         assertThat( nullLink, CoreMatchers.<Object>nullValue());

      }

      try
      {
         cqc2.postCommand( xyzLink.rel().get(), new StringRepresentation("{valid:false}") );
         Assert.fail("ResourceException should have been thrown");
      } catch (ResourceException e)
      {
         // Ok
      }

      Form form = new Form();
      form.set( "valid", "true" );
      cqc2.postCommand( "notxyz", form.getWebRepresentation());
   }

   @Test
   public void testRootIndex()
   {
      ResourceValue result = cqc.queryResource();

      assertThat( result.toJSON(), equalTo( "{\"commands\":[{\"classes\":\"command\",\"href\":\"delete\",\"id\":\"delete\",\"rel\":\"delete\",\"text\":\"Delete\"},{\"classes\":\"command\",\"href\":\"commandwithvalue\",\"id\":\"commandwithvalue\",\"rel\":\"commandwithvalue\",\"text\":\"Command with value\"},{\"classes\":\"command\",\"href\":\"idempotentcommandwithvalue\",\"id\":\"idempotentcommandwithvalue\",\"rel\":\"idempotentcommandwithvalue\",\"text\":\"Idempotent command with value\"}],\"index\":null,\"queries\":[{\"classes\":\"query\",\"href\":\"querywithvalue\",\"id\":\"querywithvalue\",\"rel\":\"querywithvalue\",\"text\":\"Query with value\"},{\"classes\":\"query\",\"href\":\"querywithoutvalue\",\"id\":\"querywithoutvalue\",\"rel\":\"querywithoutvalue\",\"text\":\"Query without value\"}],\"resources\":[]}"));
   }

   @Test
   public void testSubResourceIndex()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      ResourceValue result = cqc2.queryResource();

      assertThat( result.toJSON(), equalTo( "{\"commands\":[{\"classes\":\"command\",\"href\":\"xyz\",\"id\":\"xyz\",\"rel\":\"xyz\",\"text\":\"Xyz\"},{\"classes\":\"command\",\"href\":\"commandwithrolerequirement\",\"id\":\"commandwithrolerequirement\",\"rel\":\"commandwithrolerequirement\",\"text\":\"Command with role requirement\"},{\"classes\":\"command\",\"href\":\"changedescription\",\"id\":\"changedescription\",\"rel\":\"changedescription\",\"text\":\"Change description\"}],\"index\":null,\"queries\":[{\"classes\":\"query\",\"href\":\"querywithvalue\",\"id\":\"querywithvalue\",\"rel\":\"querywithvalue\",\"text\":\"Query with value\"},{\"classes\":\"query\",\"href\":\"querywithrolerequirement\",\"id\":\"querywithrolerequirement\",\"rel\":\"querywithrolerequirement\",\"text\":\"Query with role requirement\"},{\"classes\":\"query\",\"href\":\"genericquery\",\"id\":\"genericquery\",\"rel\":\"genericquery\",\"text\":\"Generic query\"},{\"classes\":\"query\",\"href\":\"description\",\"id\":\"description\",\"rel\":\"description\",\"text\":\"Description\"}],\"resources\":[{\"classes\":\"resource\",\"href\":\"subresource1/\",\"id\":\"subresource1\",\"rel\":\"subresource1\",\"text\":\"Subresource 1\"},{\"classes\":\"resource\",\"href\":\"subresource2/\",\"id\":\"subresource2\",\"rel\":\"subresource2\",\"text\":\"Subresource 2\"}]}" ) );
   }

   @Test
   public void testSubResourceQueryWithRoleRequirement()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      TestResult result = cqc2.query( "querywithrolerequirement", valueBuilderFactory.newValueFromJSON( TestQuery.class, "{'abc':'foo'}" ), TestResult.class );

      assertThat( result.toJSON(), equalTo( "{\"xyz\":\"bar\"}" ) );
   }

   @Test
   public void testSubResourceGenericQuery()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      TestResult result = cqc2.query( "genericquery", valueBuilderFactory.newValueFromJSON( TestQuery.class, "{'abc':'foo'}" ), TestResult.class );

      assertThat( result.toJSON(), equalTo( "{\"xyz\":\"bar\"}" ) );
   }

   @Test
   public void testSubResourceCompositeCommandQuery()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      cqc2.postCommand( "changedescription", valueBuilderFactory.newValueFromJSON( StringValue.class, "{'string':'foo'}" ) );
      StringValue result = cqc2.query( "description", StringValue.class );

      assertThat( result.toJSON(), equalTo( "{\"string\":\"foo\"}" ) );
   }

   @Test
   public void testContext()
   {
      RoleMap.newCurrentRoleMap();
      RoleMap.current().set( transientBuilderFactory.newTransient(TestComposite.class ));

      DescribableContext context = objectBuilderFactory.newObject(DescribableContext.class);

      ValueBuilder<StringValue> vb = valueBuilderFactory.newValueBuilder( StringValue.class );
      vb.prototype().string().set( "Foo" );
      context.changeDescription( vb.newInstance() );

      assertThat(context.description().string().get(), equalTo("Foo"));
   }

   public interface TestQuery
         extends ValueComposite
   {
      @UseDefaults
      Property<String> abc();
   }

   public interface TestCommand
         extends ValueComposite
   {
      @UseDefaults
      Property<String> abc();
   }

   public interface TestResult
         extends ValueComposite
   {
      Property<String> xyz();
   }

   public static class RootRestlet
         extends CommandQueryRestlet2
   {
      @Override
      protected Uniform createRoot( Request request, Response response )
      {
         return module.objectBuilderFactory().newObjectBuilder( RootResource.class ).use(this).newInstance();
      }
   }

   public static class RootResource
         extends CommandQueryResource
         implements SubResources
   {
      private static TestComposite instance;


      public RootResource()
      {
         super( RootContext.class );
      }

      public TestResult querywithvalue( ) throws Throwable
      {
         return (TestResult) invoke( );
      }

      public TestResult querywithoutvalue( ) throws Throwable
      {
         return context(RootContext.class).queryWithoutValue();
      }

      public void commandwithvalue(  ) throws Throwable
      {
         invoke( );
      }

      public void resource( String currentSegment )
      {
         RoleMap roleMap = RoleMap.current();

         roleMap.set( new File( "" ) );

         if (instance == null)
            roleMap.set( instance = module.transientBuilderFactory().newTransient( TestComposite.class ) );
         else
            roleMap.set( instance );

         subResource( SubResource1.class );
      }
   }

   public static class SubResource1
         extends CommandQueryResource
   {
      public SubResource1()
      {
         super( SubContext.class, SubContext2.class, DescribableContext.class );
      }

      public void genericquery(  ) throws Throwable
      {
         result( invoke( ) );
      }

      public void querywithvalue( ) throws Throwable
      {
         result( invoke( ) );
      }

      public void querywithoutvalue() throws Throwable
      {
         result( invoke( ) );
      }

      public void commandwithvalue( ) throws Throwable
      {
         result( invoke(  ) );
      }

      @SubResource
      public void subresource1( )
      {
         subResource( SubResource1.class );
      }

      @SubResource
      public void subresource2(  )
      {
         subResource( SubResource1.class );
      }
   }

   public static class RootContext
      implements DeleteContext
   {
      private static int count = 0;

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      public TestResult queryWithValue( TestQuery query )
      {
         return vbf.newValueFromJSON( TestResult.class, "{'xyz':'bar'}" );
      }

      public TestResult queryWithoutValue()
      {
         return vbf.newValueFromJSON( TestResult.class, "{'xyz':'bar'}" );
      }

      public void commandWithValue( TestCommand command )
      {
         if (!command.abc().get().equals( "right" ))
            throw new IllegalArgumentException( "Wrong argument" );

         // Done
      }

      public void idempotentCommandWithValue( TestCommand command ) throws ConcurrentEntityModificationException
      {
         // On all but every third invocation, throw a concurrency exception
         // This is to test retries on the server-side
         count++;
         if (count%3 != 0)
         {
            uowf.currentUnitOfWork().addUnitOfWorkCallback( new UnitOfWorkCallback()
            {
               public void beforeCompletion() throws UnitOfWorkCompletionException
               {
                  throw new ConcurrentEntityModificationException( Collections.<EntityComposite>emptyList());
               }

               public void afterCompletion( UnitOfWorkStatus status )
               {
               }
            });
         }

         if (!command.abc().get().equals( "right" ))
            throw new IllegalArgumentException( "Wrong argument" );

         // Done
      }

      public void delete() throws ResourceException, IOException
      {
         // Ok!
         command = "delete";
      }
   }

   public static class SubContext
      implements InteractionValidation
   {
      @Structure
      ValueBuilderFactory vbf;

      public TestResult queryWithValue( TestQuery query )
      {
         return vbf.newValueFromJSON( TestResult.class, "{'xyz':'bar'}" );
      }

      // Test interaction constraints

      @RequiresRoles(File.class)
      public TestResult queryWithRoleRequirement( TestQuery query )
      {
         return vbf.newValueFromJSON( TestResult.class, "{'xyz':'bar'}" );
      }

      @RequiresRoles(File.class)
      public void commandWithRoleRequirement()
      {
      }

      // Interaction validation
      private static boolean xyzValid = true;

      @RequiresValid("xyz")
      public void xyz(@Name("valid") boolean valid)
      {
         xyzValid = valid;
      }

      @RequiresValid("notxyz")
      public void notxyz(@Name("valid") boolean valid)
      {
         xyzValid = valid;
      }

      public boolean isValid( String name )
      {
         if (name.equals("xyz"))
            return xyzValid;
         else if (name.equals( "notxyz" ))
            return !xyzValid;
         else
            return false;
      }
   }

   public static class SubContext2
   {
      @Structure
      ValueBuilderFactory vbf;

      public TestResult genericQuery( TestQuery query )
      {
         return vbf.newValueFromJSON( TestResult.class, "{'xyz':'bar'}" );
      }
   }

   public static class DescribableContext
   {
      @Structure
      ValueBuilderFactory vbf;

      @Role
      Describable describable;

      @Role
      DescribableData describableData;

      public StringValue description()
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( se.streamsource.dci.value.StringValue.class );
         builder.prototype().string().set( describableData.description().get() );
         return builder.newInstance();
      }

      public void changeDescription( StringValue newDesc )
      {
         describable.changeDescription( newDesc.string().get() );
      }

      @Mixins(Describable.Mixin.class)
      public interface Describable
      {
         void changeDescription( String newDesc );

         class Mixin
               implements Describable
         {
            @This
            DescribableData data;

            public void changeDescription( String newDesc )
            {
               data.description().set( newDesc );
            }
         }
      }
   }

   public interface DescribableData
   {
      @UseDefaults
      Property<String> description();
   }

   public interface TestComposite
         extends TransientComposite, DescribableData, DescribableContext.Describable
   {

   }
}
