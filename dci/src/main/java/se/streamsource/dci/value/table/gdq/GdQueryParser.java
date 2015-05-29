package se.streamsource.dci.value.table.gdq;

import java.util.ArrayList;
import java.util.List;

/** Parses a sort-of subset of google data query language.
 * See the test for examples.
 *
 */
public class GdQueryParser {

   static GdQuery parse(String s) {
      GdqTokenizer tokenizer = new GdqTokenizer(s);
      GdQuery result = new GdQuery();

      while (tokenizer.hasToken()) {
         if (tokenizer.tokenType() == GdqTokenType.KEYWORD_SELECT) {
            result.select = parseMultiWordClause(tokenizer);
         }
         else if (tokenizer.tokenType() == GdqTokenType.KEYWORD_WHERE) {
            result.where = parseMultiWordClause(tokenizer);
         }
         else if (tokenizer.tokenType() == GdqTokenType.KEYWORD_ORDER) {
            result.orderBy = parseOrderByClause(tokenizer);
         }
         else {
            throw new GdQueryParseException("Unexpected token: " + tokenizer.tokenStringValue());
         }
      }

      return result;
   }

   /** Parses a clause of the form <keyword> <word>+, eg.
    * "select foo bar baz".
    */
   private static String parseMultiWordClause(GdqTokenizer tokenizer) {
      String initialToken = tokenizer.tokenStringValue();
      tokenizer.consumeToken();

      if (!tokenizer.hasToken(GdqTokenType.WORD)) {
         throw new GdQueryParseException("Expected word in "+ initialToken +" clause");
      }

      StringBuilder result = new StringBuilder(tokenizer.tokenStringValue());
      tokenizer.consumeToken();
      while (tokenizer.hasToken(GdqTokenType.WORD)) {
         result.append(" ");
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

   private static String consumeExpectedToken(GdqTokenizer tokenizer, GdqTokenType expectedTokenType, String description) {
      if (tokenizer.tokenType() != expectedTokenType) {
         throw new GdQueryParseException("Expected " + description + ", but found "+ tokenizer.tokenStringValue());
      }
      String value = tokenizer.tokenStringValue();
      tokenizer.consumeToken();
      return value;
   }
}
