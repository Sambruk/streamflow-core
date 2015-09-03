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