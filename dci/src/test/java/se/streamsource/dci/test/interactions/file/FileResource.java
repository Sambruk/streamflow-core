/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.dci.test.interactions.file;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.service.MetadataService;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * JAVADOC
 */
public class FileResource
   extends CommandQueryResource
   implements SubResources
{
   public FileResource( )
   {
      super( FileContext.class );
   }

   @RequiresFile
   public Representation content() throws FileNotFoundException
   {
      File file = RoleMap.role( File.class );

      MetadataService metadataService = (MetadataService) module.serviceFinder().findService( MetadataService.class ).get();
      String ext = file.getName().split( "\\." )[1];
      MediaType mediaType = metadataService.getMediaType( ext );
      return new InputRepresentation( context(FileContext.class).content(), mediaType );
   }

   public void resource( String segment ) throws ResourceException
   {
      File file = new File( RoleMap.role( File.class ), segment );

      if (!file.exists())
         throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND);

      RoleMap.current().set( file );
      subResource( FileResource.class );
   }
}