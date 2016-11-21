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
package se.streamsource.streamflow.web.context.administration.surface.emailaccesspoints;

import org.qi4j.api.constraint.Name;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.web.domain.structure.organization.EmailAccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.EmailAccessPoints;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * TODO
 */
public class EmailAccessPointsAdministrationContext
        implements IndexContext<Iterable<EmailAccessPoint>>
{
   public Iterable<EmailAccessPoint> index()
   {
      EmailAccessPoints.Data data = role(EmailAccessPoints.Data.class);
      return data.emailAccessPoints();
   }

   public void create(@Name("email") String email)
   {
      EmailAccessPoints eap = role(EmailAccessPoints.class);
      eap.createEmailAccessPoint(email);
   }
}
