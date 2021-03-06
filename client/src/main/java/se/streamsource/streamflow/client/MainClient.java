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

      if (locale.getLanguage().equals("en"))
         Locale.setDefault( Locale.ENGLISH );
      else
         Locale.setDefault( new Locale( "sv", "SE", "gov" ) );

      org.jdesktop.application.Application.launch( StreamflowApplication.class, args );

      Logger.getLogger( "status" ).setAdditivity( false );
      Logger.getLogger( "progress" ).setAdditivity( false );
   }

   public void stop()
   {
      org.jdesktop.application.Application.getInstance( StreamflowApplication.class ).exit();
   }
}
