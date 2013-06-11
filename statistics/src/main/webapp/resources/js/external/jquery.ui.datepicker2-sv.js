
/* Swedish initialisation for the jQuery UI date picker plugin. */
/* Written by Anders Ekdahl ( anders@nomadiz.se). */
jQuery(function($){
    $.datepicker.regional['sv'] = {
		closeText: 'St&auml;ng',
        prevText: '&laquo;F&ouml;rra',
		nextText: 'Nästa&raquo;',
		currentText: 'Idag',
        monthNames: ['Januari','Februari','Mars','April','Maj','Juni',
        'Juli','Augusti','September','Oktober','November','December'],
        monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
        'Jul','Aug','Sep','Okt','Nov','Dec'],
		dayNamesShort: ['S&ouml;n','M&aring;n','Tis','Ons','Tor','Fre','L&ouml;r'],
		dayNames: ['S&ouml;ndag','M&aring;ndag','Tisdag','Onsdag','Torsdag','Fredag','L&ouml;rdag'],
		dayNamesMin: ['S&ouml;','M&aring;','Ti','On','To','Fr','L&ouml;'],
		weekHeader: 'Ve',
        dateFormat: 'yy-mm-dd',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
    $.datepicker.setDefaults($.datepicker.regional['sv']);
});