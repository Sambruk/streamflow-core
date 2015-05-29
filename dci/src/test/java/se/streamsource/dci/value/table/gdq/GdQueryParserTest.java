package se.streamsource.dci.value.table.gdq;

import static org.junit.Assert.*;

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
      assertNotNull(q.options);
      assertEquals(0, q.options.size());
   }

   @Test
   public void parseSelect() {
      GdQuery q = GdQueryParser.parse("select *");
      assertNotNull(q);
      assertEquals("*", q.select);
   }

   @Test
   public void parseSelectMultiple() {
      GdQuery q = GdQueryParser.parse("select foo  bar");
      assertNotNull(q);
      assertEquals("foo bar", q.select);
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

   @Test(expected = GdQueryParseException.class)
   public void parseEmptyWhere() {
      GdQueryParser.parse("where");
   }

   @Test(expected = GdQueryParseException.class)
   public void parseEmptyWhere2() {
      GdQueryParser.parse("where limit 10");
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
   public void parseLongQuery() {
      GdQuery q = GdQueryParser.parse("select * where foo order by bar, baz desc limit 10 offset 20 options qux");
      assertNotNull(q);
      assertEquals("*", q.select);
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
      assertEquals(1, q.options.size());
      assertEquals("qux", q.options.get(0));
   }

}
