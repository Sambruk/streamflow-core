/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.dci.test.context.file;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Application;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.service.MetadataService;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.ContextNotFoundException;
import se.streamsource.dci.context.DeleteContext;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.dci.value.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * JAVADOC
 */
@Mixins(FileContext.Mixin.class)
public interface FileContext
   extends Context, IndexContext<LinksValue>, SubContexts<FileContext>, DeleteContext
{
   void rename(StringValue newName);

   @RequiresDirectory
   void newfile() throws ResourceException;

   @RequiresFile
   StringValue lastModified();

   @RequiresFile
   Representation content() throws FileNotFoundException;

   abstract class Mixin
      extends ContextMixin
      implements FileContext
   {
      public void rename( StringValue newName )
      {
         File file = context.role( File.class );
         File newFile = new File( file.getParentFile(), newName.string().get() );
         boolean worked = file.renameTo( newFile );
         System.out.println(worked);
      }

      public void newfile() throws ResourceException
      {
         try
         {
            new File(context.role(File.class), "New file.txt").createNewFile();
         } catch (IOException e)
         {
            throw new ResourceException(e);
         }
      }

      public StringValue lastModified()
      {
         ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
         builder.prototype().string().set( context.role( File.class ).lastModified()+"" );
         return builder.newInstance();
      }

      public Representation content() throws FileNotFoundException
      {
         File file = context.role( File.class );

         MetadataService metadataService = context.role( Application.class ).getMetadataService();
         String ext = file.getName().split( "\\." )[1];
         MediaType mediaType = metadataService.getMediaType(ext );
         return new InputRepresentation(new FileInputStream(file), mediaType);
      }

      public LinksValue index()
      {
         File file = context.role( File.class );

         LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).rel( "page" );

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

      public void delete() throws ResourceException
      {
         context.role( File.class ).delete();
      }

      public FileContext context( String id )
      {
         File file = new File( context.role( File.class ), id );

         if (!file.exists())
            throw new ContextNotFoundException();

         context.playRoles( file );

         return subContext( FileContext.class );
      }
   }
}