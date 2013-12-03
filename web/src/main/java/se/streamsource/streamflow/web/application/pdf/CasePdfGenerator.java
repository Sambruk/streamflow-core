/**
 *
 * Copyright 2009-2013 Jayway Products AB
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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;
import org.qi4j.api.io.Transforms;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.util.DateFunctions;
import se.streamsource.streamflow.api.administration.form.AttachmentFieldValue;
import se.streamsource.streamflow.api.administration.form.DateFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.api.administration.form.GeoLocationFieldValue;
import se.streamsource.streamflow.api.administration.form.LocationDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactAddressDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageType;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.api.workspace.cases.general.FormSignatureDTO;
import se.streamsource.streamflow.util.Translator;
import se.streamsource.streamflow.web.context.workspace.cases.conversation.MessagesContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseDescriptor;
import se.streamsource.streamflow.web.domain.entity.caze.CaseOutput;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLogEntryValue;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolvable;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.caze.CasePriority;
import se.streamsource.streamflow.web.domain.structure.caze.Notes;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;
import se.streamsource.streamflow.web.domain.structure.created.Creator;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedPageValue;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import static se.streamsource.streamflow.util.Strings.*;

/**
 * A specialisation of CaseOutput that is responsible for exporting a case in
 * PDF format; The provided configuration tells what parts of the case are
 * included in the export.
 */
public class CasePdfGenerator implements CaseOutput
{
   @Structure
   Module module;

   @Service
   AttachmentStore store;

   private PdfDocument document;
   private ResourceBundle bundle;
   private final CaseOutputConfigDTO config;
   private Locale locale;
   private String templateUri;

   private PdfFont h1Font = new PdfFont( PDType1Font.HELVETICA_BOLD, 16 );
   private PdfFont valueFont = new PdfFont( PDType1Font.HELVETICA, 12 );
   private PdfFont valueFontBold = new PdfFont( PDType1Font.HELVETICA_BOLD, 12 );
   private PdfFont valueFontBoldItalic = new PdfFont( PDType1Font.HELVETICA_BOLD_OBLIQUE, 12 );
   private PdfFont headerFont = new PdfFont( PDType1Font.HELVETICA, 10 );

   private String caseId = "";
   private String printedOn = "";

   private Color headingColor = new Color(0x4b,0x89,0xd0);

   public CasePdfGenerator(@Uses CaseOutputConfigDTO config, @Optional @Uses String templateUri, @Uses Locale locale, @Uses PdfDocument document)
   {
      this.config = config;
      this.locale = locale;
      this.templateUri = templateUri;
      bundle = ResourceBundle.getBundle( CasePdfGenerator.class.getName(), locale );
      this.document = document;
      document.init();
   }

   public void outputCase( CaseDescriptor cazeDescriptor ) throws Throwable
   {
      Case caze = cazeDescriptor.getCase();

      caseId = ((CaseId.Data) caze).caseId().get();
      printedOn = DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, locale ).format( new Date() );

      document.print( "", valueFont ).changeColor( headingColor )
            .println( bundle.getString( "caseSummary" ) + " - " + caseId, h1Font ).line().changeColor( Color.BLACK )
            .print( "", valueFont );

      float tabStop = document.calculateTabStop( valueFontBold, bundle.getString( "title" ),
            bundle.getString( "createdOn" ), bundle.getString( "createdBy" ), bundle.getString( "owner" ),
            bundle.getString( "assignedTo" ), bundle.getString( "caseType" ), bundle.getString( "labels" ),
            bundle.getString( "resolution" ), bundle.getString( "dueOn" ), bundle.getString( "priority" ) );

      document.printLabelAndTextWithTabStop( bundle.getString( "title" ) + ": ", valueFontBold, caze.getDescription() == null ? "" : caze.getDescription(), valueFont,
            tabStop );

      if (((CasePriority.Data)caze).priority().get() != null)
      {
         document.printLabelAndTextWithTabStop( bundle.getString( "priority" ) + ": ", valueFontBold,
               ((CasePriority.Data)caze).priority().get().getDescription(),
               valueFont, tabStop );
      }
      
