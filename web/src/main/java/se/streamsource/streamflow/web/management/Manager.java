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
package se.streamsource.streamflow.web.management;

import java.io.IOException;
import java.text.ParseException;

import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.web.application.statistics.StatisticsStoreException;

/**
 * Management methods for Streamflow. These operations are available
 * through JMX.
 */
public interface Manager
{
   public void reindex() throws Exception;

   public String exportDatabase( @Name("Compress") boolean compress ) throws IOException;

   public String importDatabase( @Name("Filename") String name ) throws IOException;

   public String exportEvents( @Name("Compress") boolean compress ) throws IOException;

   public String exportEventsRange( @Name("Compress") boolean compress, @Name("From, yyyyMMdd:HHmm") String fromDate, @Optional @Name("To, yyyyMMdd:HHmm") String toDate ) throws IOException, ParseException;

   // Backup

   public String backup() throws Exception;

   public String restore( @Name("Dbonly") boolean dbOnly ) throws Exception;

   public String databaseSize();

   public void refreshStatistics() throws StatisticsStoreException;

   public String performArchivalCheck();

   public void performArchival();

   public String interruptArchival();
   
   public void sendDueOnNotifications();

   public String importUserAndGroupsFromLdap();

   @Computed
   Property<Integer> failedLogins();

   @Immutable
   Property<String> version();
}
