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

package se.streamsource.streamflow.infrastructure.attachment;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStoreService;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
      FileOutputStream out = new FileOutputStream(data);
      out.write( string.getBytes() );
      out.close();

      FileInputStream fin = new FileInputStream(data);
      String id = store.storeAttachment( fin );

      InputStream in = new BufferedInputStream(store.getAttachment( id ));

      byte[] buf = new byte[string.getBytes().length];
      in.read( buf );
      String string2 = new String(buf);

      Assert.assertEquals( string, string2 );

      store.deleteAttachment( id );

      try
      {
         store.getAttachment( id );
      } catch (IOException e)
      {
         // Ok!
      }
   }
}
