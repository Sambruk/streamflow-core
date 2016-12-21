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
package org.streamsource.streamflow.statistic.dto;

import java.util.List;
import java.util.Map;

/**
 * Statistic results.
 */
public class StatisticsResult
{
   private List<CaseCount> caseCountSummary;
   private List<CaseCount> caseCountByOuOwner;
   private List<CaseCount> caseCountByCaseType;
   private List<CaseCount> caseCountByTopOuOwner;
   private Map<String, List<CaseCount>> caseCountByLabelPerCaseType;

   public List<CaseCount> getCaseCountSummary()
   {
      return caseCountSummary;
   }

   public void setCaseCountSummary(List<CaseCount> caseCountSummary)
   {
      this.caseCountSummary = caseCountSummary;
   }

   public List<CaseCount> getCaseCountByTopOuOwner()
   {
      return caseCountByTopOuOwner;
   }

   public void setCaseCountByTopOuOwner(List<CaseCount> caseCountByTopOuOwner)
   {
      this.caseCountByTopOuOwner = caseCountByTopOuOwner;
   }

   public List<CaseCount> getCaseCountByOuOwner()
   {
      return caseCountByOuOwner;
   }

   public void setCaseCountByOuOwner(List<CaseCount> caseCountByOuOwner)
   {
      this.caseCountByOuOwner = caseCountByOuOwner;
   }

   public List<CaseCount> getCaseCountByCaseType()
   {
      return caseCountByCaseType;
   }

   public void setCaseCountByCaseType(List<CaseCount> caseCountByCaseType)
   {
      this.caseCountByCaseType = caseCountByCaseType;
   }

   public Map<String, List<CaseCount>> getCaseCountByLabelPerCaseType()
   {
      return caseCountByLabelPerCaseType;
   }

   public void setCaseCountByLabelPerCaseType(Map<String, List<CaseCount>> caseCountByLabelPerCaseType)
   {
      this.caseCountByLabelPerCaseType = caseCountByLabelPerCaseType;
   }
}
