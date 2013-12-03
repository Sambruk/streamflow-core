/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.web.infrastructure.index;

import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeSource;

import java.io.Reader;

/**
 * JAVADOC
 */
public class SingleTokenTokenizer
      extends CharTokenizer
{
   public SingleTokenTokenizer( Version matchVersion, Reader in )
   {
      super( matchVersion, in );
   }

   public SingleTokenTokenizer( Version matchVersion, AttributeSource source, Reader in )
   {
      super( matchVersion, source, in );
   }

   public SingleTokenTokenizer( Version matchVersion, AttributeSource.AttributeFactory factory, Reader in )
   {
      super( matchVersion, factory, in );
   }


   @Override
   protected boolean isTokenChar( int c )
   {
      return true;
   }
}
