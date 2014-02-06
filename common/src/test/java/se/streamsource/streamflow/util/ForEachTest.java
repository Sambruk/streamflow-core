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
package se.streamsource.streamflow.util;

import org.junit.Test;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;

import static org.qi4j.api.specification.Specifications.in;

/**
 * TODO
 */
public class ForEachTest
{
   @Test
   public void testForEach()
   {
      Iterable<String> iterable = Iterables.iterable("A","B","C","D");

      ForEach.forEach(iterable).filter(in("B")).map(new Function<String, String>()
      {
         public String map(String s)
         {
            return s+s;
         }
      }).visit(new Visitor<String, RuntimeException>()
      {
         public boolean visit(String visited)
         {
            System.out.println(visited);
            return true;
         }
      });
   }
}
