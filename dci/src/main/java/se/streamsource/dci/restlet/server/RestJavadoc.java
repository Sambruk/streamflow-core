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
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * JAVADOC
 */
public class RestJavadoc
{
   private Class context;

   private Map<Class, Node> documents = new HashMap<Class, Node>();

   public RestJavadoc( Class context )
   {
      this.context = context;
   }

   public String method( String name )
   {
      if (name.endsWith( "/" ))
         name = name.substring( 0, name.length() - 1 );

      Method method = getMethod( name );

      Node doc = getDocument( method );

      if (doc == null)
         return null;

      String result;

      try
      {

         String titlePath = "//html:h3[contains(text(),'" + name + "')]/following-sibling::html:dl/html:dd/text()";
         XObject title = XPathAPI.eval( doc, titlePath );
         result = title.toString().trim();


      } catch (TransformerException e)
      {
         result = "";
      }

      if (result.equals( "" ))
         result = null;

      return result;
   }

   private Node getDocument( Method method )
   {
      Class clazz = method.getDeclaringClass();

      Node doc = documents.get( clazz );

      if (doc == null)
      {

         File target = new File( clazz.getProtectionDomain().getCodeSource().getLocation().getPath() ).getParentFile();
         File apiDocs = new File( target, "site/apidocs" );
         File classDocs = new File( apiDocs, clazz.getName().replace( '.', '/' ) + ".html" );

         if (!classDocs.exists())
            return null;

         try
         {
            Parser p = new Parser();
            p.setFeature( "http://xml.org/sax/features/namespace-prefixes", true );
            // to define the html: prefix (off by default)
            SAX2DOM sax2dom = new SAX2DOM();
            p.setContentHandler( sax2dom );
            p.parse( new InputSource( new FileReader( classDocs ) ) );
            doc = sax2dom.getDOM();

//         TransformerFactory.newInstance().newTransformer().transform(new DOMSource( doc ), new StreamResult(System.out));

            documents.put( clazz, doc );
         } catch (Exception e)
         {
            throw new IllegalArgumentException( "Could not parse HTML for class", e );
         }
      }

      return doc;
   }

   private Method getMethod( String name )
   {
      for (Method method : context.getMethods())
      {
         if (method.getName().equals( name ))
            return method;
      }

      throw new IllegalArgumentException( "Method not found:" + name );
   }
}
