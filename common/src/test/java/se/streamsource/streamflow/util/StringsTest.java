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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test of Strings utility
 */
public class StringsTest
{
    @Test
    public void testEmpty()
    {
        Assert.assertThat( Strings.empty( null ), CoreMatchers.equalTo( true ) );
        Assert.assertThat( Strings.empty( "" ), CoreMatchers.equalTo( true ) );
        Assert.assertThat( Strings.empty( " " ), CoreMatchers.equalTo( true ) );
        Assert.assertThat( Strings.empty( "X " ), CoreMatchers.equalTo( false ) );
        Assert.assertThat( Strings.empty( "X" ), CoreMatchers.equalTo( false ) );
    }

    @Test
    public void testHumanReadable()
    {
        Assert.assertThat( Strings.humanReadable( "changeDescription" ), CoreMatchers.equalTo( "Change description" ) );
        Assert.assertThat( Strings.humanReadable( "ON_HOLD" ), CoreMatchers.equalTo( "On hold" ) );
        Assert.assertThat( Strings.humanReadable( "doStuffNow" ), CoreMatchers.equalTo( "Do stuff now" ) );
        Assert.assertThat( Strings.humanReadable( "DoStuffNow" ), CoreMatchers.equalTo( "Do stuff now" ) );
        Assert.assertThat( Strings.humanReadable( "URLtest" ), CoreMatchers.equalTo( "URL test" ) );
    }
}
