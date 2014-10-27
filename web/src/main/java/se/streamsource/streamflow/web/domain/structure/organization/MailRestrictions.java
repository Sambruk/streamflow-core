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
package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.organization.MailRestrictionEntity;

/**
 *
 */
@Mixins( MailRestrictions.Mixin.class )
public interface MailRestrictions
{
    MailRestriction createMailRestriction( String mailAddress );

    void removeMailRestriction( MailRestriction mailRestriction );

    Iterable<MailRestriction> getMailRestrictions();


    interface Data
    {
        @Queryable(false)
        ManyAssociation<MailRestriction> mailRestrictions();
    }

    interface Events
    {
        MailRestriction createdMailRestriction( @Optional DomainEvent event, String identity );
        void removedMailRestriction( @Optional DomainEvent event, MailRestriction mailRestriction );
    }

    abstract class Mixin
        implements MailRestrictions,
            Data, Events
    {
        @Service
        IdentityGenerator idGen;

        @Structure
        Module module;

        @This
        Data state;

        public Iterable<MailRestriction> getMailRestrictions()
        {
            return state.mailRestrictions();
        }

        public MailRestriction createMailRestriction( String mailAdress )
        {
            MailRestriction mailRestriction = createdMailRestriction(null, idGen.generate(MailRestrictionEntity.class));
            mailRestriction.changeDescription( mailAdress );
            return mailRestriction;
        }

        public MailRestriction createdMailRestriction( DomainEvent event, String identity )
        {
            UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
            MailRestriction mailRestriction = uow.newEntity( MailRestriction.class, identity );
            state.mailRestrictions().add( state.mailRestrictions().count(), mailRestriction );
            return mailRestriction;
        }

        public void removeMailRestriction( MailRestriction mailRestriction )
        {
            if (state.mailRestrictions().contains( mailRestriction ))
            {
                removedMailRestriction(null, mailRestriction);
                mailRestriction.removeEntity();
            }
        }

        public void removedMailRestriction( DomainEvent event, MailRestriction mailRestriction )
        {
            state.mailRestrictions().remove( mailRestriction );
        }
    }
}
