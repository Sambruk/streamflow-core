/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.search;

import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Iterator;

public class SearchTerms
{
   private static Map<Locale, List<String>> i18nSearchTerms = new HashMap<Locale, List<String>>();
   private static Locale sv_SE_gov = new Locale( "sv", "SE", "gov" );
   private static Locale sv_SE = new Locale( "sv", "SE" );
   //createdOn, taskType, project, label, assignedTo, description, note, name, contactId, phoneNumber, emailAddress.
   private static List<String> searchTerms_sv_SE =
         Arrays.asList( "skapad:", "ärendetyp:", "projekt:", "etikett:", "tilldelad:", "namn:", "kontaktid:", "telefon:", "email:" );
   private static List<String> searchTerms_sv_SE_gov =
         Arrays.asList( "skapad:", "ärendetyp:", "funktion:", "etikett:", "tilldelad:", "namn:", "personnr:", "telefon:", "email:" );
   private static List<String> searchTerms_en_GB =
         Arrays.asList( "created:", "tasktype:", "project:", "label:", "assigned:", "name:", "contactid:", "phone:", "email:" );
   private static List<String> searchTerms =
         Arrays.asList( "createdOn:", "taskType:", "project:", "label:", "assignedTo:", "name:", "contactId:", "phoneNumber:", "emailAddress:" );

   static
   {
      i18nSearchTerms.put( sv_SE_gov, searchTerms_sv_SE_gov );
      i18nSearchTerms.put( sv_SE, searchTerms_sv_SE );
      i18nSearchTerms.put( Locale.UK, searchTerms_en_GB );
   }

   public static String translate( String search )
   {
      String translation = search;
      List<String> usedSearchTermsLang = i18nSearchTerms.get( Locale.getDefault() );
      for (Iterator<String> stringIterator = usedSearchTermsLang.iterator(); stringIterator.hasNext();)
      {
         String term = stringIterator.next();
         if (translation.contains( term ))
         {
            translation = translation.replace( term, searchTerms.get( usedSearchTermsLang.indexOf( term ) ) );
         }
      }
      return translation;
   }
}
