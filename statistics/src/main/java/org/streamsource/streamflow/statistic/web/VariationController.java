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
package org.streamsource.streamflow.statistic.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.streamsource.streamflow.statistic.dto.SearchCriteria;
import org.streamsource.streamflow.statistic.service.StatisticService;
import org.streamsource.streamflow.statistic.service.StatisticServiceFactory;

/**
 * The case type variation controller.
 */
@Controller
public class VariationController
{
   @RequestMapping(value = "variation")
   public ModelAndView variation(@RequestParam(required = false) String fromDate,
         @RequestParam(required = false) String toDate, @RequestParam(required = false) String caseTypeId)
   {
      SearchCriteria criteria = new SearchCriteria( fromDate, toDate, SearchCriteria.SearchPeriodicity.monthly.name() );

      StatisticService statistics = StatisticServiceFactory.getInstance( criteria );

      ModelAndView modelAndView = new ModelAndView( "variation" );
      modelAndView.addObject( "fromDate", criteria.getFormattedFromDate() );
      modelAndView.addObject( "toDate", criteria.getFormattedToDate() );
      modelAndView.addObject( "caseTypeId", caseTypeId );
      modelAndView.addObject( "caseTypes", statistics.getCaseTypes() );
      modelAndView.addObject( "result", statistics.getVariationForCaseType( caseTypeId ) );

      return modelAndView;
   }
}
