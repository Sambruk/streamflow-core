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
package se.streamsource.dci.value.table.gdq;

import static org.junit.Assert.*;

import org.junit.Test;

import se.streamsource.dci.value.table.gdq.GdqTokenType;
import se.streamsource.dci.value.table.gdq.GdqTokenizer;

public class GdqTokenizerTest {

   @Test
   public void emptyStringHasNoTokens() {
      GdqTokenizer t = new GdqTokenizer("");
      assertFalse(t.hasToken());
   }

   @Test
   public void whiteStringHasNoTokens() {
      GdqTokenizer t = new GdqTokenizer(" \t\n");
      assertFalse(t.hasToken());
   }

   @Test
   public void selectIsRecognizedAsToken() {
      GdqTokenizer t = new GdqTokenizer("select");
      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.KEYWORD_SELECT, t.tokenType());
   }

   @Test
   public void wordIsRecognizedAsToken() {
      GdqTokenizer t = new GdqTokenizer("some_word");
      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.WORD, t.tokenType());
      assertEquals("some_word", t.tokenStringValue());
   }

   @Test
   public void tokenizeMultipleTokens() {
      GdqTokenizer t = new GdqTokenizer("select some_word order by foo, bar");

      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.KEYWORD_SELECT, t.tokenType());
      t.consumeToken();

      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.WORD, t.tokenType());
      assertEquals("some_word", t.tokenStringValue());
      t.consumeToken();

      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.KEYWORD_ORDER, t.tokenType());
      t.consumeToken();

      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.KEYWORD_BY, t.tokenType());
      t.consumeToken();

      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.WORD, t.tokenType());
      assertEquals("foo", t.tokenStringValue());
      t.consumeToken();

      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.COMMA, t.tokenType());
      t.consumeToken();

      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.WORD, t.tokenType());
      assertEquals("bar", t.tokenStringValue());
      t.consumeToken();

      assertFalse(t.hasToken());
   }

   @Test
   public void tokenizeMultipleTokensWithAlternativeSpacing() {
      GdqTokenizer t = new GdqTokenizer(" select   some_word order by foo ,bar   ");

      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.KEYWORD_SELECT, t.tokenType());
      t.consumeToken();

      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.WORD, t.tokenType());
      assertEquals("some_word", t.tokenStringValue());
      t.consumeToken();

      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.KEYWORD_ORDER, t.tokenType());
      t.consumeToken();

      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.KEYWORD_BY, t.tokenType());
      t.consumeToken();

      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.WORD, t.tokenType());
      assertEquals("foo", t.tokenStringValue());
      t.consumeToken();

      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.COMMA, t.tokenType());
      t.consumeToken();

      assertTrue(t.hasToken());
      assertEquals(GdqTokenType.WORD, t.tokenType());
      assertEquals("bar", t.tokenStringValue());
      t.consumeToken();

      assertFalse(t.hasToken());
   }
}
