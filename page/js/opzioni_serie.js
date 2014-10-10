$(document).ready(function() {
	chosenConfig();
	loadSeriePreferite();
});
function chosenConfig(){
	var config = {
		'.chosen-select'           : {},
		'max_selected_options'	   : 5,
		'.chosen-select-deselect'  : {allow_single_deselect:true},
		'.chosen-select-no-single' : {disable_search_threshold:10},
		'.chosen-select-no-results': {no_results_text:'Oops, nothing found!'},
		'.chosen-select-width'     : {width:"350px"},
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
				var noselect = $(this).find("no_select").text();
				var pref_subs = $(this).find("pref_subs").text();
				var elem = creaSerieElementoPagina("<b>"+nome+"</b> - "+provider_name, id, provider, noselect, pref_subs, 0);
				$("#accordion").append(elem);
				$("#langs_"+id).trigger("chosen:updated");
			});
			//;
			$("select[id^='langs_']").each(function(){
				$(this).addClass("chosen-select");
				
				//$(this).trigger("chosen-updated");
			});
			chosenConfig();
		},
		error : function(msg) {
			showModal("","Si è verificato un errore durante l'aggiornamento");
		}
	});
}
function creaSerieElementoPagina(nome, id, provider, noselect, langs, preferenzeDown) {
	var element = "<div class='panel panel-default seriePreferita' id='serie" + id + "'>" + 
					"<div class='panel-heading'>" + 
						"<h4 class='panel-title'>" + 
							"<a class='nomeSerie' data-toggle='collapse' data-parent='#accordion' href='#collapse" + id + "'>" + nome + "</a>" +
							"<button style='float:right' class='btn btn-success' title='Salva impostazioni' onclick='salvaImpostazioni("+id+")'><span class='glyphicon glyphicon glyphicon-ok'></span></button>&nbsp;"+
						"</h4>" + 
					"</div>" +
					"<div id='collapse" + id + "' class='panel-collapse collapse'>" +
						"<div class='panel-body'>"+
							"<div class='panel-group' id='accordion" + id + "'>"+
								"<p><input type='checkbox' id='escludi_"+id+"' "+(noselect=="true"?"checked":"")+" /> Escludi da seleziona episodi da scaricare</p>" +
								"<div>Lingue sottotitoli: "+
								"<select id='langs_"+id+"' data-placeholder='Seleziona lingua' style='width:350px' multiple>" +
                    	    		"<option value='de' "+(isLinguaSelected(langs, "de")?"selected":"")+">Tedesco</option>"+
                    	    		"<option value='fr' "+(isLinguaSelected(langs, "fr")?"selected":"")+">Francesce</option>"+
                    	    		"<option value='pr' "+(isLinguaSelected(langs, "pr")?"selected":"")+">Portoghese</option>"+
                    	    		"<option value='it' "+(isLinguaSelected(langs, "it")?"selected":"")+">Italiano</option>"+
                    	    		"<option value='en' "+(isLinguaSelected(langs, "en")?"selected":"")+">Inglese</option>"+
                    	    		"<option value='es' "+(isLinguaSelected(langs, "es")?"selected":"")+">Spagnolo</option>"+
                    			"</select>"+
								"</div>" +
								"<p>Preferenze download qui</p>"+
							"</div>" +
						"</div>" +
					"</div>" +
				"</div>";
	return element;
}
function isLinguaSelected(lingue, lingua){
	var lingueS = lingue.split("|");
	for(var i=0;i<lingueS.length;i++){
		if(lingueS[i]==lingua)
			return true;
	}
	return false;
}
function salvaImpostazioni(id){
	var escludi = $("#escludi_"+id).is(":checked");
	var lingue = $("#langs_"+id).val(); //array
	var lingue_s = "";
	for(var i=0;i<lingue.length;i++){
		lingue_s+=lingue[i];
		if(i<lingue.length-1)
			lingue_s+="|";
	}
	if(lingue_s.length==0)
		lingue_s="null";
	
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=modificaPreferenzeSerie&id="+id+"&escludi="+escludi+"&lingue_sub="+lingue_s,
		dataType : "xml",
		success : function(msg) {
			var r = parseBooleanXML(msg);
			if(r)
				showModal("","Preferenze salvate correttamente");
			else
				showModal("","Si è verificato un errore");
		},
		error : function(msg) {
			showModal("","Si è verificato un errore durante l'aggiornamento");
		}
	});
}