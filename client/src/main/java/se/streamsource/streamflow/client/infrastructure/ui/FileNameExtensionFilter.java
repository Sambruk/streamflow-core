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

package se.streamsource.streamflow.client.infrastructure.ui;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FileNameExtensionFilter extends FileFilter
{
   String description = null;
   List<String> extensions = null;
   boolean showDirectories;

   public FileNameExtensionFilter( String aDescription, boolean showDirectories, String... extension )
   {
      description = aDescription;
      this.showDirectories = showDirectories;
      extensions = Arrays.asList( extension );
   }

   @Override
   public boolean accept( File f )
   {
      if( showDirectories && f.isDirectory())
      {
        return true;  
      }

      if (f.getName().lastIndexOf( '.' ) != -1)
      {
         String extension = f.getName().substring(
               f.getName().lastIndexOf( '.' ) + 1 );

         if (extensions.contains( extension ))
         {
            return true;
         } else
         {
            return false;
         }
      } else
      {
         return false;
      }
   }

   @Override
   public String getDescription()
   {
      return description;
   }

}
