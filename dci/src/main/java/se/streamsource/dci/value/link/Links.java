/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.dci.value.link;

import org.qi4j.api.specification.Specification;
import org.qi4j.api.util.Function;

/**
 * Helper methods for links
 */
public final class Links
{
   public static Specification<LinkValue> withId(final String id)
   {
      return new Specification<LinkValue>()
      {
         public boolean satisfiedBy(LinkValue linkValue)
         {
            return linkValue.id().get().equals(id);
         }
      };
   }

   public static Specification<LinkValue> withText(final String text)
   {
      return new Specification<LinkValue>()
      {
         public boolean satisfiedBy(LinkValue linkValue)
         {
            return linkValue.text().get().equals(text);
         }
      };
   }

   public static Specification<LinkValue> withRel(final String rel)
   {
      return new Specification<LinkValue>()
      {
         public boolean satisfiedBy(LinkValue linkValue)
         {
            return linkValue.rel().get().equals(rel);
         }
      };
   }

   public static Specification<LinkValue> withClass(final String clazz)
   {
      return new Specification<LinkValue>()
      {
         public boolean satisfiedBy(LinkValue linkValue)
         {
            return linkValue.classes().get().contains(clazz);
         }
      };
   }

   public static Function<LinkValue, String> toRel()
   {
      return new Function<LinkValue, String>()
      {
         public String map(LinkValue linkValue)
         {
            return linkValue.rel().get();
         }
      };
   }
}
