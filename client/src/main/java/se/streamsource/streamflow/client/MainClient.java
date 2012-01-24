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

package se.streamsource.streamflow.client;

import org.apache.log4j.Logger;

import javax.swing.UnsupportedLookAndFeelException;
import java.util.Locale;

/**
 * Main class for client.
 */
public class MainClient
{
   public static void main( String[] args ) throws Exception
   {
      new MainClient().start( args );
   }

   public void start( String... args ) throws IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException, ClassNotFoundException
   {
      // Set system properties
//      UIManager.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel");
      System.setProperty( "com.apple.mrj.application.apple.menu.about.name", "Streamflow" );
      System.setProperty( "apple.laf.useScreenMenuBar", "true" );
      System.setProperty( "dock:name", "Streamflow" );

      Locale locale = Locale.getDefault();

      if (locale.getLanguage().equals("sv"))
         Locale.setDefault( new Locale( "sv", "SE", "gov" ) );
      else
         Locale.setDefault(Locale.ENGLISH);

      //Locale.setDefault( new Locale( "sv", "SE", "gov" ) );

      org.jdesktop.application.Application.launch( StreamflowApplication.class, args );

      Logger.getLogger( "status" ).setAdditivity( false );
      Logger.getLogger( "progress" ).setAdditivity( false );
   }

   public void stop()
   {
      org.jdesktop.application.Application.getInstance( StreamflowApplication.class ).exit();
   }
}
