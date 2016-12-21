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

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.api.administration.form.SelectionFieldValue;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

import java.util.Map;

/**
 *
 */
@Mixins( WebAPReplacedSelectionFieldValues.Mixin.class )
public interface WebAPReplacedSelectionFieldValues
{
    void changeReplacementFieldValue( String id, SelectionFieldValue newValue );
    void removeReplacementFieldValue( String id );
    SelectionFieldValue getReplacementFieldValue( String id );

    boolean hasReplacements();

    interface Data
    {
        @UseDefaults
        Property<Map<String,SelectionFieldValue>> replacements();

        void changedReplacementFieldValue( @Optional DomainEvent event, String id, SelectionFieldValue newValue );
        void removedReplacementFieldValue( @Optional DomainEvent event, String id );
    }

    abstract class Mixin
        implements WebAPReplacedSelectionFieldValues, Data
    {
        @This
        Data data;

        public SelectionFieldValue getReplacementFieldValue( String id )
        {
            return data.replacements().get().get( id );
        }

        /**
         * Change replacement field value. If the selection field value values are empty remove the whole entry
         * from the map.
         * @param id - The field id.
         * @param newValue - The replacement field value.
         */
        public void changeReplacementFieldValue( String id, SelectionFieldValue newValue )
        {
            if( newValue.values().get().isEmpty() )
            {
                removeReplacementFieldValue( id );
            } else
            {
                changedReplacementFieldValue(null, id, newValue);
            }
        }

        public void changedReplacementFieldValue( @Optional DomainEvent event, String id, SelectionFieldValue newValue )
        {
            Map<String,SelectionFieldValue> map = data.replacements().get();
            if( map.containsKey( id ))
            {
                map.remove( id );
            }
            map.put( id, newValue );
            data.replacements().set(map);
        }

        public void removeReplacementFieldValue( String id )
        {
            if( data.replacements().get().containsKey( id ))
            {
                removedReplacementFieldValue(null, id);
            }
        }

        public void removedReplacementFieldValue( @Optional DomainEvent event, String id )
        {
            Map<String,SelectionFieldValue> map = data.replacements().get();
            map.remove(id);
            data.replacements().set(map);
        }

        public boolean hasReplacements()
        {
            return !data.replacements().get().keySet().isEmpty();
        }
    }
}
