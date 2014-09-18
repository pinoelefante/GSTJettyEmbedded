$(document).ready(function() {
	loadSeriePreferite();
	
	var config = {
		'.chosen-select'           : {},
		'.chosen-select-deselect'  : {allow_single_deselect:true},
		'.chosen-select-no-single' : {disable_search_threshold:10},
		'.chosen-select-no-results': {no_results_text:'Oops, nothing found!'},
		'.chosen-select-width'     : {width:"95%"}
	}
	for (var selector in config) {
		$(selector).chosen(config[selector]);
	}
});
function loadSeriePreferite() {
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=getSeriePreferite",
		dataType : "xml",
		success : function(msg) {
			$(msg).find("serie").each(function() {
				var nome = $(this).find("name").text();
				var id = $(this).find("id").text();
				var provider = $(this).find("provider").text();
				var provider_name = $(this).find("provider_name").text();
				var itasa = $(this).find("id_itasa").text();
				var subsfactory = $(this).find("id_subsfactory").text();
				var subspedia = $(this).find("id_subspedia").text();
				
				var option = document.createElement("option");
				option.value=id;
				option.innerHTML=nome;
				option.provider=provider;
				option.itasa=itasa;
				option.subsfactory=subsfactory;
				option.subspedia=subspedia;
				
				$(option).appendTo("#seriePreferite");
			});
			$("#seriePreferite").trigger("chosen:updated");
			$("#seriePreferite").trigger("change");
		},
		error : function(msg) {
			showModal("","Si è verificato un errore durante l'aggiornamento");
		}
	});
}
function onchangePreferita(val){
	
}