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
