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
package se.streamsource.streamflow.infrastructure.attachment;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.util.Iterables;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStoreService;

/**
 * JAVADOC
 */
public class AttachmentStoreTest
   extends AbstractQi4jTest
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.layer().application().setName( getClass().getSimpleName() );
      module.services( FileConfiguration.class, AttachmentStoreService.class );
      module.objects(AttachmentStoreTest.class);
   }

   @Service
   AttachmentStore store;

   @Test
   public void testCreateGetRemoveAttachment() throws IOException
   {
      objectBuilderFactory.newObjectBuilder( AttachmentStoreTest.class ).injectTo( this );

      String string = "Hello World";

      File data = File.createTempFile( "test", "bin" );
      data.deleteOnExit();

      Inputs.iterable(Iterables.iterable(string)).transferTo(Outputs.text(data));

      String id = store.storeAttachment( Inputs.byteBuffer(data, 1024) );

      File data2 = File.createTempFile( "test2", "bin" );
      data.deleteOnExit();

      Input<ByteBuffer, IOException> attachment = store.attachment(id);

      attachment.transferTo(Outputs.<Object>byteBuffer(data2));

      final StringBuffer buf = new StringBuffer();
      Inputs.text(data2).transferTo(Outputs.withReceiver(new Receiver<String, RuntimeException>()
      {
         public void receive(String item) throws RuntimeException
         {
            buf.append(item);
         }
      }));

      Assert.assertEquals( string, buf.toString() );

      store.deleteAttachment( id );

      try
      {
         store.attachment( id );
      } catch (IOException e)
      {
         // Ok!
      }
   }
}
