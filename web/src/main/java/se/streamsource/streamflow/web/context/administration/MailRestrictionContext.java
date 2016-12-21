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
package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.streamflow.web.domain.structure.organization.MailRestriction;
import se.streamsource.streamflow.web.domain.structure.organization.MailRestrictions;

import static se.streamsource.dci.api.RoleMap.role;

/**
 *
 */
@Mixins( MailRestrictionContext.Mixin.class )
public interface MailRestrictionContext
    extends Context, DeleteContext
{
    abstract class Mixin
        implements MailRestrictionContext
    {
        public void delete()
        {
            MailRestrictions mailRestrictions = role(MailRestrictions.class);
            MailRestriction mailRestriction = role(MailRestriction.class);

            mailRestrictions.removeMailRestriction(mailRestriction);
        }
    }
}
