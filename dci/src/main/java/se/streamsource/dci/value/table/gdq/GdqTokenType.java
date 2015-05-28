package se.streamsource.dci.value.table.gdq;

enum GdqTokenType {
   WORD,
   COMMA(","),
   KEYWORD_SELECT("select"),
   KEYWORD_WHERE("where"),
   KEYWORD_ORDER("order"),
   KEYWORD_BY("by"),
   KEYWORD_GROUP("group"),
   KEYWORD_PIVOT("pivot"),
   KEYWORD_LABEL("label"),
   KEYWORD_FORMAT("format"),
   KEYWORD_OPTIONS("options"),
   KEYWORD_LIMIT("limit"),
   KEYWORD_OFFSET("offset");

   String stringValue;

   private GdqTokenType() {
   }

   private GdqTokenType(String s) {
      this.stringValue = s;
   }

   public static GdqTokenType forStringValue(String s) {
      for (GdqTokenType t: GdqTokenType.values()) {
         if (s.equals(t.stringValue)) {
            return t;
         }
      }

      return WORD;
   }
}