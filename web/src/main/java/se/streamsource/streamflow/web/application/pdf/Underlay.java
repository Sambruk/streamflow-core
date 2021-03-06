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
package se.streamsource.streamflow.web.application.pdf;

import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.COSStreamArray;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.util.MapUtil;

import java.io.*;
import java.util.*;

/**
 * Underlay one document with a template, mainly used to apply header/footer to an existing document
 * How it (should) work:<br>
 * If the document has 10 pages, and the layout 2 the following is the result:<br>
 * <pre>
 * Document: 1234567890
 * Layout  : 1212121212
 * </pre>
 * <br>
 * <p/>
 * This is based on the org.apache.pdfbox.Overlay class but has been modified to work properly by
 *
 * @author Henrik Reinhold (henrik.reinhold@jayway.com)
 *         <p/>
 *         Original authors:
 * @author Mario Ivankovits (mario@ops.co.at)
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 */
public class Underlay
{

   private List layoutPages = new ArrayList( 10 );

   private PDDocument pdfUnderlay;
   private PDDocument pdfDocument;
   private int pageCount = 0;
   private COSStream saveGraphicsStateStream;
   private COSStream restoreGraphicsStateStream;


   private static PDDocument getDocument( String filename ) throws IOException
   {
      FileInputStream input = null;
      PDFParser parser = null;
      PDDocument result = null;
      try
      {
         input = new FileInputStream( filename );
         parser = new PDFParser( input );
         parser.parse();
         result = parser.getPDDocument();
      }
      finally
      {
         if (input != null)
         {
            input.close();
         }
      }
      return result;
   }

   /**
    * Private class.
    */
   private static class LayoutPage
   {
      private final COSBase contents;
      private final COSDictionary res;
      private final Map objectNameMap;

      /**
       * Constructor.
       *
       * @param contentsValue      The contents.
       * @param resValue           The resource dictionary
       * @param objectNameMapValue The map
       */
      public LayoutPage( COSBase contentsValue, COSDictionary resValue, Map objectNameMapValue )
      {
         contents = contentsValue;
         res = resValue;
         objectNameMap = objectNameMapValue;
      }
   }

   /**
    * This will underlay two documents onto each other.  The underlay document is
    * repeatedly underlayed under the destination document for every page in the
    * destination.
    */
   public PDDocument underlay(PDDocument destination, InputStream templateStream) throws IOException {
      pdfDocument = destination;
      try {
         PDFParser parser = new PDFParser(templateStream);
         parser.parse();
         pdfUnderlay = parser.getPDDocument();
         overlayWithDarkenBlendMode(pdfDocument, pdfUnderlay);
      } finally {
         if (templateStream != null) {
            templateStream.close();
         }
      }
      return pdfDocument;
   }


   private void overlayWithDarkenBlendMode(PDDocument document, PDDocument overlay) throws IOException {
      PDXObjectForm xobject = importAsXObject(document, (PDPage) overlay.getDocumentCatalog().getAllPages().get(0));
      PDExtendedGraphicsState darken = new PDExtendedGraphicsState();
      darken.getCOSDictionary().setName("BM", "Darken");

      List<PDPage> pages = document.getDocumentCatalog().getAllPages();

      for (PDPage page : pages) {
         Map<String, PDExtendedGraphicsState> states = page.getResources().getGraphicsStates();
         if (states == null)
            states = new HashMap<>();
         String darkenKey = MapUtil.getNextUniqueKey(states, "Dkn");
         states.put(darkenKey, darken);
         page.getResources().setGraphicsStates(states);

         PDPageContentStream stream = new PDPageContentStream(document, page, true, false, true);
         stream.appendRawCommands(String.format("/%s gs ", darkenKey));
         stream.drawXObject(xobject, 0, 0, 1, 1);
         stream.close();
      }
   }

   private PDXObjectForm importAsXObject(PDDocument target, PDPage page) throws IOException {
      final PDStream xobjectStream = new PDStream(target, page.getContents().createInputStream(), false);
      final PDXObjectForm xobject = new PDXObjectForm(xobjectStream);

      xobject.setResources(page.findResources());
      xobject.setBBox(page.findCropBox());

      COSDictionary group = new COSDictionary();
      group.setName("S", "Transparency");
      group.setBoolean(COSName.getPDFName("K"), true);
      xobject.getCOSStream().setItem(COSName.getPDFName("Group"), group);

      return xobject;
   }

   private COSStream makeUniqObjectNames( Map objectNameMap, COSStream stream ) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream( 10240 );

      byte[] buf = new byte[10240];
      int read;
      InputStream is = stream.getUnfilteredStream();
      while ((read = is.read( buf )) > -1)
      {
         baos.write( buf, 0, read );
      }

      buf = baos.toByteArray();
      baos = new ByteArrayOutputStream( buf.length + 100 );
      StringBuffer sbObjectName = new StringBuffer( 10 );
      boolean bInObjectIdent = false;
      boolean bInText = false;
      boolean bInEscape = false;
      for (int i = 0; i < buf.length; i++)
      {
         byte b = buf[i];

         if (!bInEscape)
         {
            if (!bInText && b == '(')
            {
               bInText = true;
            }
            if (bInText && b == ')')
            {
               bInText = false;
            }
            if (b == '\\')
            {
               bInEscape = true;
            }

            if (!bInText && !bInEscape)
            {
               if (b == '/')
               {
                  bInObjectIdent = true;
               } else if (bInObjectIdent && Character.isWhitespace( (char) b ))
               {
                  bInObjectIdent = false;

                  String objectName = sbObjectName.toString().substring( 1 );
                  String newObjectName = objectName + "overlay";
                  baos.write( '/' );
                  baos.write( newObjectName.getBytes() );

                  objectNameMap.put( objectName, COSName.getPDFName( newObjectName ) );

                  sbObjectName.delete( 0, sbObjectName.length() );
               }
            }

            if (bInObjectIdent)
            {
               sbObjectName.append( (char) b );
               continue;
            }
         } else
         {
            bInEscape = false;
         }

         baos.write( b );
      }

