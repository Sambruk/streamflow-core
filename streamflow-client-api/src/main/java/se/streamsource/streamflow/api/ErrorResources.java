/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.api;

/**
 * i18n resources for error handling on both server and client.
 */
public enum ErrorResources
{
   username_password_violation,
   unauthorized_access,
   concurrent_change,
   Conflict,
   communication_error,

   search_string_malformed,
   error,
   user_already_exists,
   description_cannot_be_more_than_50,
   project_remove_failed_open_cases,
   password_violation,
   priority_remove_failed_default_exist,
   form_without_pages,
   form_page_without_fields,
   accesspoint_already_exists,
   integrationpoint_already_exists,
   form_move_field_rule_violation,
   invalid_value;
}
