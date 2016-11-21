/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.infrastructure.attachment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Random;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.util.Visitor;

/**
 * JAVADOC
 */
@Mixins(AttachmentStoreService.Mixin.class)
public interface AttachmentStoreService
   extends AttachmentStore, ServiceComposite
{
   public class Mixin
      implements AttachmentStore
   {
      Random random = new Random();

      @Service
      FileConfiguration fileConfig;

      public String storeAttachment( Input<ByteBuffer, IOException> input )
            throws IOException
      {
         // Create new id for file
         File file = createFile();

         // Write data to disk
         input.transferTo(Outputs.<Object>byteBuffer(file));
         return getId(file);
      }

      public Input<ByteBuffer, IOException> attachment(String id) throws FileNotFoundException
      {
         File file = getFile(id);

         if (!file.exists())
            throw new FileNotFoundException("Attachment for id "+id+" does not exist");

         return Inputs.byteBuffer(file, 4096);
      }

      public void attachment(String id, Visitor<InputStream, IOException> visitor) throws IOException
      {
         File file = getFile(id);

         if (!file.exists())
            throw new FileNotFoundException("Attachment for id "+id+" does not exist");

         BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file), 4096);
         try
         {
            visitor.visit(inputStream);
         } finally
         {
            inputStream.close();
         }
      }

      public Input<String, IOException> text(String id) throws FileNotFoundException
      {
         File file = getFile(id);

         if (!file.exists())
            throw new FileNotFoundException("Attachment for id "+id+" does not exist");

         return Inputs.text(file);
      }

      public void deleteAttachment( String id ) throws IOException
      {
         File file = getFile(id);

         if (!file.exists())
            throw new FileNotFoundException("Attachment for id "+id+" does not exist");

         file.delete();
      }

      public long getAttachmentSize(String id) throws FileNotFoundException
      {
         File file = getFile(id);

         if (!file.exists())
            throw new FileNotFoundException("Attachment for id "+id+" does not exist");

         return file.length();
      }

      private String getId(File file)
      {
         return file.getName().substring(0, file.getName().length()-4);
      }

      private File createFile()
      {
         DecimalFormat format = new DecimalFormat("000000000");
         File file;
         String id;
         do
         {
            id = format.format( Math.abs(random.nextLong()) );
            file = getFile(id);
         } while (file.exists()); // Ensure we're not reusing an id

         return file;
      }

      private File getFile( String id )
      {
         if (id.startsWith("store:"))
            id = id.substring("store:".length());

         // Ensure that directory for data exists
         File files = new File(fileConfig.dataDirectory(), "files");

         if (!files.exists())
            files.mkdirs();

         File fileDir = new File(files, id.substring( id.length()-3 ));

         if (!fileDir.exists())
            fileDir.mkdir();

         return new File(fileDir, id+".bin");
      }
   }
}