      COSDictionary streamDict = new COSDictionary();
      streamDict.setInt( COSName.LENGTH, baos.size() );
      COSStream output = new COSStream( streamDict, pdfDocument.getDocument().getScratchFile() );
      output.setFilters( stream.getFilters() );
      OutputStream os = output.createUnfilteredStream();
      baos.writeTo( os );
      os.close();

      return output;
   }

   private void processPages( List pages ) throws IOException
   {
      Iterator pageIter = pages.iterator();
      while (pageIter.hasNext())
      {
         PDPage page = (PDPage) pageIter.next();
         COSDictionary pageDictionary = page.getCOSDictionary();
         COSBase contents = pageDictionary.getDictionaryObject( COSName.CONTENTS );
         if (contents instanceof COSStreamArray)
         {
            COSStreamArray cosStreamArray = (COSStreamArray) contents;
            COSArray array = new COSArray();
            for (int i = 0; i < cosStreamArray.getStreamCount(); i++)
            {
               array.add(cosStreamArray.get( i ));
            }
            mergePage( array, page );   
            pageDictionary.setItem( COSName.CONTENTS, array );
         } else if (contents instanceof COSStream)
         {
            COSStream contentsStream = (COSStream) contents;

            COSArray array = new COSArray();

            array.add( contentsStream );

            mergePage( array, page );

            pageDictionary.setItem( COSName.CONTENTS, array );
         } else if (contents instanceof COSArray)
         {
            COSArray contentsArray = (COSArray) contents;

            mergePage( contentsArray, page );
         } else
         {
            throw new IOException( "Contents are unknown type:" + contents.getClass().getName() );
         }
         pageCount++;
      }
   }

   private void mergePage( COSArray array, PDPage page )
   {
      int layoutPageNum = pageCount % layoutPages.size();
      LayoutPage layoutPage = (LayoutPage) layoutPages.get( layoutPageNum );
      PDResources resources = page.findResources();
      if (resources == null)
      {
         resources = new PDResources();
         page.setResources( resources );
      }
      COSDictionary docResDict = resources.getCOSDictionary();
      COSDictionary layoutResDict = layoutPage.res;


      mergeArray( COSName.PROC_SET, docResDict, layoutResDict );
      mergeDictionary( COSName.COLORSPACE, docResDict, layoutResDict, layoutPage.objectNameMap );
      mergeDictionary( COSName.FONT, docResDict, layoutResDict, layoutPage.objectNameMap );
      mergeDictionary( COSName.XOBJECT, docResDict, layoutResDict, layoutPage.objectNameMap );
      mergeDictionary( COSName.EXT_G_STATE, docResDict, layoutResDict, layoutPage.objectNameMap );

      array.add( 0, layoutPage.contents );

   }

   /**
    * merges two dictionaries.
    *
    * @param dest
    * @param source
    */
   private void mergeDictionary( COSName name, COSDictionary dest, COSDictionary source, Map objectNameMap )
   {
      COSDictionary destDict = (COSDictionary) dest.getDictionaryObject( name );
      COSDictionary sourceDict = (COSDictionary) source.getDictionaryObject( name );

      if (destDict == null)
      {
         destDict = new COSDictionary();
         dest.setItem( name, destDict );
      }
      if (sourceDict != null)
      {

         for (Map.Entry<COSName, COSBase> entry : sourceDict.entrySet())
         {
            COSName mappedKey = (COSName) objectNameMap.get( entry.getKey().getName() );
            if (mappedKey != null)
            {
               destDict.setItem( mappedKey, entry.getValue() );
            }
         }
      }
   }

   /**
    * merges two arrays.
    *
    * @param dest
    * @param source
    */
   private void mergeArray( COSName name, COSDictionary dest, COSDictionary source )
   {
      COSArray destDict = (COSArray) dest.getDictionaryObject( name );
      COSArray sourceDict = (COSArray) source.getDictionaryObject( name );

      if (destDict == null)
      {
         destDict = new COSArray();
         dest.setItem( name, destDict );
      }

      for (int sourceDictIdx = 0; sourceDict != null && sourceDictIdx < sourceDict.size(); sourceDictIdx++)
      {
         COSBase key = sourceDict.get( sourceDictIdx );
         if (key instanceof COSName)
         {
            COSName keyname = (COSName) key;

            boolean bFound = false;
            for (int destDictIdx = 0; destDictIdx < destDict.size(); destDictIdx++)
            {
               COSBase destkey = destDict.get( destDictIdx );
               if (destkey instanceof COSName)
               {
                  COSName destkeyname = (COSName) destkey;
                  if (destkeyname.equals( keyname ))
                  {
                     bFound = true;
                     break;
                  }
               }
            }
            if (!bFound)
            {
               destDict.add( keyname );
            }
         }
      }
   }
}
