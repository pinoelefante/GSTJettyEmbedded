$(document).ready(function() {
	chosenConfig();
});
function chosenConfig(){
	var config = {
		'.chosen-select'           : {},
		'max_selected_options'	   : 5,
		'.chosen-select-deselect'  : {allow_single_deselect:true},
		'.chosen-select-no-single' : {disable_search_threshold:10},
		'.chosen-select-no-results': {no_results_text:'Oops, nothing found!'},
		'.chosen-select-width'     : {width:"100%"},
	}
	for (var selector in config) {
		$(selector).chosen(config[selector]);
	}
}