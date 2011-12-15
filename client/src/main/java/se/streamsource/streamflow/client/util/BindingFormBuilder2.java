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

package se.streamsource.streamflow.client.util;

import java.awt.Component;

import javax.swing.JLabel;

import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * Form builder that allows easy binding at the same time.
 *
 * CAUTION: Really think twice before changing this class! We don't want it to become like the first version with lots of weird methods.
 */
public class BindingFormBuilder2
{
   private DefaultFormBuilder formBuilder;
   private ResourceMap resourceMap;
   private ActionBinder actionBinder;
   private ValueBinder valueBinder;

   public BindingFormBuilder2( DefaultFormBuilder formBuilder, ActionBinder actionBinder, ValueBinder valueBinder )
   {
      this( formBuilder, actionBinder, valueBinder, null );
   }

   public BindingFormBuilder2( DefaultFormBuilder formBuilder, ActionBinder actionBinder, ValueBinder valueBinder, ResourceMap resourceMap )
   {
      this.formBuilder = formBuilder;
      this.actionBinder = actionBinder;
      this.valueBinder = valueBinder;
      this.resourceMap = resourceMap;
   }

   public BindingFormBuilder2 appendWithLabel( Enum resourceKey, Component component, String valueBinding, String actionBinding, Object... args)
   {
      String resource = getResource( resourceKey, args );

      JLabel label = formBuilder.append( resource );
      label.setFocusable( false );
      label.setLabelFor( component );
      return append( component, valueBinding, actionBinding);
   }

   public BindingFormBuilder2 append( Component component, String valueBinding, String actionBinding )
   {
      if (valueBinding != null)
         valueBinder.bind( valueBinding, component );

      if (actionBinding != null)
         actionBinder.bind( actionBinding, component);

      formBuilder.append( component );

      return this;
   }

   public BindingFormBuilder2 nextLine()
   {
      formBuilder.nextLine();
      return this;
   }

   public String getResource( Enum resourceKey, Object... args )
   {
      String key = resourceKey.toString();

      String resource = resourceMap == null ? null : resourceMap.getString( key, args );
      if (resource == null)
      {
         ResourceMap map = Application.getInstance().getContext().getResourceMap( resourceKey.getClass() );
         resource = map.getString( key, args );
      }

      if (resource == null)
      {
         resource = "#" + key;
      }
      return resource;
   }

   public void setActionBinder( ActionBinder actionBinder )
   {
      this.actionBinder = actionBinder;
   }

   public void setValueBinder( ValueBinder valueBinder )
   {
      this.valueBinder = valueBinder;
   }

   public DefaultFormBuilder getFormBuilder()
   {
      return formBuilder;
   }
}


