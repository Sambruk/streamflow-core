package org.streamsource.streamflow.statistic.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.streamsource.streamflow.statistic.dto.SearchCriteria;
import org.streamsource.streamflow.statistic.service.StatisticService;
import org.streamsource.streamflow.statistic.service.StatisticServiceFactory;

/**
 * Created by IntelliJ IDEA.
 * User: arvidhuss
 * Date: 2/23/12
 * Time: 2:19 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class VariationController
{

   @RequestMapping(value = "variation")
   public ModelAndView variation( @RequestParam(required = false) String fromDate,
                              @RequestParam(required = false ) String toDate,
                              @RequestParam(required = false ) String caseTypeId )
   {

      SearchCriteria criteria = new SearchCriteria(fromDate, toDate, SearchCriteria.SearchPeriodicity.monthly.name() );

      StatisticService statistics = StatisticServiceFactory.getInstance( criteria );

      ModelAndView modelAndView = new ModelAndView( "variation" );
      modelAndView.addObject( "fromDate", criteria.getFormattedFromDate() );
      modelAndView.addObject( "toDate", criteria.getFormattedToDate() );
      modelAndView.addObject( "caseTypeId", caseTypeId );
      modelAndView.addObject( "casetypes", statistics.getCaseTypes() );
      modelAndView.addObject( "result",statistics.getVariationForCaseType( caseTypeId ) );
      return modelAndView;
   }
}