      document.printLabelAndTextWithTabStop( bundle.getString( "createdOn" ) + ": ", valueFontBold,
            DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, locale ).format( caze.createdOn().get() ),
            valueFont, tabStop );

      if (((DueOn.Data) caze).dueOn().get() != null)
      {
         document.printLabelAndTextWithTabStop( bundle.getString( "dueOn" ) + ": ", valueFontBold,
               new SimpleDateFormat( bundle.getString( "date_format" ) ).format( ((DueOn.Data) caze).dueOn().get() ),
               valueFont, tabStop );
      }

      Creator creator = caze.createdBy().get();
      if (creator != null)
      {
         document.printLabelAndTextWithTabStop( bundle.getString( "createdBy" ) + ": ", valueFontBold,
               ((Describable) creator).getDescription(), valueFont, tabStop );
      }

      Owner owner = ((Ownable.Data) caze).owner().get();
      if (owner != null)
      {
         document.printLabelAndTextWithTabStop( bundle.getString( "owner" ) + ": ", valueFontBold,
               ((Describable) owner).getDescription(), valueFont, tabStop );
      }

      Assignee assignee = ((Assignable.Data) caze).assignedTo().get();
      if (assignee != null)
      {
         document.printLabelAndTextWithTabStop( bundle.getString( "assignedTo" ) + ": ", valueFontBold,
               ((Describable) assignee).getDescription(), valueFont, tabStop );
      }

      CaseType caseType = ((TypedCase.Data) caze).caseType().get();

      if (caseType != null)
      {
         document.printLabelAndTextWithTabStop( bundle.getString( "caseType" ) + ": ", valueFontBold,
               ((Describable) caseType).getDescription(), valueFont, tabStop );
      }

      Resolution resolution = ((Resolvable.Data) caze).resolution().get();

      if (resolution != null)
      {
         document.printLabelAndTextWithTabStop( bundle.getString( "resolution" ) + ":", valueFontBold,
               ((Describable) resolution).getDescription(), valueFont, tabStop );
      }

      List<Label> labels = ((Labelable.Data) caze).labels().toList();
      if (!labels.isEmpty())
      {
         String allLabels = "";
         int count = 0;
         for (Label label : labels)
         {
            count++;
            allLabels += label.getDescription() + (count == labels.size() ? "" : ", ");
         }

         document.printLabelAndTextWithTabStop( bundle.getString( "labels" ) + ": ", valueFontBold, allLabels, valueFont, tabStop );
      }

      String note = ((Notes)caze).getLastNote() != null ? ((Notes)caze).getLastNote().note().get() : "";
      if (!empty(note))
      {
         if( Translator.HTML.equalsIgnoreCase( ((Notes) caze).getLastNote().contentType().get() ))
         {
            note = Translator.htmlToText( note );
         }
         document.moveUpOneRow( valueFontBold ).print( bundle.getString( "note" ) + ":", valueFontBold ).print( note, valueFont ).print( "", valueFont );
      }

      // traverse structure
      if (config.contacts().get())
      {
         generateContacts( cazeDescriptor.contacts() );
      }

      if (config.submittedForms().get())
      {
         generateSubmittedForms( cazeDescriptor.submittedForms() );
      }

      if (config.conversations().get())
      {
         generateConversations( cazeDescriptor.conversations() );
      }

      if (config.attachments().get())
      {
         generateAttachments( cazeDescriptor.attachments() );
      }

      if (config.caselog().get())
      {
         generateCaselog(cazeDescriptor.caselog());
      }
   }

   private void generateCaselog(Input<CaseLogEntryValue, RuntimeException> caselog) throws IOException
   {
      // TODO This needs to be cleaned up. Translations should be in a better place!
      ResourceBundle bnd = ResourceBundle.getBundle( MessagesContext.class.getName(), locale );
      final Map<String, String> translations = new HashMap<String, String>();
      for (String key : bnd.keySet())
      {
         translations.put(key, bnd.getString(key));
      }

      caselog.transferTo(new Output<CaseLogEntryValue, IOException>()
      {
         public <SenderThrowableType extends Throwable> void receiveFrom(Sender<? extends CaseLogEntryValue, SenderThrowableType> sender) throws IOException, SenderThrowableType
         {
            document.changeColor( headingColor ).println( bundle.getString( "caselog" ), valueFontBold )
                  .changeColor(Color.BLACK);

            sender.sendTo(new Receiver<CaseLogEntryValue, IOException>()
            {
               public void receive(CaseLogEntryValue entry) throws IOException
               {
                  UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
                  String label = uow.get( Describable.class, entry.createdBy().get().identity()).getDescription()
                        + ", "
                        + DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, locale ).format(
                              entry.createdOn().get() ) + ": ";

                  document.print( label, valueFontBold ).print( Translator.translate( entry.message().get(), translations ), valueFont )
                        .print("", valueFont);
               }
            });
         }
      });
   }

   private void generateContacts(Input<ContactDTO, RuntimeException> contacts) throws IOException
   {
      final Transforms.Counter<ContactDTO> counter = new Transforms.Counter<ContactDTO>();
      contacts.transferTo(Transforms.map(counter, new Output<ContactDTO, IOException>()
      {
         public <SenderThrowableType extends Throwable> void receiveFrom(Sender<? extends ContactDTO, SenderThrowableType> sender) throws IOException, SenderThrowableType
         {
            sender.sendTo(new Receiver<ContactDTO, IOException>()
            {
               public void receive(ContactDTO value) throws IOException
               {
                  Map<String, String> nameValuePairs = new LinkedHashMap<String, String>( 10 );
                  if (!empty(value.name().get()))
                     nameValuePairs.put( bundle.getString( "name" ), value.name().get() );

                  if (!value.phoneNumbers().get().isEmpty()
                        && !empty(value.phoneNumbers().get().get(0).phoneNumber().get()))
                     nameValuePairs.put( bundle.getString( "phoneNumber" ), value.phoneNumbers().get().get( 0 )
                           .phoneNumber().get() );

                  if (!value.addresses().get().isEmpty())
                  {
                     ContactAddressDTO address = value.addresses().get().get( 0 );
                     if (!empty(address.address().get()))
                        nameValuePairs.put( bundle.getString( "address" ), address.address().get() );
                     if (!empty(address.zipCode().get()))
                        nameValuePairs.put( bundle.getString( "zipCode" ), address.zipCode().get() );
                     if (!empty(address.city().get()))
                        nameValuePairs.put( bundle.getString( "city" ), address.city().get() );
                     if (!empty(address.region().get()))
                        nameValuePairs.put( bundle.getString( "region" ), address.region().get() );
                     if (!empty(address.country().get()))
                        nameValuePairs.put( bundle.getString( "country" ), address.country().get() );
                  }

                  if (!value.emailAddresses().get().isEmpty()
                        && !empty(value.emailAddresses().get().get(0).emailAddress().get()))
                     nameValuePairs.put( bundle.getString( "email" ), value.emailAddresses().get().get( 0 ).emailAddress()
                           .get() );

                  if (!empty(value.contactId().get()))
                     nameValuePairs.put( bundle.getString( "contactID" ), value.contactId().get() );

                  if (!empty(value.company().get()))
                     nameValuePairs.put( bundle.getString( "company" ), value.company().get() );

                  if (!empty(value.note().get()))
                     nameValuePairs.put( bundle.getString( "note" ), value.note().get() );

                  float tabStop = document.calculateTabStop( valueFontBold,
                        nameValuePairs.keySet().toArray( new String[nameValuePairs.keySet().size()] ) );

                  if (!nameValuePairs.entrySet().isEmpty())
                  {
                     document.changeColor( headingColor );
                     document.print(
                           bundle.getString( "contact" ) + (counter.getCount() == 1 ? "" : " " + counter.getCount()),
                           valueFontBold );
                     document.changeColor( Color.BLACK ).print( "", valueFont );
                  }

                  for (Map.Entry<String, String> stringEntry : nameValuePairs.entrySet())
                  {
                     document.printLabelAndTextWithTabStop( stringEntry.getKey() + ":", valueFontBold, stringEntry.getValue(),
                           valueFont, tabStop );
                  }
               }
            } );
         }
      } ) );
   }

   public void generateConversations( Input<Conversation, RuntimeException> conversations ) throws IOException
   {
      final Transforms.Counter<Conversation> counter = new Transforms.Counter<Conversation>();
      Output<Conversation, IOException> output = Transforms.map( counter, new Output<Conversation, IOException>()
      {
         public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends Conversation, SenderThrowableType> sender ) throws IOException, SenderThrowableType
         {
            sender.sendTo( new Receiver<Conversation, IOException>()
            {
               public void receive( Conversation conversation ) throws IOException
               {
                  if (counter.getCount() == 1)
                  {
                     document.changeColor( headingColor ).print( bundle.getString( "conversations" ), valueFontBold )
                           .changeColor( Color.BLACK );
                  }

                  List<Message> messages = ((Messages.Data) conversation).messages().toList();
                  if (!messages.isEmpty())
                  {
                     document.print( conversation.getDescription(), valueFontBold );

                     for (Message message : messages)
                     {
                        Message.Data data = ((Message.Data) message);
                        String label = data.sender().get().getDescription()
                              + ", "
                              + DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, locale ).format(
                              data.createdOn().get() ) + ": ";
                        String text = data.body().get();
                        if(MessageType.HTML.equals( data.messageType().get() ))
                        {
                           text = Translator.htmlToText( text );
                        }
                        document.print( label, valueFontBold ).print( text, valueFont )
                              .print( "", valueFont );
                     }
                  }
               }
            } );
         }
      } );
      conversations.transferTo( output );
   }

   public void generateSubmittedForms( Input<SubmittedFormValue, RuntimeException> submittedForms ) throws IOException
   {

      ArrayList<SubmittedFormValue> formValues = new ArrayList<SubmittedFormValue>();
      submittedForms.transferTo( Outputs.collection( formValues ) );

      // Latest forms first please
      Collections.reverse( formValues );

      Set<EntityReference> printedForms = new HashSet<EntityReference>();
      boolean printedHeader = false;
      for (SubmittedFormValue formValue : formValues)
      {
         if (printedForms.contains( formValue.form().get() ))
            continue; // Skip this form - already printed

         Describable form = module.unitOfWorkFactory().currentUnitOfWork().get( Describable.class, formValue.form().get().identity() );

         // Form PDF section header
         if (!printedHeader)
         {
            document.changeColor( headingColor );
            document.print( bundle.getString( "submittedForms" ) + ":", valueFontBold );
            document.changeColor( Color.BLACK );
            printedHeader = true;
         }

         document.print( form.getDescription() + ":", valueFontBold ).print( "", valueFontBold ).print( "", valueFontBold );


         float tabStop = document.calculateTabStop( valueFontBold,
                                 new String[]{bundle.getString( "lastSubmitted" ), bundle.getString( "lastSubmittedBy" )} );

         // Submitted by whom and when
         document.printLabelAndTextWithTabStop( bundle.getString( "lastSubmitted" ) + ":", valueFontBold,
               DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, locale ).format( formValue.submissionDate().get() ),
               valueFont, tabStop );
         document.printLabelAndTextWithTabStop( bundle.getString( "lastSubmittedBy" ) + ":", valueFontBold,
               module.unitOfWorkFactory().currentUnitOfWork().get( Describable.class, formValue.submitter().get().identity() ).getDescription(), valueFont,
               tabStop );

         if (formValue.signatures().get() != null && !formValue.signatures().get().isEmpty())
         {
            FormSignatureDTO signature = formValue.signatures().get().get( 0 );
            document.printLabelAndTextWithTabStop( bundle.getString( "signedBy" ) + ":", valueFontBold,
                  signature.signerName().get() + " (" + signature.signerId().get() + ")", valueFont,
               tabStop )  ;
         }
         
         for (SubmittedPageValue submittedPageValue : formValue.pages().get())
         {
            if (!submittedPageValue.fields().get().isEmpty())
            {
               Describable page = module.unitOfWorkFactory().currentUnitOfWork().get( Describable.class, submittedPageValue.page().get().identity() );
               document.print( page.getDescription() + ( page.getDescription().trim().endsWith( ":" ) ? "" : ":" ), valueFontBoldItalic );
               document.print( "", valueFont );

               // Fetch and occasionally format field name and value
               for (SubmittedFieldValue submittedFieldValue : submittedPageValue.fields().get())
               {

                  FieldValue fieldValue = module.unitOfWorkFactory().currentUnitOfWork().get( FieldEntity.class, submittedFieldValue.field().get().identity() )
                        .fieldValue().get();

                  if (!empty(submittedFieldValue.value().get()))
                  {
                     String label = module.unitOfWorkFactory().currentUnitOfWork().get( Describable.class, submittedFieldValue.field().get().identity() )
                           .getDescription();
                     String value = "";
                     // convert JSON String if field type AttachmentFieldValue
                     if (fieldValue instanceof AttachmentFieldValue)
                     {
                        AttachmentFieldSubmission attachment = module.valueBuilderFactory().newValueFromJSON(AttachmentFieldSubmission.class, submittedFieldValue
                              .value().get());
                        value = attachment.name().get();

                     } else if (fieldValue instanceof DateFieldValue && !empty(submittedFieldValue.value().get()))
                     {
                        value = new SimpleDateFormat( bundle.getString( "date_format" ) ).format( DateFunctions
                              .fromString( submittedFieldValue.value().get() ) );
                     } else if ( fieldValue instanceof GeoLocationFieldValue ) 
                     {
                        LocationDTO locationDTO = module.valueBuilderFactory().newValueFromJSON( LocationDTO.class, submittedFieldValue.value().get() );
                        value = locationDTO.street().get() + ", " + locationDTO.zipcode().get() + ", " + locationDTO.city().get();
//                        String locationString = locationDTO.location().get();
//                        if (locationString != null) {
//                           locationString = locationString.replace( ' ', '+' );
//                           if (locationString.contains( "(" )) {
//                              String[] positions = locationString.split( "\\),\\(");
//                              locationString = positions[0].substring( 1, positions[0].length() -1 );
//                           }
//                        }
//                        text += "<a href=\"http://maps.google.com/maps?z=13&t=m&q=" + locationString + "\" alt=\"Google Maps\">Klicka här för att visa karta</a>";
                     } else
                     {
                        value = submittedFieldValue.value().get();
                     }
                     document.printLabelAndIndentedText( label + ( label.trim().endsWith( ":" ) ? "" : ":" ), valueFontBold, value, valueFont, 20.0f );
                  }
               }
               document.print( "", valueFont );
            }
         }
         printedForms.add( formValue.form().get() );
      }
   }

   public void generateAttachments( Input<Attachment, RuntimeException> attachments ) throws IOException
   {
      final Transforms.Counter<Attachment> counter = new Transforms.Counter<Attachment>();
      attachments.transferTo( Transforms.map( counter, new Output<Attachment, IOException>()
      {
         public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends Attachment, SenderThrowableType> sender ) throws IOException, SenderThrowableType
         {
            sender.sendTo( new Receiver<Attachment, IOException>()
            {
               public void receive( Attachment attachment ) throws IOException
               {
                  if (counter.getCount() == 1)
                  {
                     document.changeColor( headingColor ).print( bundle.getString( "attachments" ) + ":", valueFontBold )
                           .changeColor( Color.BLACK );
                  }

                  document.print( ((AttachedFile.Data) attachment).name().get(), valueFont );

/* TODO Fix image insert. For some reason adding images to a PDF doesn't seem to work
                  if (((AttachedFile.Data) attachment).mimeType().get().startsWith("image/"))
                  {
                     try
                     {
                        store.attachment(((AttachedFile.Data) attachment).uri().get(), new Visitor<InputStream, IOException>()
                        {
                           public boolean visit(InputStream visited) throws IOException
                           {
                              BufferedImage image = ImageIO.read(visited);

                              document.print("Image insert", valueFont);

                              document.insertImage(image);

                              document.print("Image inserted", valueFont);

                              return true;
                           }
                        });
                     } catch (IOException e)
                     {
                        LoggerFactory.getLogger(getClass()).warn("Could not insert image into generated PDF", e);
                     }
                  }
*/
               }
            } );
         }
      } ) );
   }

   public PDDocument getPdf() throws IOException
   {
      document.closeAndReturn();
      PDDocument generatedDoc = document.generateHeaderAndPageNumbers( headerFont, caseId, bundle.getString( "printDate" )
            + ": " + printedOn );

      generatedDoc.getDocumentInformation().setCreator( "Streamflow" );
      Calendar calendar = Calendar.getInstance();
      generatedDoc.getDocumentInformation().setCreationDate( calendar );
      generatedDoc.getDocumentInformation().setTitle( caseId );

      if (templateUri != null)
      {

         String attachmentId;
         try
         {
            attachmentId = new URI( templateUri ).getSchemeSpecificPart();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            store.attachment(attachmentId).transferTo(Outputs.byteBuffer(baos));
            
            Underlay underlay = new Underlay();
            generatedDoc = underlay.underlay(generatedDoc, new ByteArrayInputStream(baos.toByteArray()));

         } catch (Exception e)
         {

            e.printStackTrace();
         }
      }

      return generatedDoc;
   }
}
