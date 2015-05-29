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
         switch (tokenizer.tokenType()) {
         case KEYWORD_SELECT:
            result.select = parseMultiWordClause(tokenizer);
            break;
         case KEYWORD_WHERE:
            result.where = parseMultiWordClause(tokenizer);
            break;
         case KEYWORD_ORDER:
            result.orderBy = parseOrderByClause(tokenizer);
            break;
         case KEYWORD_LIMIT:
            result.limit = parseIntegerClause(tokenizer);
            break;
         case KEYWORD_OFFSET:
            result.offset = parseIntegerClause(tokenizer);
            break;
         case KEYWORD_OPTIONS:
            result.options = parseMultiWordClause(tokenizer);
            break;
         case KEYWORD_GROUP:
         case KEYWORD_PIVOT:
         case KEYWORD_FORMAT:
         case KEYWORD_LABEL:
            throw new GdQueryParseException("Not supported: " + tokenizer.tokenStringValue());
         default:
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
