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
