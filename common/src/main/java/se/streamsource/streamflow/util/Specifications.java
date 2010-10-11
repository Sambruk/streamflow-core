/*
 * Copyright 2009-2010 Streamsource AB
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

/**
 * Utility methods for common specifications
 */
public class Specifications
{
   public static <T> Specification<T> TRUE()
   {
      return new Specification<T>()
      {
         public boolean valid( T instance )
         {
            return true;
         }
      };
   }

   public static <T> Specification<T> not( final Specification<T> specification)
   {
      return new Specification<T>()
      {
         public boolean valid( T instance )
         {
            return !specification.valid( instance );
         }
      };
   }

   public static <T> Specification<T> and( final Specification<T>... specifications)
   {
      return new Specification<T>()
      {
         public boolean valid( T instance )
         {
            for (Specification<T> specification : specifications)
            {
               if (!specification.valid( instance ))
                  return false;
            }

            return true;
         }
      };
   }

   public static <T> Specification<T> or( final Specification<T>... specifications)
   {
      return new Specification<T>()
      {
         public boolean valid( T instance )
         {
            for (Specification<T> specification : specifications)
            {
               if (specification.valid( instance ))
                  return true;
            }

            return false;
         }
      };
   }
}
