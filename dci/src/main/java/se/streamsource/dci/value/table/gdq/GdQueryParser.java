package se.streamsource.dci.value.table.gdq;

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

      if (!tokenizer.hasToken() || tokenizer.tokenType() != GdqTokenType.WORD) {
         throw new GdQueryParseException("Expected word in "+ initialToken +" clause");
      }

      StringBuilder result = new StringBuilder(tokenizer.tokenStringValue());
      tokenizer.consumeToken();
      while (tokenizer.hasToken() && tokenizer.tokenType() == GdqTokenType.WORD) {
         result.append(" ");
         result.append(tokenizer.tokenStringValue());
         tokenizer.consumeToken();
      }

      return result.toString();
   }

   private static void consumeExpectedToken(GdqTokenizer tokenizer, GdqTokenType expectedTokenType) {
      if (tokenizer.tokenType() != expectedTokenType) {
         throw new GdQueryParseException("Found token " + tokenizer.tokenStringValue() + " but expected "+ expectedTokenType.stringValue);
      }
      tokenizer.consumeToken();
   }
}
