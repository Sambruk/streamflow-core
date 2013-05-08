/*
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
$(document).ready(function() {
	"use strict";

	$(".expand-all-labels").click(function() {
		var expander = $(this);
		var allExpanders = $(".expand-labels");
		if (expander.text() === "+") {
			expander.text("-");
			allExpanders.text("-");
			expander.attr("title", "Dölj alla etiketter");
			allExpanders.attr("title", "Dölj etiketter");
			$(".label-stats").show("fast");
		} else {
			expander.text("+");
			allExpanders.text("+");
			expander.attr("title", "Visa alla etiketter");
			allExpanders.attr("title", "Visa etiketter");
			$(".label-stats").hide("fast");
		}
	});

	$(".expand-labels").click(function() {
		var expander = $(this);
		if (expander.text() === "+") {
			expander.text("-");
			expander.attr("title", "Dölj etiketter");
			$(this).parent().parent().nextUntil("tr.casetype-stats").show("fast");
		} else {
			expander.text("+");
			expander.attr("title", "Visa etiketter");
			$(this).parent().parent().nextUntil("tr.casetype-stats").hide("fast");
		}
	});
});
