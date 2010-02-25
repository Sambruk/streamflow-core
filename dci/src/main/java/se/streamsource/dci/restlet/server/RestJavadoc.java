/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.dci.restlet.server;

import com.sun.org.apache.xalan.internal.xsltc.trax.SAX2DOM;
import com.sun.org.apache.xpath.internal.XPathAPI;
import com.sun.org.apache.xpath.internal.objects.XObject;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * JAVADOC
 */
public class RestJavadoc
{
   private Class context;
   public Node doc;

   public RestJavadoc( Class context )
   {

      this.context = context;

      File target = new File( context.getProtectionDomain().getCodeSource().getLocation().getPath() ).getParentFile();
      File apiDocs = new File( target, "site/apidocs" );
      File classDocs = new File( apiDocs, context.getName().replace( '.', '/' ) + ".html" );

      try
      {
         Parser p = new Parser();
         p.setFeature( "http://xml.org/sax/features/namespace-prefixes", true );
         // to define the html: prefix (off by default)
         SAX2DOM sax2dom = new SAX2DOM();
         p.setContentHandler( sax2dom );
         p.parse( new InputSource( new FileReader(classDocs) ) );
         doc = sax2dom.getDOM();

         TransformerFactory.newInstance().newTransformer().transform(new DOMSource( doc ), new StreamResult(System.out));
      } catch (Exception e)
      {
         throw new IllegalArgumentException( "Could not parse HTML for class", e );
      }
   }

   public String method( String name )
   {
      String result;

      try
      {
         if (name.endsWith( "/" ))
            name = name.substring( 0, name.length()-1 );

         String titlePath = "//html:pre/b['"+name+"()']/following-sibling::html:dl[0]";
         XObject title = XPathAPI.eval( doc, titlePath );
         result =  title.toString();


      } catch (TransformerException e)
      {
         result = "";
      }

      if (result.equals(""))
         result = null;

      return result;
   }
}
