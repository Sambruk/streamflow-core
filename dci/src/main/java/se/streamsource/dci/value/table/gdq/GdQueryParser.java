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

import java.util.ArrayList;
import java.util.List;

/** Parses a sort-of subset of google data query language.
 * See the test for examples.
 *
 */
public class GdQueryParser {

   public static GdQuery parse(String s) {
      GdqTokenizer tokenizer = new GdqTokenizer(s);
      GdQuery result = new GdQuery();

      if (tokenizer.hasToken(GdqTokenType.KEYWORD_SELECT)) {
         result.select = parseSelectClause(tokenizer);
      }
      if (tokenizer.hasToken(GdqTokenType.KEYWORD_WHERE)) {
         result.where = parseMultiWordClause(tokenizer);
      }
      if (tokenizer.hasToken(GdqTokenType.KEYWORD_ORDER)) {
         result.orderBy = parseOrderByClause(tokenizer);
      }
      if (tokenizer.hasToken(GdqTokenType.KEYWORD_LIMIT)) {
         result.limit = parseIntegerClause(tokenizer);
      }
      if (tokenizer.hasToken(GdqTokenType.KEYWORD_OFFSET)) {
         result.offset = parseIntegerClause(tokenizer);
      }
      if (tokenizer.hasToken(GdqTokenType.KEYWORD_OPTIONS)) {
         result.options = parseMultiWordClause(tokenizer);
      }
      if (tokenizer.hasToken(GdqTokenType.KEYWORD_GROUP)
          || tokenizer.hasToken(GdqTokenType.KEYWORD_PIVOT)
          || tokenizer.hasToken(GdqTokenType.KEYWORD_FORMAT)
          || tokenizer.hasToken(GdqTokenType.KEYWORD_LABEL)
          ) {
         throw new GdQueryParseException("Unsupported query clause: " + tokenizer.tokenStringValue());
      }
      if (tokenizer.hasToken()) {
         throw new GdQueryParseException("Unexpected token: " + tokenizer.tokenStringValue());
      }

      return result;
   }

   private static List<String> parseSelectClause(GdqTokenizer tokenizer) {
      consumeExpectedToken(tokenizer, GdqTokenType.KEYWORD_SELECT, "select");

      List<String> result = new ArrayList<String>();

      result.add(consumeExpectedToken(tokenizer, GdqTokenType.WORD, "column name in select clause"));
      while (tokenizer.hasToken(GdqTokenType.COMMA)) {
         tokenizer.consumeToken();
         result.add(consumeExpectedToken(tokenizer, GdqTokenType.WORD, "column name in select clause"));
      }

      return result;
   }

   /** Parses a clause of the form <keyword> <word>*, eg.
    * "select foo bar baz".
    */
   private static String parseMultiWordClause(GdqTokenizer tokenizer) {
      tokenizer.consumeToken();

      StringBuilder result = new StringBuilder();
      while (tokenizer.hasToken(GdqTokenType.WORD)) {
         if (result.length() > 0) {
            result.append(" ");
         }
         result.append(tokenizer.tokenStringValue());
         tokenizer.consumeToken();
      }

      return result.toString();
   }

   /** Parses an order by clause of the form
    * order by <word> (asc|desc)? ( , <word> (asc|desc)?)*
    */
   private static List<OrderByElement> parseOrderByClause(GdqTokenizer tokenizer) {
      consumeExpectedToken(tokenizer, GdqTokenType.KEYWORD_ORDER, "order");
      consumeExpectedToken(tokenizer, GdqTokenType.KEYWORD_BY, "by");

      List<OrderByElement> result = new ArrayList<OrderByElement>();

      result.add(parseOrderByElement(tokenizer));
      while (tokenizer.hasToken(GdqTokenType.COMMA)) {
         tokenizer.consumeToken();
         result.add(parseOrderByElement(tokenizer));
      }

      return result;
   }

   private static OrderByElement parseOrderByElement(GdqTokenizer tokenizer) {
      String name = consumeExpectedToken(tokenizer, GdqTokenType.WORD, "name in order by clause");
      OrderByDirection direction = OrderByDirection.UNDEFINED;
      if (tokenizer.hasToken() && tokenizer.tokenType() == GdqTokenType.WORD) {
         String directionString = tokenizer.tokenStringValue();
         if ("asc".equals(directionString)) {
            direction = OrderByDirection.ASCENDING;
         }
         else if ("desc".equals(directionString)) {
            direction = OrderByDirection.DESCENDING;
         }
         else {
            throw new GdQueryParseException("Invalid order by direction: " + directionString);
         }
         tokenizer.consumeToken();
      }
      return new OrderByElement(name, direction);
   }

   private static Integer parseIntegerClause(GdqTokenizer tokenizer) {
      String initialToken = tokenizer.tokenStringValue();
      tokenizer.consumeToken();

      if (tokenizer.hasToken(GdqTokenType.WORD)) {
         try {
            String s = tokenizer.tokenStringValue();
            tokenizer.consumeToken();
            return Integer.parseInt(s);
         }
         catch (NumberFormatException e) { }
      }

      throw new GdQueryParseException("Expected integer in "+ initialToken +" clause");
   }

   private static String consumeExpectedToken(GdqTokenizer tokenizer, GdqTokenType expectedTokenType, String description) {
      if (tokenizer.tokenType() != expectedTokenType) {
         throw new GdQueryParseException("Expected " + description + ", but found "+ tokenizer.tokenStringValue());
      }
      String value = tokenizer.tokenStringValue();
      tokenizer.consumeToken();
      return value;
   }
}
