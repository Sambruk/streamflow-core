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

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class GdQueryParserTest {

   @Test
   public void parseEmptyString() {
      GdQuery q = GdQueryParser.parse("");
      assertNotNull(q);
      assertNull(q.select);
      assertNull(q.where);
      assertNotNull(q.orderBy);
      assertEquals(0, q.orderBy.size());
      assertNull(q.limit);
      assertNull(q.offset);
      assertNull(q.options);
   }

   @Test
   public void parseSelect() {
      GdQuery q = GdQueryParser.parse("select *");
      assertNotNull(q);
      assertEquals(Arrays.asList("*"), q.select);
   }

   @Test
   public void parseSelectMultiple() {
      GdQuery q = GdQueryParser.parse("select foo,  bar");
      assertNotNull(q);
      assertEquals(Arrays.asList("foo", "bar"), q.select);
   }

   @Test(expected = GdQueryParseException.class)
   public void parseEmptySelect() {
      GdQueryParser.parse("select");
   }

   @Test(expected = GdQueryParseException.class)
   public void parseEmptySelect2() {
      GdQueryParser.parse("select where foo");
   }

   @Test
   public void parseWhere() {
      GdQuery q = GdQueryParser.parse("where foo");
      assertNotNull(q);
      assertEquals("foo", q.where);
   }

   @Test
   public void parseWhereMultiple() {
      GdQuery q = GdQueryParser.parse("where foo bar");
      assertNotNull(q);
      assertEquals("foo bar", q.where);
   }

   @Test
   public void parseEmptyWhere() {
      GdQuery q = GdQueryParser.parse("where");
      assertNotNull(q);
      assertEquals("", q.where);
   }

   @Test
   public void parseEmptyWhere2() {
      GdQuery q = GdQueryParser.parse("where limit 10");
      assertNotNull(q);
      assertEquals("", q.where);
   }

   @Test
   public void parseOrderBy() {
      GdQuery q = GdQueryParser.parse("order by foo");
      assertNotNull(q);
      assertEquals(1, q.orderBy.size());
      assertNotNull(q.orderBy.get(0));
      assertEquals("foo", q.orderBy.get(0).name);
      assertEquals(OrderByDirection.UNDEFINED, q.orderBy.get(0).direction);
   }

   @Test
   public void parseOrderByAscending() {
      GdQuery q = GdQueryParser.parse("order by foo asc");
      assertNotNull(q);
      assertEquals(1, q.orderBy.size());
      assertNotNull(q.orderBy.get(0));
      assertEquals("foo", q.orderBy.get(0).name);
      assertEquals(OrderByDirection.ASCENDING, q.orderBy.get(0).direction);
   }

   @Test
   public void parseOrderByDescending() {
      GdQuery q = GdQueryParser.parse("order by foo desc");
      assertNotNull(q);
      assertEquals(1, q.orderBy.size());
      assertNotNull(q.orderBy.get(0));
      assertEquals("foo", q.orderBy.get(0).name);
      assertEquals(OrderByDirection.DESCENDING, q.orderBy.get(0).direction);
   }

   @Test(expected = GdQueryParseException.class)
   public void parseEmptyOrderBy() {
      GdQueryParser.parse("order by");
   }

   @Test(expected = GdQueryParseException.class)
   public void parseEmptyOrderBy2() {
      GdQueryParser.parse("order by limit");
   }

   @Test(expected = GdQueryParseException.class)
   public void parseMalformedOrderBy() {
      GdQueryParser.parse("order foo asc");
   }

   @Test(expected = GdQueryParseException.class)
   public void parseMalformedOrderBy2() {
      GdQueryParser.parse("order by foo bar");
   }

   @Test
   public void parseLimit() {
      GdQuery q = GdQueryParser.parse("limit 10");
      assertNotNull(q);
      assertEquals(new Integer(10), q.limit);
   }

   @Test(expected = GdQueryParseException.class)
   public void parseInvalidLimit() {
      GdQueryParser.parse("limit invalid");
   }

   @Test(expected = GdQueryParseException.class)
   public void parseEmptyLimit() {
      GdQueryParser.parse("limit");
   }

   @Test(expected = GdQueryParseException.class)
   public void parseEmptyLimit2() {
      GdQueryParser.parse("limit offset 10");
   }

   @Test
   public void parseOffset() {
      GdQuery q = GdQueryParser.parse("offset 20");
      assertNotNull(q);
      assertEquals(new Integer(20), q.offset);
   }

   @Test(expected = GdQueryParseException.class)
   public void parseInvalidOffset() {
      GdQueryParser.parse("offset invalid");
   }

   @Test(expected = GdQueryParseException.class)
   public void parseEmptyOffset() {
      GdQueryParser.parse("offset");
   }

   @Test(expected = GdQueryParseException.class)
   public void parseEmptyOffset2() {
      GdQueryParser.parse("offset limit 10");
   }

   @Test
   public void parseOptions() {
      GdQuery q = GdQueryParser.parse("options foo");
      assertNotNull(q);
      assertEquals("foo", q.options);
   }

   @Test
   public void parseOptionsMultiple() {
      GdQuery q = GdQueryParser.parse("options foo  bar");
      assertNotNull(q);
      assertEquals("foo bar", q.options);
   }

   public void parseEmptyOptions() {
      GdQuery q = GdQueryParser.parse("options");
      assertNotNull(q);
      assertEquals("", q.options);
   }

   @Test
   public void parseLongQuery() {
      GdQuery q = GdQueryParser.parse("select * where foo order by bar, baz desc limit 10 offset 20 options qux");
      assertNotNull(q);
      assertEquals(Arrays.asList("*"), q.select);
      assertEquals("foo", q.where);
      assertNotNull(q.orderBy);
      assertEquals(2, q.orderBy.size());
      assertNotNull(q.orderBy.get(0));
      assertEquals("bar", q.orderBy.get(0).name);
      assertEquals(OrderByDirection.UNDEFINED, q.orderBy.get(0).direction);
      assertNotNull(q.orderBy.get(1));
      assertEquals("baz", q.orderBy.get(1).name);
      assertEquals(OrderByDirection.DESCENDING, q.orderBy.get(1).direction);
      assertEquals(new Integer(10), q.limit);
      assertEquals(new Integer(20), q.offset);
      assertNotNull(q.options);
      assertEquals("qux", q.options);
   }

   @Test(expected = GdQueryParseException.class)
   public void parseInvalidClauseOrder() {
      GdQueryParser.parse("where foo select bar");
   }

   @Test(expected = GdQueryParseException.class)
   public void groupByShouldBeUnsupported() {
      GdQueryParser.parse("group by foo");
   }

   @Test(expected = GdQueryParseException.class)
   public void pivotShouldBeUnsupported() {
      GdQueryParser.parse("pivot foo");
   }

   @Test(expected = GdQueryParseException.class)
   public void formatShouldBeUnsupported() {
      GdQueryParser.parse("format foo");
   }

   @Test(expected = GdQueryParseException.class)
   public void labelShouldBeUnsupported() {
      GdQueryParser.parse("label foo bar");
   }



}
