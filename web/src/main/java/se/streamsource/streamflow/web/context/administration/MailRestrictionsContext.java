/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.CreateContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;

import se.streamsource.streamflow.web.domain.structure.organization.MailRestriction;
import se.streamsource.streamflow.web.domain.structure.organization.MailRestrictions;

import java.util.Locale;
import java.util.ResourceBundle;

import static se.streamsource.dci.api.RoleMap.role;

/**
 * JAVADOC
 */
@Mixins( MailRestrictionsContext.Mixin.class )
public interface MailRestrictionsContext
   extends Context, IndexContext<LinksValue>, CreateContext<String, MailRestriction>
{
    MailRestriction create( @Name("name") String mailAddress);

    abstract class Mixin implements MailRestrictionsContext
    {

        @Structure
        Module module;


        public LinksValue index()
        {
            return new LinksBuilder( module.valueBuilderFactory() ).rel( "mailrestriction" )
                    .addDescribables(RoleMap.role(MailRestrictions.class).getMailRestrictions()).newLinks();
        }

        public MailRestriction create( String mailAddress )
        {
            return role(MailRestrictions.class).createMailRestriction( mailAddress );
        }

    }

}
