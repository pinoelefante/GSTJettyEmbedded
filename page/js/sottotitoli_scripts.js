$(document).ready(function() {
	loadSeriePreferite();
	loadProviders();
	chosenConfig();
});
function chosenConfig(){
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
}
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
function loadProviders(){
	$.ajax({
		type : "POST",
		url : "./OperazioniSottotitoliServlet",
		data : "action=getProviders",
		dataType : "xml",
		success : function(msg) {
			$(msg).find("provider").each(function() {
				var nome = $(this).find("name").text();
				var id_provider = $(this).attr("id_provider");
				var select;
				switch(id_provider){
					case "1":
						select=$("#selectItasa");
						break;
					case "2":
						select=$("#selectSubsfactory");
						break;
					case "3":
						select=$("#selectSubspedia");
						break;
					case "4":
						select=$("#selectPodnapisi");
						break;
					case "5":
						select=$("#selectOpensubtitles");
						break;
					case "6":
						select=$("#selectIAddic7ed");
						break;
					default:
						alert("DEFAULT CASE!!!!");
				}
				$(this).find("serie").each(function(){
					var nomeSerie = $(this).find("nome").text();
					var idSerie = $(this).find("id").text();
					var option = document.createElement("option");
					option.value = idSerie;
					option.innerHTML = nomeSerie;
					
					$(option).appendTo(select);
				});
				$(select).trigger("chosen:updated");
			});
			
		},
		error : function(msg) {
			showModal("","Si è verificato un errore durante l'aggiornamento");
		}
	});
}
function onchangePreferita(val){
	var serie = $("#seriePreferite option:selected");
	if(serie.length==0 || serie == null || serie.value == "undefined"){
		alert("Seleziona serie");
		return;
	}
	
	var itasa=parseInt($(serie).prop("itasa"));
	if(itasa>0)
		$('#selectItasa option[value="' + itasa + '"]').prop('selected', true);
	else
		$("#selectItasa option.noValue").prop('selected', true);
	$("#selectItasa").trigger("chosen:updated");
	$("#selectItasa").trigger("change");
	
	var subsfactory=parseInt($(serie).prop("subsfactory"));
	if(subsfactory>0)
		$('#selectSubsfactory option[value="' + subsfactory + '"]').prop('selected', true);
	else 
		$("#selectSubsfactory option.noValue").prop('selected', true);
	$("#selectSubsfactory").trigger("chosen:updated");
	$("#selectSubsfactory").trigger("change");
	
	var subspedia=parseInt($(serie).prop("subspedia"));
	if(subspedia>0)
		$('#selectSubspedia option[value="' + subspedia + '"]').prop('selected', true);
	else 
		$("#selectSubspedia option.noValue").prop('selected', true);
	$("#selectSubspedia").trigger("chosen:updated");
	$("#selectSubspedia").trigger("change");
}