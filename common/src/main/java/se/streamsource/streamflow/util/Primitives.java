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

import org.qi4j.api.util.NullArgumentException;

/**
 * Helper to Qi4j 2.0 back port ElasticSearch
 *
 * courtesy of Paul Merlin
 */
public class Primitives {

    /**
     * Check if a non-null object is of any of the Primitive Value Types or an array of them.
     * <p>
     *     String, Boolean, Integer, Double, Float, Long, Byte, Short and Character and their Java primitive types
     *     counterparts are considered as Primitive Value Types.
     * </p>
     * <p>
     *     Date, BigInteger, BigDecimal and JodaTime types are not considered as Primitive Value Types.
     * </p>
     *
     * @return true if object is a primitive value or an array of primitive values
     * @throws IllegalArgumentException if object is null
     */
    public static boolean isPrimitiveValue( Object object )
    {
        NullArgumentException.validateNotNull("object", object);
        if( object instanceof String
                || object instanceof Character
                || object instanceof Boolean
                || object instanceof Integer
                || object instanceof Double
                || object instanceof Float
                || object instanceof Long
                || object instanceof Byte
                || object instanceof Short )
        {
            return true;
        }
        if( object.getClass().isArray() )
        {
            return isArrayOfPrimitiveValues( object );
        }
        return false;
    }

    private static boolean isArrayOfPrimitiveValues( Object array )
    {
        if( array instanceof String[]
                || array instanceof char[] || array instanceof Character[]
                || array instanceof boolean[] || array instanceof Boolean[]
                || array instanceof int[] || array instanceof Integer[]
                || array instanceof double[] || array instanceof Double[]
                || array instanceof float[] || array instanceof Float[]
                || array instanceof long[] || array instanceof Long[]
                || array instanceof byte[] || array instanceof Byte[]
                || array instanceof short[] || array instanceof Short[] )
        {
            return true;
        }
        return false;
    }
}
