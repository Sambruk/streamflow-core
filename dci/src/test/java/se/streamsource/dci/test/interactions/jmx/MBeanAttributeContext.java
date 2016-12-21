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
package se.streamsource.dci.test.interactions.jmx;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.Value;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JAVADOC
 */
public class MBeanAttributeContext
      implements IndexContext<Value>
{
   @Structure
   Module module;

   public Value index()
   {
      try
      {
         Object attribute = RoleMap.role( MBeanServer.class ).getAttribute( RoleMap.role( ObjectName.class ), RoleMap.role( MBeanAttributeInfo.class ).getName() );

         if (attribute instanceof TabularDataSupport)
         {
            TabularDataSupport table = (TabularDataSupport) attribute;
            ValueBuilder<TabularDataValue> builder = module.valueBuilderFactory().newValueBuilder( TabularDataValue.class );
            Set<Map.Entry<Object, Object>> entries = table.entrySet();
            List<List<String>> cells = builder.prototype().cells().get();
            for (Map.Entry<Object, Object> entry : entries)
            {
               CompositeDataSupport cds = (CompositeDataSupport) entry.getValue();
               String key = cds.get( "key" ).toString();
               String value = cds.get( "value" ).toString();

               List<String> row = new ArrayList<String>();
               row.add( key );
               row.add( value );
               cells.add( row );
            }
            return builder.newInstance();
         } else
         {
            ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
            builder.prototype().string().set( attribute.toString() );
            return builder.newInstance();
         }
      } catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

   public void update( @Name("value") String newValue ) throws InstanceNotFoundException, InvalidAttributeValueException, ReflectionException, AttributeNotFoundException, MBeanException
   {
      Attribute attribute = new Attribute( RoleMap.role( MBeanAttributeInfo.class ).getName(), newValue );
      RoleMap.role( MBeanServer.class ).setAttribute( RoleMap.role( ObjectName.class ), attribute );
   }
}