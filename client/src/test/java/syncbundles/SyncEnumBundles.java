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

import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.io.Transforms;
import org.qi4j.api.specification.Specification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * When executed, this tool will find all properties files that are ResourceBundles and try to locate
 * an Enum class that has the same name. If one is found, check all properties to see if they exist
 * in the Enum. If no, then remove them.
 *
 * Invoke without parameters to do a "dry run", without making changes. This will just log what changes
 * that should be done. If you add the parameter "fix", then the changes will be written back.
 */
public class SyncEnumBundles
{
   private static File base;
   private static Logger logger;
   private static boolean fix = false;

   public static void main( String[] args ) throws IOException, ClassNotFoundException
   {
      if (args.length > 0 && args[0].equals("fix"))
         fix = true;

      base = new File(".");
      logger = LoggerFactory.getLogger( SyncEnumBundles.class );

      logger.info( "Checking resources at:"+ base.getAbsolutePath());
      if (fix)
         logger.info("Changes will be written to resource files");
      else
         logger.info("Changes will only be logged");

      check( base );
   }

   private static void check( File resources ) throws IOException, ClassNotFoundException
   {
      for (File file : resources.listFiles())
      {
         if (file.isDirectory())
            check(file);
         else
         {
            if (file.getName().endsWith( ".properties" ))
            {
               // TODO I'm sure there's a fancy regex that could do this instead
               // Get a file on the form /src/main/resources/foo/bar/resources/Some_sv.properties
               // to the form foo.bar.Some
               String enumName = file.getPath();
               enumName = enumName.substring(enumName.indexOf( "resources/" )+"resources/".length());
               enumName = enumName.replace( File.separatorChar, '.' );
               enumName = enumName.substring( 0, enumName.indexOf(".properties" ));
               enumName = enumName.replace(".resources", "");
               enumName = enumName.split( "_" )[0];

               final Class enumClass;
               try
               {
                  enumClass = SyncEnumBundles.class.getClassLoader().loadClass( enumName );
                  if (!enumClass.isEnum())
                     continue;
               } catch (ClassNotFoundException e)
               {
                  continue;
               }

               logger.info("Checking bundle at:"+file.getName()+", classname:"+enumName);

               File output = File.createTempFile( "fixed", ".properties" );
               Inputs.text( file ).transferTo( Transforms.filter(new Specification<String>()
               {
                  public boolean satisfiedBy( String item )
                  {
                     if (!item.contains( "=" ))
                        return true;

                     String[] property = item.split( "=" );

                     String name = property[0];

                     try
                     {
                        enumClass.getField( name );
                        return true;
                     } catch (NoSuchFieldException e)
                     {
                        logger.info( "   Property "+name+" has been removed" );
                        return false;
                     }
                  }
               }, Outputs.text( output )));

               if (fix)
               {
                  if (output.renameTo( file ))
                     logger.debug( "Fixed "+file );
                  else
                     logger.warn("Could not overwrite fixed file "+file);
               } else
               {
                  if (!output.delete())
                     logger.warn("Could not delete temporary file");
               }
            }
         }
      }
   }
}
