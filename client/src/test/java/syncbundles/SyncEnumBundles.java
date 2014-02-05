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
package syncbundles;

/*
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

import org.qi4j.api.io.Input;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;
import org.qi4j.api.io.Transforms;
import org.qi4j.api.specification.Specification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * When executed, this tool will find all properties files that are ResourceBundles and try to locate
 * an Enum class that has the same name. If one is found, check all properties to see if they exist
 * in the Enum. If no, then remove them.
 * <p/>
 * Invoke without parameters to do a "dry run", without making changes. This will just log what changes
 * that should be done. If you add the parameter "fix", then the changes will be written back.
 */
public class SyncEnumBundles
{
   private static File base;
   private static Logger logger;
   private static boolean fix = false;

   public static void main(String[] args) throws IOException, ClassNotFoundException
   {
      if (args.length > 0 && args[0].equals("fix"))
         fix = true;

      base = new File(".");
      logger = LoggerFactory.getLogger(SyncEnumBundles.class);

      logger.info("Checking resources at:" + base.getAbsolutePath());
      if (fix)
         logger.info("Changes will be written to resource files");
      else
         logger.info("Changes will only be logged");

      check(base);
   }

   private static void check(File resources) throws IOException, ClassNotFoundException
   {
      for (File file : resources.listFiles())
      {
         if (file.isDirectory())
            check(file);
         else
         {
            if (file.getName().endsWith(".properties"))
            {
               // TODO I'm sure there's a fancy regex that could do this instead
               // Get a file on the form /src/main/resources/foo/bar/resources/Some_sv.properties
               // to the form foo.bar.Some
               String enumName = file.getPath();
               enumName = enumName.substring(enumName.indexOf("resources/") + "resources/".length());
               enumName = enumName.replace(File.separatorChar, '.');
               enumName = enumName.substring(0, enumName.indexOf(".properties"));
               enumName = enumName.replace(".resources", "");
               enumName = enumName.split("_")[0];

               final Class enumClass;
               try
               {
                  enumClass = SyncEnumBundles.class.getClassLoader().loadClass(enumName);
                  if (!enumClass.isEnum())
                     continue;
               } catch (ClassNotFoundException e)
               {
                  continue;
               }

               logger.info("Checking bundle at:" + file.getName() + ", classname:" + enumName);

               File output = File.createTempFile("fixed", ".properties");
               textInput(file).transferTo(Transforms.filter(new Specification<String>()
               {
                  public boolean satisfiedBy(String item)
                  {
                     if (!item.contains("="))
                        return true;

                     String[] property = item.split("=");

                     String name = property[0];

                     try
                     {
                        enumClass.getField(name);
                        return true;
                     } catch (NoSuchFieldException e)
                     {
                        logger.info("   Property " + name + " has been removed");
                        return false;
                     }
                  }
               }, text(output)));

               if (fix)
               {
                  if (output.renameTo(file))
                     logger.debug("Fixed " + file);
                  else
                     logger.warn("Could not overwrite fixed file " + file);
               } else
               {
                  if (!output.delete())
                     logger.warn("Could not delete temporary file");
               }
            }
         }
      }
   }

   public static Output<String, IOException> text(final File file)
   {
      return new Output<String, IOException>()
      {
         public <SenderThrowableType extends Throwable> void receiveFrom(Sender<? extends String, SenderThrowableType> sender) throws IOException, SenderThrowableType
         {
            OutputStream stream = new FileOutputStream(file);

            // If file should be gzipped, do that automatically
            if (file.getName().endsWith(".gz"))
               stream = new GZIPOutputStream(stream);

            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, "ISO-8859-1"));

            try
            {
               sender.sendTo(new Receiver<String, IOException>()
               {
                  public void receive(String item) throws IOException
                  {
                     writer.append(item).append('\n');
                  }
               });
               writer.close();
            } catch (IOException e)
            {
               // We failed writing - close and delete
               writer.close();
               file.delete();
            } catch (Throwable senderThrowableType)
            {
               // We failed writing - close and delete
               writer.close();
               file.delete();

               throw (SenderThrowableType) senderThrowableType;
            }
         }
      };
   }

   public static Input<String, IOException> textInput(final File source)
   {
      return new Input<String, IOException>()
      {
         public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super String, ReceiverThrowableType> output) throws IOException, ReceiverThrowableType
         {
            InputStream stream = new FileInputStream(source);

            // If file is gzipped, unzip it automatically
            if (source.getName().endsWith(".gz"))
               stream = new GZIPInputStream(stream);

            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "ISO-8859-1"));

            try
            {
               output.receiveFrom(new Sender<String, IOException>()
               {
                  public <ReceiverThrowableType extends Throwable> void sendTo(Receiver<? super String, ReceiverThrowableType> receiver) throws ReceiverThrowableType, IOException
                  {
                     String line;
                     while ((line = reader.readLine()) != null)
                     {
                        receiver.receive(line);
                     }
                  }
               });
            } finally
            {
               reader.close();
            }
         }
      };
   }
}
