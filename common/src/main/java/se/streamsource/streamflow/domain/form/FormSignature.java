/*
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

package se.streamsource.streamflow.domain.form;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * Signature for a form
 */
public interface FormSignature
   extends ValueComposite
{
   /**
    * This is the TBS-variant of the form which is shown to the user
    * 
    *
    * @return
    */
   Property<String> form();

   /**
    * This is the TBS (To-Be-Signed text) in an encoded
    * form, typically a hash.
    *
    * @return
    */
   Property<String> encodedForm();

   Property<String> signerId();

   /**
    * This is the signature string after the user
    * has signed the form
    * @return
    */
   Property<String> signature();

   /**
    * This is the id of the provider client used
    * to perform the signing. TODO List clients
    *
    * @return
    */
   Property<String> provider();
}
