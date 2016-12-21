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
package se.streamsource.streamflow.web.infrastructure.index;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeSource;

import java.io.Reader;
import java.util.Map;

/**
 * JAVADOC
 */
public class SingleTokenTokenizerFactory
      extends TokenizerFactory
{
    /**
     * Initialize this factory via a set of key-value pairs.
     */
    public SingleTokenTokenizerFactory(Map<String, String> args) {
        super(args);
    }

    @Override
    public Tokenizer create(AttributeSource.AttributeFactory factory, Reader input) {
        return new SingleTokenTokenizer( luceneMatchVersion, factory, input );
    }
}
