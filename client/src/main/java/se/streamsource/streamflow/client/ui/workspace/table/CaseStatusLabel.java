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
package se.streamsource.streamflow.client.ui.workspace.table;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseResources;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.util.Strings;

/**
 * JAVADOC
 */
public class CaseStatusLabel
   extends JLabel
{
   public CaseStatusLabel()
   {
      setHorizontalAlignment( SwingConstants.CENTER );
   }

   public void setStatus(CaseStates status, String resolution)
   {
      boolean withResolution = !Strings.empty( resolution );
      String iconName = withResolution ? "case_status_withresolution_" + status.toString().toLowerCase() + "_icon"
            : "case_status_" + status.toString().toLowerCase() + "_icon";

      setIcon( i18n.icon( CaseResources.valueOf( iconName ), i18n.ICON_16 ) );

      setName( i18n.text( CaseResources.valueOf("case_status_"+status.toString().toLowerCase()+"_text" ) ));
      setToolTipText( i18n.text( CaseResources.valueOf("case_status_"+status.toString().toLowerCase()+"_text" ) ) );
   }
}
