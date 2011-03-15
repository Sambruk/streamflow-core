/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.infrastructure.attachment;

import org.apache.commons.io.IOUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Random;

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

      public String storeAttachment( InputStream in )
            throws IOException
      {
         // Create new id for file
         DecimalFormat format = new DecimalFormat("000000000");
         File file;
         String id;
         do
         {
            id = format.format( Math.abs(random.nextLong()) );
            file = getFile(id);
         } while (file.exists()); // Ensure we're not reusing an id

         // Write data to disk
         BufferedOutputStream out = null;
         try
         {
            out = new BufferedOutputStream(new FileOutputStream(file));
            IOUtils.copyLarge( new BufferedInputStream(in, 1024),  out);

            in.close();
            out.close();

            return id;
         } catch (IOException ex)
         {
            in.close();
            if (out != null)
               out.close();

            // Remove file if it was partially written
            file.delete();

            throw ex;
         }
      }

      public InputStream getAttachment( String id ) throws IOException
      {
         File file = getFile(id);

         if (!file.exists())
            throw new FileNotFoundException("Attachment for id "+id+" does not exist");

         return new BufferedInputStream(new FileInputStream(file));
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

      private File getFile( String id )
      {
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
