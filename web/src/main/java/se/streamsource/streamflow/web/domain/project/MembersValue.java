/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.domain.project;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.List;

/**
 * JAVADOC
 */
@Mixins(MembersValue.MembersMixin.class)
public interface MembersValue
        extends ValueComposite
{
    @UseDefaults
    Property<List<MemberValue>> members();

    MemberValue getMemberValue(EntityReference participantRef);

    public abstract class MembersMixin
            implements MembersValue
    {
        @State
        Property<List<MemberValue>> members;

        public MemberValue getMemberValue(EntityReference participantRef)
        {
            for (MemberValue memberValue : members.get())
            {
                if (memberValue.participant().get().equals(participantRef))
                {
                    return memberValue;
                }
            }
            return null;
        }
    }
}