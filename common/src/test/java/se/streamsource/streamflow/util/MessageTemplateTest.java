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
package se.streamsource.streamflow.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * TODO
 */
public class MessageTemplateTest
{
   @Test
   public void testTemplate()
   {
      Map<String, String> map = new HashMap<String, String>();
      map.put("foo", "bar");
      map.put("not", "definitely");
      assertThat(MessageTemplate.text("{null}This is {not} a test {foo}", map), equalTo("This is definitely a test bar"));
   }

   @Test
   public void testTemplateBuilder()
   {
      assertThat(MessageTemplate.text("{null}This is {not} a test {foo}").bind("foo", "bar").bind("not", "definitely").eval(), equalTo("This is definitely a test bar"));
   }
}
