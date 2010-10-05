/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.application.pdf;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.test.AbstractQi4jTest;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.resource.caze.SubmittedFormDTO;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class SubmittedFormPdfGeneratorTest extends AbstractQi4jTest
{

   @Service
   SubmittedFormPdfGenerator pdfGenerator;

   @Service
   MemoryEntityStoreService entityStore;

   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.layerAssembly().applicationAssembly().setName( "StreamFlowServer" );
      module.addServices( SubmittedFormPdfGenerator.class );
      module.addServices( MemoryEntityStoreService.class ).visibleIn( Visibility.layer );
      module.addValues( SubmittedFormValue.class );
      module.addObjects( getClass() );

   }

   @Test
   @Ignore
   public void generatePdf() throws IOException, COSVisitorException
   {
      objectBuilderFactory.newObjectBuilder( SubmittedFormPdfGeneratorTest.class ).injectTo( this );


      entityStore.importData( readDbFileFromDisk( "/Users/henrikreinhold/Library/Application Support/StreamFlowServer/exports/streamflow_data_20101004_131529.json" ) );

      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      CaseEntity caseEntity = uow.get( CaseEntity.class, "009434c1-f52c-4a40-ad26-8c1fd68ade86-2" );
      SubmittedFormDTO submittedForm = caseEntity.getSubmittedForm( 0 );
      PDDocument pdDocument = pdfGenerator.generatepdf( null );

      try
      {
         pdDocument.save( "testoutput.pdf" );
      }
      finally
      {
         if (pdDocument != null)
         {
            pdDocument.close();
         }
      }
   }

   private String readDbFileFromDisk( String jsonFileName )
   {
      File file = new File( jsonFileName );
      StringBuffer contents = new StringBuffer();
      BufferedReader reader = null;

      try
      {
         reader = new BufferedReader( new FileReader( file ) );
         String text = null;

         while ((text = reader.readLine()) != null)
         {
            contents.append( text )
                  .append( System.getProperty(
                        "line.separator" ) );
         }
      } catch (Exception e)
      {
         e.printStackTrace();
      } finally
      {
         try
         {
            if (reader != null)
            {
               reader.close();
            }
         } catch (IOException e)
         {
            e.printStackTrace();
         }
      }
      return contents.toString();
   }
}
