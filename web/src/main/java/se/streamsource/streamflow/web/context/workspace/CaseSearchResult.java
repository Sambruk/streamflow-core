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
package se.streamsource.streamflow.web.context.workspace;

import se.streamsource.streamflow.web.domain.structure.caze.Case;

public class CaseSearchResult {

   private final Iterable<Case> result;
   private final Integer unlimitedCount;

   public CaseSearchResult(Iterable<Case> result, Integer unlimitedCount) {
      this.result = result;
      this.unlimitedCount = unlimitedCount;
   }

   public CaseSearchResult(Iterable<Case> result) {
      this(result, null);
   }

   public Iterable<Case> getResult() {
      return result;
   }

   public Integer getUnlimitedCount() {
      return unlimitedCount;
   }
}
