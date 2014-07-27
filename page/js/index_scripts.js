function aggiungiSerie(){
	var provider=selectProvider.options[selectProvider.selectedIndex].value;
	var serie;
	var serie=selectSerie.options[selectSerie.selectedIndex];
	operazioneInCorso("Invio richiesta per aggiungere "+serie.innerHTML+" alle serie preferite");
	$.ajax({
		type: "POST",
		url: "./OperazioniSerieServlet",
		data: "action=add&serie="+serie.value+"&provider="+provider,
		dataType: "xml",
		success: function(msg){
			var response=parseBooleanXML(msg);
			if(response){
				var elem = creaSerieElementoPagina(serie.innerHTML, serie.value, provider);
				$("#accordion").append(elem);
				//TODO aggiornamento serie
			}
		}
	});
}
function creaSerieElementoPagina(nome, id, provider) {
	var element = document.createElement("div");
	$(element).addClass("panel panel panel-default");
	element.id="serie"+id;
	element.innerHTML=
		"<div class='panel-heading'>"+
			"<h4 class='panel-title'>"+
				"<a data-toggle='collapse' data-parent='#accordion' href='#collapse"+id+"'>"+nome+"</a>"+
			"</h4>"+
			"<div class='buttonsAccordion'>"+
				"<button class='btn btn-warning' title='Aggiorna episodi' onclick='aggiornaEpisodi("+id+")'><span class='glyphicon glyphicon-refresh'></span></button>&nbsp;"+
				"<button class='btn btn-danger' title='Rimuovi dai preferiti' onclick='removeSerie("+id+")'><span class='glyphicon glyphicon-remove'></span></button>"+
			"</div>"+
			"<h5 id='episodiScaricare"+id+"'>(0 episodi da scaricare)</h5>"
		"</div>"+
	
		"<div id='collapse"+id+"One' class='panel-collapse collapse in'>"+
			"<div class='panel-body' id='listTorrent"+id+"'></div>"+
		"</div>";
	return element;
}
function caricaSerieFromProvider(){
	var provider = selectProvider.options[selectProvider.selectedIndex];
	if(provider.value ==null || provider.value==undefined || provider.value<=0){
		optSelectSerie.innerHTML="";
		serieNuoveDivContainer.innerHTML="";
		return;
	}
	operazioneInCorso("Invio richiesta serie per provider "+provider.innerHTML);
	$.ajax({
		type: "POST",
		url: "./OperazioniSerieServlet",
		data: "action=getSerieFromProvider&provider="+provider.value,
		dataType: "xml",
		success: function(msg){
			optSelectSerie.innerHTML="";
			var response = parseBooleanXML(msg);
			if(!response){
				operazioneInCorso("");
				showModal("","Si è verificato un errore durante il caricamento delle serie");
				return;
			}
			operazioneInCorso("Carico le serie");
			$(msg).find("serie").each(function(){
				var nome = $(this).find("name").text();
				var id = $(this).find("id").text();
				var provider = $(this).find("provider").text();
				var serie = document.createElement("option");
				serie.value = id;
				serie.provider = provider;
				serie.innerHTML = nome;
				$("#optSelectSerie").append(serie);
			});
			caricaSerieNuove(provider);
			operazioneInCorso("");
		},
		error: function(msg){
			operazioneInCorso("");
			showModal("","Si è verificato un errore durante il caricamento delle serie");
			return;
		}
	});
}
function caricaSerieNuove(provider){
	operazioneInCorso("Invio richiesta nuove serie");
	$.ajax({
		type: "POST",
		url: "./OperazioniSerieServlet",
		data: "action=getSerieNuoveFromProvider&provider="+provider.value,
		dataType: "xml",
		success: function(msg){
			operazioneInCorso("Carico le nuove serie di "+provider.innerHTML);
			var response = parseBooleanXML(msg);
			if(!response){
				showModal("","Si è verificato un errore durante il caricamento delle serie");
				return;
			}
			serieNuoveDivContainer.innerHTML="";
			$(msg).find("serie").each(function(){
				var nome = $(this).find("name").text();
				var id = $(this).find("id").text();
				var provider = $(this).find("provider").text();
				var serie = document.createElement("div");
				$(serie).addClass("panel-serieNuova");
				serie.innerHTML="<h4 class='panel-title'>"+nome+"</h4>"+
					"<div class='buttonsAccordion'>"+
						"<button class='btn btn-warning' title='Aggiungi' onclick='aggiungiSerie(id)'><span class='glyphicon glyphicon-plus'></span></button>&nbsp;"+
						"<button class='btn btn-warning' title='Info Serie' onclick='infoSerie(id)'><span class='glyphicon glyphicon-exclamation-sign'></span></button>"+
					"</div>";
				$("#serieNuoveDivContainer").append(serie);
			});
			operazioneInCorso("");
		},
		error: function(msg){
			operazioneInCorso("");
			showModal("","Si è verificato un errore durante il caricamento delle serie");
			return;
		}
	});
}
function caricaProvider(){
	operazioneInCorso("Invio richiesta provider");
	$.ajax({
		type: "POST",
		url: "./OperazioniSerieServlet",
		data: "action=getProviders",
		dataType: "xml",
		success: function(msg){
			operazioneInCorso("Carico i provider");
			groupProvider.innerHTML="";
			$(msg).find("provider").each(function(){
				var nome = $(this).find("name").text();
				var id = $(this).find("id").text();
				var provider = document.createElement("option");
				provider.value = id;
				provider.innerHTML = nome;
				$("#groupProvider").append(provider);
			});
			operazioneInCorso("");
		},
		error: function(msg){
			operazioneInCorso("");
			showModal("Si è verificato un errore durante il caricamento dei provider");
		}
	});
}
function parseBooleanXML(xml){
	var valore = $(xml).find("booleanResponse").text();
	if(valore=='true')
		return true;
	else
		return false;
}
function download(){
	
}
function ignora(){ 
	
}
function aggiornaSerie(bottone) {
	$(bottone).prop("disabled","true");
	var provider = selectProvider.options[selectProvider.selectedIndex];
	operazioneInCorso("Aggiorno l'elenco delle serie tv di "+provider.innerHTML);
	$.ajax({
		type: "POST",
		url: "./OperazioniSerieServlet",
		data: "action=updateListSeries&provider="+provider.value,
		dataType: "xml",
		success: function(msg){
			if(parseBooleanXML(msg)){
				operazioneInCorso("Aggiornamento dell'elenco delle serie completato con successo");
				//TODO caricamento serie
			}
			else
				showModal("","Si è verificato un errore durante l'aggiornamento");
			operazioneInCorso("");
			$(bottone).removeAttr("disabled");
		},
		error: function(msg){
			operazioneInCorso("");
			showModal("Si è verificato un errore durante l'aggiornamento");
			$(bottone).removeAttr("disabled");
		}
	});
}
function selezionaTutto(){
	
}
function operazioneInCorso(messaggio){
	if(messaggio.length>0)
		divMessaggioOperazione.innerHTML="<img src='img/loading.gif' height='16' width='16'/>&nbsp;"+messaggio;
	else
		divMessaggioOperazione.innerHTML="";
}
function loadSeriePreferite(){
	operazioneInCorso("Invio richiesta mie serie preferite");
	$.ajax({
		type: "POST",
		url: "./OperazioniSerieServlet",
		data: "action=getSeriePreferite",
		dataType: "xml",
		success: function(msg){
			operazioneInCorso("Caricando le serie preferite");
			$(msg).find("serie").each(function(){
				var nome = $(this).find("name").text();
				var id = $(this).find("id").text();
				var provider = $(this).find("provider").text();
				var elem = creaSerieElementoPagina(nome, id, provider);
				$("#accordion").append(elem);
			});
			operazioneInCorso("");
		},
		error: function(msg){
			operazioneInCorso("");
			showModal("Si è verificato un errore durante l'aggiornamento");
		}
	});
}
function removeSerie(id){
	$.ajax({
		type: "POST",
		url: "./OperazioniSerieServlet",
		data: "action=remove&id="+id,
		dataType: "xml",
		success: function(msg){
			var r = parseBooleanXML(msg);
			if(r){
				$("#serie"+id).remove();
			}
			operazioneInCorso("");
		},
		error: function(msg){
			operazioneInCorso("");
			showModal("Si è verificato un errore durante l'aggiornamento");
		}
	});
}
function aggiornaEpisodi(id) {
	
}