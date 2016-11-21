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

/**
 * TODO
 */
public class UpdateRule
{
   private UpdateOperation operation;

   protected String fromVersion;
   protected String toVersion;

   public UpdateRule(String fromVersion, String toVersion, UpdateOperation operation)
   {
      this.fromVersion = fromVersion;
      this.toVersion = toVersion;
      this.operation = operation;
   }

   public String fromVersion()
   {
      return fromVersion;
   }

   public String toVersion()
   {
      return toVersion;
   }

   public UpdateOperation getOperation()
   {
      return operation;
   }
}
