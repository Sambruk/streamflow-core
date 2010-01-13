/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client;

import org.apache.log4j.Logger;

import javax.swing.*;
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
      System.setProperty( "com.apple.mrj.application.apple.menu.about.name", "StreamFlow" );
      System.setProperty( "apple.laf.useScreenMenuBar", "true" );
      System.setProperty( "dock:name", "StreamFlow" );
      Locale.setDefault( new Locale( "sv", "SE", "gov" ) );
//        Locale.setDefault(Locale.ENGLISH);

      org.jdesktop.application.Application.launch( StreamFlowApplication.class, args );

      Logger.getLogger( "status" ).setAdditivity( false );
      Logger.getLogger( "progress" ).setAdditivity( false );
   }

   public void stop()
   {
      org.jdesktop.application.Application.getInstance( StreamFlowApplication.class ).exit();
   }
}
