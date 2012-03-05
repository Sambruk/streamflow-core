/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package org.streamsource.streamflow.statistic.dto;

import java.util.List;

/**
 * Statistic results.
 */
public class StatisticsResult
{
   private List<CaseCount> casecountSummary;
   private List<CaseCount> caseCountByOuOwner;
   private List<CaseCount> caseCountByCasetype;
   private List<CaseCount> caseCountByTopOuOwner;

   public List<CaseCount> getCasecountSummary()
   {
      return casecountSummary;
   }

   public void setCasecountSummary( List<CaseCount> casecountSummary )
   {
      this.casecountSummary = casecountSummary;
   }

   public List<CaseCount> getCaseCountByTopOuOwner()
   {
      return caseCountByTopOuOwner;
   }

   public void setCaseCountByTopOuOwner( List<CaseCount> caseCountByTopOuOwner )
   {
      this.caseCountByTopOuOwner = caseCountByTopOuOwner;
   }

   public List<CaseCount> getCaseCountByOuOwner()
   {
      return caseCountByOuOwner;
   }

   public void setCaseCountByOuOwner( List<CaseCount> caseCountByOuOwner )
   {
      this.caseCountByOuOwner = caseCountByOuOwner;
   }

   public List<CaseCount> getCaseCountByCasetype()
   {
      return caseCountByCasetype;
   }

   public void setCaseCountByCasetype( List<CaseCount> caseCountByCasetype )
   {
      this.caseCountByCasetype = caseCountByCasetype;
   }
}
