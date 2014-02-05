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

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.document.AbstractExcelView;
import org.streamsource.streamflow.statistic.dto.CaseCount;
import org.streamsource.streamflow.statistic.dto.Period;
import org.streamsource.streamflow.statistic.dto.SearchCriteria;
import org.streamsource.streamflow.statistic.dto.StatisticsResult;
import org.streamsource.streamflow.statistic.service.StatisticService;
import org.streamsource.streamflow.statistic.service.StatisticServiceFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The statistic controller class.
 */
@Controller
public class StatisticController
{
   public StatisticController()
   {
   }

   @RequestMapping(value = "count")
   public ModelAndView index(@RequestParam(required = false) String fromDate,
         @RequestParam(required = false) String toDate, @RequestParam(required = false) String periodicity)
   {

      SearchCriteria criteria = new SearchCriteria( fromDate, toDate, periodicity );

      StatisticService statistics = StatisticServiceFactory.getInstance( criteria );

      ModelAndView modelAndView = new ModelAndView( "count" );
      modelAndView.addObject( "fromDate", criteria.getFormattedFromDate() );
      modelAndView.addObject( "toDate", criteria.getFormattedToDate() );
      modelAndView.addObject( "periodicity", criteria.getPeriodicity().toString() );
      modelAndView.addObject( "periods", criteria.getPeriods() );
      modelAndView.addObject( "result", statistics.getStatistics() );

      return modelAndView;
   }

   @RequestMapping(value = "download")
   public ModelAndView download(@RequestParam(required = false) String fromDate,
         @RequestParam(required = false) String toDate, @RequestParam(required = false) String periodicity)
   {
      SearchCriteria criteria = new SearchCriteria( fromDate, toDate, periodicity );

      StatisticService statistics = StatisticServiceFactory.getInstance( criteria );

      Map<String, Object> model = new HashMap<String, Object>();
      model.put( "criteria", criteria );
      model.put( "statistics", statistics.getStatistics() );

      return new ModelAndView( new AbstractExcelView()
      {
         @Override
         protected void buildExcelDocument(Map<String, Object> model, HSSFWorkbook workbook,
               HttpServletRequest request, HttpServletResponse response) throws Exception
         {
            SearchCriteria criteria = (SearchCriteria) model.get( "criteria" );
            StatisticsResult statistics = (StatisticsResult) model.get( "statistics" );

            // create a summary sheet
            createWorkbookSheet( workbook, "Summering", criteria.getPeriods(), statistics.getCaseCountSummary() );

            // create top OU sheet
            createWorkbookSheet( workbook, "Huvudenhet", criteria.getPeriods(), statistics.getCaseCountByTopOuOwner() );

            createWorkbookSheet( workbook, "Ägare", criteria.getPeriods(), statistics.getCaseCountByOuOwner() );

            createWorkbookSheet( workbook, "Ärendetyp", criteria.getPeriods(), statistics.getCaseCountByCaseType() );
            
            createWorkbookSheet( workbook, "Ärendetyp med etiketter", criteria.getPeriods(), statistics.getCaseCountByCaseType(), statistics.getCaseCountByLabelPerCaseType());

            response.setHeader( "Content-Disposition",
                  "attachment; filename=\"" + "StreamflowStatistics_" + criteria.getFormattedFromDate() + "_"
                        + criteria.getFormattedToDate() + ".xls\"" );
         }

         private void createWorkbookSheet(HSSFWorkbook workbook, String name, String[] periods,
               List<CaseCount> caseCounts, Map<String, List<CaseCount>> caseCountsByLabel)
         {
            // create a sheet
            HSSFSheet summary = workbook.createSheet( name );

            HSSFRow header = summary.createRow( 0 );
            int count = 0;
            header.createCell( count++ ).setCellValue( "Ärendetyp" );
            header.createCell( count++ ).setCellValue( "Etikett" );
            header.createCell( count++ ).setCellValue( "Total" );

            for (String period : periods)
            {
               header.createCell( count++ ).setCellValue( period );
            }

            int rowNum = 1;
            
            for (CaseCount caseCount : caseCounts)
            {
               count = 0;
               HSSFRow row = summary.createRow( rowNum++ );
               row.createCell( count++ ).setCellValue( caseCount.getName() );
               row.createCell( count++ ).setCellValue( "" );
               row.createCell( count++ ).setCellValue( caseCount.getTotal() );

               for (Period period : caseCount.getValues())
               {
                  row.createCell( count++ ).setCellValue( period.getCount() );
               }
               
               if (caseCountsByLabel.get( caseCount.getName() ) != null) {
                  for (CaseCount labelCaseCount : caseCountsByLabel.get( caseCount.getName() )) {
                     count = 0;
                     HSSFRow newRow = summary.createRow( rowNum++ );
                     newRow.createCell( count++ ).setCellValue( "" );
                     newRow.createCell( count++ ).setCellValue( labelCaseCount.getName() );
                     newRow.createCell( count++ ).setCellValue( labelCaseCount.getTotal() );
   
                     for (Period period : labelCaseCount.getValues())
                     {
                        newRow.createCell( count++ ).setCellValue( period.getCount() );
                     }
                  }
               }
            }
         }
         
         private void createWorkbookSheet(HSSFWorkbook workbook, String name, String[] periods,
               List<CaseCount> caseCounts)
         {
            // create a sheet
            HSSFSheet summary = workbook.createSheet( name );

            HSSFRow header = summary.createRow( 0 );
            int count = 0;
            header.createCell( count++ ).setCellValue( "" );
            header.createCell( count++ ).setCellValue( "Total" );

            for (String period : periods)
            {
               header.createCell( count++ ).setCellValue( period );
            }

            int rowNum = 1;
            for (CaseCount caseCount : caseCounts)
            {
               count = 0;
               HSSFRow row = summary.createRow( rowNum++ );
               row.createCell( count++ ).setCellValue( caseCount.getName() );
               row.createCell( count++ ).setCellValue( caseCount.getTotal() );

               for (Period period : caseCount.getValues())
               {
                  row.createCell( count++ ).setCellValue( period.getCount() );
               }
            }
         }
      }, model );
   }
}
