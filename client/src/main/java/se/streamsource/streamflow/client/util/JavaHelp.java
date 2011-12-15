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

package se.streamsource.streamflow.client.util;


import java.awt.Component;
import java.net.URL;

import javax.help.HelpBroker;
import javax.help.HelpSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JavaHelp
{
   private HelpBroker hb;

   public JavaHelp()
   {
      final Logger logger = LoggerFactory.getLogger( getClass().getName() );
      // Help system
      String helpHS = "helpset.hs";
      ClassLoader cl = getClass().getClassLoader();
      HelpSet hs;
      try
      {
         URL hsURL = HelpSet.findHelpSet( cl, helpHS );
         hs = new HelpSet( null, hsURL );

         // Create a HelpBroker object:
         hb = hs.createHelpBroker();
         hb.setCurrentID( "intro1" );

      } catch (Exception ee)
      {
         // Say what the exception really is
         logger.warn( "HelpSet " + helpHS + " not found: " + ee.getMessage() );
      }
   }


   public void enableHelp( Component c, String helpID )
   {
      if (hb != null)
      {
         hb.enableHelp( c, helpID, hb.getHelpSet() );
         hb.enableHelpKey( c, helpID, hb.getHelpSet() );
      }
   }

   public void init()
   {
      if (hb != null)
      {
         hb.setCurrentID( "intro1" );
         hb.setViewDisplayed( true );
         if (!hb.isDisplayed())
            hb.setDisplayed( true );
      }
   }
}
