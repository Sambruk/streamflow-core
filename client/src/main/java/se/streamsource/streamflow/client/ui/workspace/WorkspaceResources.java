/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.workspace;

/**
 * i18n resources for the "Workspace" view
 */
public enum WorkspaceResources
{
   could_not_open_file,
   could_not_refresh,

   window_name,
   select_account,
   welcome,
   choose_case,

   // Case table
   title_column_header,
   info_column_header,
   casetype_column_header,
   created_column_header,
   created_by_column_header,
   case_status_header,
   assignee_column_header,
   duedate_column_header,
   project_column_header,

   // Case info
   assigned_to_header,
   owner,
   created_by,

   // Case details
   general_tab,
   forms_tab,
   conversations_tab,
   contacts_tab,
   attachments_tab,

   // General view
   description_label,
   note_label,
   name_label,
   contact_id_label,
   company_label,
   due_on_label,
   phone_label,
   email_label,
   address_label,
   zip_label,
   city_label,
   region_label,
   country_label,
   forms_label,
   choose_casetype,
   case_log,

   // Form submission
   mandatory_field_missing,

   // Forms tab
   submitted_forms_tab,
   signatures,

   // Attachments tab
   attachment,
   attachment_name,
   attachment_size,
   create_attachment,
   attachments,
   could_not_open_attachment,

   // Context selection
   inboxes_node,
   drafts_node,
   search_node,
   perspectives_separator,

   assignments_node,

   date_time_format,
   choose_owner_title,
   change_password_title,
   choose_message_delivery_type,
   choose_project,
   choose_form,
   save_perspective,
   query_label,
   incomplete_data,
   manage_perspectives,
   case_separator,
   caze,
   too_long_query,
   choose_template,
   could_not_print,
   date_format,
   date_separator,
   wrong_format_msg,
   wrong_format_title,

   // CasesTableView sorting and grouping
   search,
   status,

   filter,
   grouping,

   all,
   label,
   assignee,
   sorting,
   project,
   created_on,

   // Due date grouping
   overdue,
   duetoday,
   duetomorrow,
   duenextweek,
   duenextmonth,
   later,
   none,
   case_type,
   noduedate,

   // CaseStates
   OPEN,
   ON_HOLD,
   choose_date, CLOSED,

   // Conversation
   sender_column_header,
   message_column_header,
   change_perspective_title,
   selected_projects,
   no_casetype,
   no_assignee,
   no_project,
   search_period,
   printing_configuration,
   caze_reinstate, priority_label, case_priority_header, no_priority;
}