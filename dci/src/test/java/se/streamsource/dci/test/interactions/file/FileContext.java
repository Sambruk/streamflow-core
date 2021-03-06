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
package se.streamsource.dci.test.interactions.file;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.CreateContext;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinksBuilder;
import se.streamsource.dci.value.link.LinksValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * JAVADOC
 */
public class FileContext
      implements CreateContext<StringValue, File>, IndexContext<LinksValue>, DeleteContext
{
   @Structure
   Module module;

   public void rename( StringValue newName )
   {
      File file = RoleMap.role( File.class );
      File newFile = new File( file.getParentFile(), newName.string().get() );
      boolean worked = file.renameTo( newFile );
      System.out.println( worked );
   }

   @RequiresDirectory
   public File create( StringValue value )
   {
      try
      {
         File file = new File(RoleMap.role(File.class), value.string().get());
         file.createNewFile();
         return file;
      } catch (IOException e)
      {
         throw new ResourceException( e );
      }
   }

   @RequiresFile
   public StringValue lastModified()
   {
      ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
      builder.prototype().string().set( RoleMap.role( File.class ).lastModified() + "" );
      return builder.newInstance();
   }

   @RequiresFile
   public InputStream content() throws FileNotFoundException
   {
      File file = RoleMap.role( File.class );
      return new FileInputStream( file );
   }

   @RequiresDirectory
   public LinksValue index()
   {
      File file = RoleMap.role( File.class );

      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).rel( "page" );

      String[] fileList = file.list();
      if (fileList != null)
      {
         for (String fileName : fileList)
         {
            builder.addLink( fileName, fileName );
         }
      }

      return builder.newLinks();
   }

   @RequiresFile
   public void delete() throws ResourceException
   {
      RoleMap.role( File.class ).delete();
   }
}