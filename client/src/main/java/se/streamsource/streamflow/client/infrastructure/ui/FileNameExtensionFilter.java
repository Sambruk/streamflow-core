package se.streamsource.streamflow.client.infrastructure.ui;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FileNameExtensionFilter extends FileFilter
{
   String description = null;
   List<String> extensions = null;

   public FileNameExtensionFilter( String aDescription, String... extension )
   {
      description = aDescription;
      extensions = Arrays.asList( extension );
   }

   @Override
   public boolean accept( File f )
   {
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
