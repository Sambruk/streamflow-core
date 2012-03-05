/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.util;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.restlet.representation.Representation;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.dialog.DialogService;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

/**
 * A task for downloading and opening of attachments in streamflow.
 */
public class OpenAttachmentTask extends Task<File, Void>
{
   @Service
   DialogService dialogs;
   
   private final String relativePath;
   private Component view;
   private Downloadable download;
   private String fileName;

   public OpenAttachmentTask(String fileName, String relativePath, Component view, Downloadable download )
   {
      super( Application.getInstance());
      this.fileName = fileName;
      this.relativePath = relativePath;
      this.view = view;
      this.download = download;

      setUserCanCancel(false);
   }

   @Override
   protected File doInBackground() throws Exception
   {
      setMessage(getResourceMap().getString("description"));

      String[] fileNameParts = fileName.split("\\.");
      Representation representation = download.download(relativePath);

      File file = File.createTempFile(fileNameParts[0] + "_", "." + fileNameParts[1]);

      Inputs.byteBuffer( representation.getStream(), 8192 ).transferTo( Outputs.byteBuffer( file ));

      return file;
   }

   @Override
   protected void succeeded(File file)
   {
      // Open file
      Desktop desktop = Desktop.getDesktop();
      try
      {
         desktop.edit(file);
      } catch (IOException e)
      {
         try
         {
            desktop.open(file);
         } catch (IOException e1)
         {
            dialogs.showMessageDialog(view, i18n.text( WorkspaceResources.could_not_open_attachment), "");
         }
      }
   }
}