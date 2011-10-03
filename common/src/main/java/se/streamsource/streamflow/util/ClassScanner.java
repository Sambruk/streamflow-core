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

package se.streamsource.streamflow.util;

import org.qi4j.api.specification.Specification;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import static org.qi4j.api.util.Iterables.*;

/**
 * Scan classpath for classes that matches given criteria. Useful for automated assemblies with lots of similar classes
 */
public class ClassScanner
{
   /**
    * Get all classes from the package of the given class, and recursively in subpackages.
    * <p/>
    * This only works if the seed class is loaded from a file: URL. Jar files are possible as well.
    *
    * @param seedClass
    * @return
    */
   public static Iterable<Class> getClasses(final Class seedClass)
   {
      URL location = seedClass.getProtectionDomain().getCodeSource().getLocation();

      if (!location.getProtocol().equals("file"))
         throw new IllegalArgumentException("Can only enumerate classes from file system locations. URL is:" + location);

      // JDK: Location is directory, Tomcat: Location is actual class file
      String tmpFileName = null;
      try
      {
         tmpFileName = URLDecoder.decode(location.getPath(), "UTF-8");
         if( tmpFileName.endsWith( ".class" ))
         {
            tmpFileName = tmpFileName.substring( 0, tmpFileName.length() - (seedClass.getName().length() + 6) );
         }
      } catch (UnsupportedEncodingException e)
      {
         // Ignore
      }

      final File file = new File(tmpFileName);

      if (file.getName().endsWith(".jar"))
      {
         try
         {
            JarFile jarFile = new JarFile(file);
            Iterable<JarEntry> entries = Iterables.iterable(jarFile.entries());
            try
            {
               return Iterables.addAll(new ArrayList<Class>(), filter(new NonAbstractClass(),
                     map(new Function<JarEntry, Class>()
                     {
                        public Class map(JarEntry jarEntry)
                        {
                           String name = jarEntry.getName();
                           name = name.substring(0, name.length() - 6);
                           name = name.replace('/', '.');
                           try
                           {
                              return seedClass.getClassLoader().loadClass(name);
                           } catch (ClassNotFoundException e)
                           {
                              return null;
                           }
                        }

                     }
                           , filter(new Specification<JarEntry>()
                     {
                        public boolean satisfiedBy(JarEntry jarEntry)
                        {
                           return jarEntry.getName().endsWith(".class");
                        }
                     }, entries))));
            } finally
            {
               jarFile.close();
            }
         } catch (IOException e)
         {
            throw new IllegalArgumentException("Could not open jar file " + file, e);
         }
      } else
      {
         final File path = new File(file, seedClass.getPackage().getName().replace('.', File.separatorChar));
         Iterable<File> files = getFiles(path, new Specification<File>()
         {
            public boolean satisfiedBy(File file)
            {
               return file.getName().endsWith(".class");
            }
         });

         return filter(new NonAbstractClass(),
               map(new Function<File, Class>()
               {
                  public Class map(File f)
                  {
                     String fileName = f.getAbsolutePath().substring(file.toString().length() + 1);
                     fileName = fileName.replace(File.separatorChar, '.').substring(0, fileName.length() - 6);

                     try
                     {
                        return seedClass.getClassLoader().loadClass(fileName);
                     } catch (Throwable e)
                     {
                        return null;
                     }
                  }
               }, files));
      }
   }

   /**
    * Useful specification for filtering classes based on a regular expression matching the class names.
    * <p/>
    * Example: matches(".*Model") -> match only class names that end with Model
    * <p/>
    * Example:
    *
    * @param regex
    * @return
    */
   public static Specification<Class> matches(String regex)
   {
      final Pattern pattern = Pattern.compile(regex);

      return new Specification<Class>()
      {
         public boolean satisfiedBy(Class aClass)
         {
            return pattern.matcher(aClass.getName()).matches();
         }
      };
   }

   private static Iterable<File> getFiles(File directory, final Specification<File> filter)
   {
      return flatten(filter(filter, iterable(directory.listFiles())),
            flatten(map(new Function<File, Iterable<File>>()
            {
               public Iterable<File> map(File file)
               {
                  return getFiles(file, filter);
               }
            }, filter(new Specification<File>()
            {
               public boolean satisfiedBy(File file)
               {
                  return file.isDirectory();
               }
            }, iterable(directory.listFiles())))));
   }

   private static class NonAbstractClass
         implements Specification<Class>
   {
      public boolean satisfiedBy(Class item)
      {
         return item.isInterface() || !Modifier.isAbstract(item.getModifiers());
      }
   }
}
