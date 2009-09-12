/* Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

package se.streamsource.streamflow.client.infrastructure.ui;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import java.awt.*;
import java.net.URL;

@Mixins(JavaHelpService.JavaHelpServiceMixin.class)
public interface JavaHelpService
    extends ServiceComposite, Activatable
{
    HelpBroker getHelpBroker();

    void enableHelp(Component c, String helpID);

    abstract class JavaHelpServiceMixin
            implements JavaHelpService, Activatable
    {

        private HelpBroker hb;

        public void activate() throws Exception
        {
            // Help system
            String helpHS = "StreamFlowHelp.hs";
            ClassLoader cl = getClass().getClassLoader();
            HelpSet hs;
            try
            {
                URL hsURL = HelpSet.findHelpSet(cl, helpHS);
                hs = new HelpSet(null, hsURL);

                // Create a HelpBroker object:
                hb = hs.createHelpBroker();
                hb.setCurrentID("top");
                
            } catch (Exception ee)
            {
                // Say what the exception really is
                System.out.println("HelpSet " + ee.getMessage());
                System.out.println("HelpSet " + helpHS + " not found");
            }

        }

        public void passivate() throws Exception
        {

        }

        public HelpBroker getHelpBroker()
        {
            return hb;
        }

        public void enableHelp(Component c, String helpID)
        {
            hb.enableHelp(c, helpID, hb.getHelpSet());
            hb.enableHelpKey(c, helpID, hb.getHelpSet());
        }


    }
}
