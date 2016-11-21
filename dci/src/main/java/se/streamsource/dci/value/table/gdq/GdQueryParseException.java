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
package se.streamsource.dci.value.table.gdq;

public class GdQueryParseException extends IllegalArgumentException {

   private static final long serialVersionUID = 1L;

   public GdQueryParseException() {
   }

   public GdQueryParseException(String message) {
      super(message);
   }

   public GdQueryParseException(Throwable cause) {
      super(cause);
   }

   public GdQueryParseException(String message, Throwable cause) {
      super(message, cause);
   }
}
