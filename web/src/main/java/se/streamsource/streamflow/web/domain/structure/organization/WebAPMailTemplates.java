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
package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.mixin.Mixins;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Templates for emails sent out by Web Access Points.
 */
@Mixins(WebAPMailTemplates.Mixin.class)
public interface WebAPMailTemplates
   extends EmailTemplates
{

   class Mixin
      extends EmailTemplates.Mixin
   {
      @Override
      public void synchronizeTemplates()
      {
         // Synchronize defaults for emails
         ResourceBundle bundle = ResourceBundle.getBundle(WebAPMailTemplates.class.getName(), new Locale( "sv", "SE" ) );
         for (String key : bundle.keySet())
         {
            if (data.emailTemplates().get().get(key) == null)
               changeTemplate(key, bundle.getString(key));
         }
      }
   }
}
