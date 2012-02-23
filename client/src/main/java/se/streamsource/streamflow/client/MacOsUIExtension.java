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
package se.streamsource.streamflow.client;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;

import se.streamsource.streamflow.client.util.i18n;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

public class MacOsUIExtension
{
   private StreamflowApplication application;

   public MacOsUIExtension(StreamflowApplication application)
   {
      this.application = application;
   }

   public void attachMacUIExtension()
   {
      Application macApp = Application.getApplication();
      macApp.setDockIconImage(i18n.icon(Icons.sf_icon, 128).getImage());
      macApp.setAboutHandler(new AboutHandler()
      {
         public void handleAbout(AppEvent.AboutEvent aboutEvent)
         {
            application.showAbout();
         }
      });
      macApp.setQuitHandler(new QuitHandler()
      {
         public void handleQuitRequestWith(AppEvent.QuitEvent quitEvent, QuitResponse quitResponse)
         {
            application.shutdown();
            quitResponse.performQuit();
         }
      });
   }

   public void attachMacOpenFileExtension()
   {
      if (System.getProperty("os.name").startsWith("Mac"))
      {
         Application macApplication = Application.getApplication();
         macApplication.setOpenFileHandler(new OpenFilesHandler()
         {
            public void openFiles(AppEvent.OpenFilesEvent openFilesEvent)
            {
               application.openFile(openFilesEvent.getFiles().get(0));
            }
         });
      }
   }

   /**
    * Replace all "ctrl" keystrokes with "meta" (Apple command) keystrokes.
    */
   public void convertAccelerators()
   {
      ActionMap actions = application.getContext().getActionMap();
      Object[] keys = actions.allKeys();
      for (Object key : keys)
      {
         Action action = actions.get(key);
         KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
         Object ack = action.getValue(Action.ACTION_COMMAND_KEY);
         if (keyStroke != null && keyStroke.toString().contains("ctrl"))
         {
            keyStroke = KeyStroke.getKeyStroke(keyStroke.toString().replace("ctrl", "meta"));
            action.putValue(Action.ACCELERATOR_KEY, keyStroke);
         }
         String toolTip = (String) action.getValue(Action.SHORT_DESCRIPTION);
         if (toolTip != null && toolTip.contains("ctrl"))
         {
            toolTip = toolTip.replace("ctrl", "meta");
            action.putValue(Action.SHORT_DESCRIPTION, toolTip);
         }
      }
   }

   /**
    * Replace all "ctrl" keystrokes with "meta" (Apple command) keystrokes.
    *
    * @param actions
    */
   public static void convertAccelerators(ActionMap actions)
   {
      Object[] keys = actions.allKeys();
      for (Object key : keys)
      {
         Action action = actions.get(key);
         KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
         Object ack = action.getValue(Action.ACTION_COMMAND_KEY);
         if (keyStroke != null && keyStroke.toString().contains("ctrl"))
         {
            keyStroke = KeyStroke.getKeyStroke(keyStroke.toString().replace("ctrl", "meta"));
            action.putValue(Action.ACCELERATOR_KEY, keyStroke);
         }
         String toolTip = (String) action.getValue(Action.SHORT_DESCRIPTION);
         if (toolTip != null && toolTip.contains("ctrl"))
         {
            toolTip = toolTip.replace("ctrl", "meta");
            action.putValue(Action.SHORT_DESCRIPTION, toolTip);
         }
      }
   }
}
