/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.domain.entity.user;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.overview.ProjectSummaryDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.project.Project;

import java.util.*;

import static org.qi4j.api.query.QueryExpressions.*;

@Mixins(OverviewQueries.Mixin.class)
public interface OverviewQueries
{
   public void generateExcelProjectSummary( Locale locale, Workbook workbook );

   public LinksValue getProjectsSummary();

   class Mixin implements OverviewQueries
   {
      @Structure
      QueryBuilderFactory qbf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Identity id;

      @This
      ProjectQueries projects;

      public LinksValue getProjectsSummary()
      {
         UnitOfWork uow = uowf.currentUnitOfWork();

         LinksBuilder linksBuilder = new LinksBuilder(vbf);

         ValueBuilder<ProjectSummaryDTO> summaryBuilder = vbf.newValueBuilder( ProjectSummaryDTO.class );
         ProjectSummaryDTO summaryPrototype = summaryBuilder.prototype();
         summaryPrototype.rel().set( "project" );

         for (Project project : projects.allProjects())
         {
            summaryPrototype.text().set( project.getDescription() );
            summaryPrototype.id().set( project.toString() );
            summaryPrototype.href().set( project.toString()+"/" );

            Association<Assignee> assigneeAssociation = templateFor( Assignable.Data.class ).assignedTo();
            Association<Owner> ownableId = templateFor( Ownable.Data.class ).owner();

            QueryBuilder<CaseEntity> ownerQueryBuilder = qbf.newQueryBuilder( CaseEntity.class ).where(
                  eq( ownableId, (Owner) project ));

            QueryBuilder<CaseEntity> inboxQueryBuilder = ownerQueryBuilder.where( and(
                  isNull( assigneeAssociation ),
                  eq( templateFor( Status.Data.class ).status(), CaseStates.OPEN ) ) );
            Query<CaseEntity> inboxQuery = inboxQueryBuilder.newQuery( uow );


            QueryBuilder<CaseEntity> assignedQueryBuilder = ownerQueryBuilder.where( and(
                  isNotNull( assigneeAssociation ),
                  eq( templateFor( Status.Data.class ).status(), CaseStates.OPEN ) ) );
            Query<CaseEntity> assignedQuery = assignedQueryBuilder.newQuery( uow );

            summaryPrototype.inboxCount().set( inboxQuery.count() );
            summaryPrototype.assignedCount().set( assignedQuery.count() );

            linksBuilder.addLink( summaryBuilder.newInstance() );

         }
         return linksBuilder.newLinks();
      }

      public void generateExcelProjectSummary( Locale locale, Workbook workbook )
      {
         LinksValue projectsSummary = getProjectsSummary();
         ResourceBundle bundle = ResourceBundle.getBundle(
               OverviewQueries.class.getName(), locale );
         Sheet sheet = createSheet( bundle.getString( "projects_summary" ),
               workbook );

         // Create header cells
         Row headerRow = sheet.createRow( (short) 0 );
         headerRow.setHeightInPoints( 30 );

         createHeaderCell( bundle.getString( "project_column_header" ), workbook,
               headerRow, (short) 0, HSSFCellStyle.ALIGN_CENTER,
               HSSFCellStyle.VERTICAL_CENTER );
         createHeaderCell( bundle.getString( "inbox_column_header" ), workbook,
               headerRow, (short) 1, HSSFCellStyle.ALIGN_CENTER,
               HSSFCellStyle.VERTICAL_CENTER );
         createHeaderCell( bundle.getString( "assigned_column_header" ), workbook,
               headerRow, (short) 2, HSSFCellStyle.ALIGN_CENTER,
               HSSFCellStyle.VERTICAL_CENTER );
         createHeaderCell( bundle.getString( "total_column_header" ), workbook,
               headerRow, (short) 3, HSSFCellStyle.ALIGN_CENTER,
               HSSFCellStyle.VERTICAL_CENTER );
         short rowCounter = 0;
         
         for (LinkValue summaryValue : projectsSummary.links().get())
         {
            ProjectSummaryDTO projectSummaryDTO = (ProjectSummaryDTO) summaryValue;
            Row contentRow = sheet.createRow( ++rowCounter );
            // contentRow.setHeightInPoints(30);

            // Project
            createCell( projectSummaryDTO.text().get(), workbook, contentRow,
                  (short) 0, HSSFCellStyle.ALIGN_LEFT,
                  HSSFCellStyle.VERTICAL_TOP );
            // Inbox
            createCell( String.valueOf( projectSummaryDTO.inboxCount().get() ),
                  workbook, contentRow, (short) 1,
                  HSSFCellStyle.ALIGN_RIGHT, HSSFCellStyle.VERTICAL_TOP );
            // Assigned
            createCell( String.valueOf( projectSummaryDTO.assignedCount().get() ),
                  workbook, contentRow, (short) 2,
                  HSSFCellStyle.ALIGN_RIGHT, HSSFCellStyle.VERTICAL_TOP );
            // Total
            createCell( String.valueOf( projectSummaryDTO.inboxCount().get()
                  + projectSummaryDTO.assignedCount().get() ), workbook,
                  contentRow, (short) 3, HSSFCellStyle.ALIGN_RIGHT,
                  HSSFCellStyle.VERTICAL_TOP );
         }
      }

      /**
       * Creates a header cell and aligns it a certain way.
       *
       * @param wb     the workbook
       * @param row    the row to create the cell in
       * @param column the column number to create the cell in
       * @param halign the horizontal alignment for the cell.
       * @param valign the vertical alignment for the cell.
       */
      private static void createHeaderCell( String cellValue, Workbook wb,
                                            Row row, short column, short halign, short valign )
      {
         Cell cell = row.createCell( column );
         cell.setCellValue( new HSSFRichTextString( cellValue ) );
         CellStyle cellStyle = wb.createCellStyle();
         cellStyle.setAlignment( halign );
         cellStyle.setVerticalAlignment( valign );
         cell.setCellStyle( cellStyle );
      }

      /**
       * Creates a cell and aligns it a certain way.
       *
       * @param wb     the workbook
       * @param row    the row to create the cell in
       * @param column the column number to create the cell in
       * @param halign the horizontal alignment for the cell.
       * @param valign the vertical alignment for the cell.
       */
      private static void createCell( String cellValue, Workbook wb, Row row,
                                      short column, short halign, short valign )
      {
         Cell cell = row.createCell( column );
         cell.setCellValue( new HSSFRichTextString( cellValue ) );
         CellStyle cellStyle = wb.createCellStyle();
         cellStyle.setAlignment( halign );
         cellStyle.setVerticalAlignment( valign );
         cell.setCellStyle( cellStyle );
      }

      protected Sheet createSheet( String name, Workbook workbook)
		{
			return workbook.createSheet(name);
		}

	}
}
