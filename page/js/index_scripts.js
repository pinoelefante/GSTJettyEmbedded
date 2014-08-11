$(document).ready(function() {
	caricaProvider();
	loadSeriePreferite();
	caricaSerieNuove();
});
function aggiungiSerie(p, s, nomeSerie) {
	var provider = (p == null ? selectProvider.options[selectProvider.selectedIndex].value : p);
	var serie = (s == null ? selectSerie.options[selectSerie.selectedIndex].value : s);
	var nome = (nomeSerie == null ? selectSerie.options[selectSerie.selectedIndex].innerHTML : nomeSerie);
	operazioneInCorso("Aggiungo la serie alle preferite");
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=add&serie=" + serie + "&provider=" + provider,
		dataType : "xml",
		success : function(msg) {
			var response = parseBooleanXML(msg);
			if (response) {
				var elem = creaSerieElementoPagina(nome, serie, provider);
				addSerieInOrder(elem, nome);
				operazioneInCorso("");
				aggiornaEpisodi(serie, provider);
			}
			else {
				showModal("Serie non aggiunta");
			}
			operazioneInCorso("");
		}
	});
}
function addSerieInOrder(elem, nomeSerie) {
	var inserita = false;
	$(".seriePreferita").each(function() {
		if (inserita)
			return;
		var nome = $(this).find("a").text();
		if (nomeSerie < nome) {
			$(elem).insertBefore(this);
			inserita = true;
			return;
		}
	});
	if (inserita == false)
		$("#accordion").append(elem);
}
function creaSerieElementoPagina(nome, id, provider) {
	var element = "<div class='panel panel-default seriePreferita' id='serie" + id + "'>" + "<div class='panel-heading'>" + "<h4 class='panel-title'>" + "<a data-toggle='collapse' data-parent='#accordion' href='#collapse" + id + "'>" + nome + "</a>" + "</h4>" + "<div class='buttonsAccordion'>" + "<button class='btn btn-warning' title='Aggiorna episodi' onclick='aggiornaEpisodi(" + id + "," + provider + ")'><span class='glyphicon glyphicon-refresh'></span></button>&nbsp;"+ "<button class='btn btn-warning' title='Info sulla serie' onclick='infoSerie("+id+")'><span class='glyphicon glyphicon-info-sign' /></button>&nbsp;" + "<button class='btn btn-danger' title='Rimuovi dai preferiti' onclick='removeSerie(" + id + ")'><span class='glyphicon glyphicon-remove'></span></button>" + "</div>" + "<h5 id='episodiScaricare" + id + "'>(0 episodi da scaricare)</h5>" + "</div>" + "<div id='collapse" + id + "' class='panel-collapse collapse'>" + "<div class='panel-body'><div class='panel-group' id='accordion" + id + "'></div></div>" + "</div>" + "</div>";
	return element;
}
function caricaSerieByProvider(provider) {
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=getSerieFromProvider&provider=" + provider,
		dataType : "xml",
		success : function(msg) {
			optSelectSerie.innerHTML = "";
			var response = parseBooleanXML(msg);
			if (!response) {
				operazioneInCorso("");
				showModal("", "Si è verificato un errore durante il caricamento delle serie");
				return;
			}
			operazioneInCorso("Carico le serie");
			$(msg).find("serie").each(function() {
				var nome = $(this).find("name").text();
				var id = $(this).find("id").text();
				var provider = $(this).find("provider").text();
				var serie = document.createElement("option");
				serie.value = id;
				serie.provider = provider;
				serie.innerHTML = nome;
				$("#optSelectSerie").append(serie);
			});
			//caricaSerieNuove(provider);
			operazioneInCorso("");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("", "Si è verificato un errore durante il caricamento delle serie");
			return;
		}
	});
}
function caricaSerieFromProvider() {
	var provider = selectProvider.options[selectProvider.selectedIndex];
	if (provider.value == null || provider.value == undefined || provider.value <= 0) {
		optSelectSerie.innerHTML = "";
		return;
	}
	operazioneInCorso("Invio richiesta serie per provider " + provider.innerHTML);
	caricaSerieByProvider(provider.value);
}
function caricaSerieNuove(){
	operazioneInCorso("Invio richiesta nuove serie");
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=getSerieNuove",
		dataType : "xml",
		success : function(msg) {
			var response = parseBooleanXML(msg);
			if (!response) {
				showModal("", "Si è verificato un errore durante il caricamento delle serie");
				return;
			}
			serieNuoveDivContainer.innerHTML = "";
			$(msg).find("serie").each(function() {
				var nome = $(this).find("name").text();
				var id = $(this).find("id").text();
				var provider = $(this).find("provider").text();
				var serie = document.createElement("div");
				$(serie).addClass("panel-serieNuova");
				serie.innerHTML = "<h4 class='panel-title'>" + nome + "</h4>" + "<div class='buttonsAccordion'>" + "<button class='btn btn-warning' title='Aggiungi' onclick=\"aggiungiSerie("+provider+","+id+",'"+nome.replace("'","\\'")+"')\"><span class='glyphicon glyphicon-plus'></span></button>&nbsp;" + "<button class='btn btn-warning' title='Info Serie' onclick='infoSerie("+id+")'><span class='glyphicon glyphicon-info-sign'></span></button>" + "</div>";
				$("#serieNuoveDivContainer").append(serie);
			});
			operazioneInCorso("");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("", "Si è verificato un errore durante il caricamento delle serie");
			return;
		}
	});
}
/*
function caricaSerieNuove(provider) {
	operazioneInCorso("Invio richiesta nuove serie");
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=getSerieNuoveFromProvider&provider=" + provider,
		dataType : "xml",
		success : function(msg) {
			var response = parseBooleanXML(msg);
			if (!response) {
				showModal("", "Si è verificato un errore durante il caricamento delle serie");
				return;
			}
			serieNuoveDivContainer.innerHTML = "";
			$(msg).find("serie").each(function() {
				var nome = $(this).find("name").text();
				var id = $(this).find("id").text();
				var provider = $(this).find("provider").text();
				var serie = document.createElement("div");
				$(serie).addClass("panel-serieNuova");
				serie.innerHTML = "<h4 class='panel-title'>" + nome + "</h4>" + "<div class='buttonsAccordion'>" + "<button class='btn btn-warning' title='Aggiungi' onclick=\"aggiungiSerie("+provider+","+id+",'"+nome.replace("'","\\'")+"')\"><span class='glyphicon glyphicon-plus'></span></button>&nbsp;" + "<button class='btn btn-warning' title='Info Serie' onclick='infoSerie("+id+")'><span class='glyphicon glyphicon-info-sign'></span></button>" + "</div>";
				$("#serieNuoveDivContainer").append(serie);
			});
			operazioneInCorso("");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("", "Si è verificato un errore durante il caricamento delle serie");
			return;
		}
	});
}
*/
function caricaProvider() {
	operazioneInCorso("Invio richiesta provider");
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=getProviders",
		dataType : "xml",
		success : function(msg) {
			operazioneInCorso("Carico i provider");
			groupProvider.innerHTML = "";
			$(msg).find("provider").each(function() {
				var nome = $(this).find("name").text();
				var id = $(this).find("id").text();
				var provider = document.createElement("option");
				provider.value = id;
				provider.innerHTML = nome;
				$("#groupProvider").append(provider);
			});
			operazioneInCorso("");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("Si è verificato un errore durante il caricamento dei provider");
		}
	});
}
function parseBooleanXML(xml) {
	var valore = $(xml).find("booleanResponse").text();
	if (valore == 'true')
		return true;
	else
		return false;
}
function download() {
	$("#accordion").find("input[type=checkbox]").each(function(){
		if($(this).is(":checked")){
			var idEp = $(this).val();
			downloadS(idEp);
		}
	}); 
}

function ignora() {
	
	 location.reload(); 
}
function aggiornaSerie(bottone) {
	$(bottone).prop("disabled", "true");
	var provider = selectProvider.options[selectProvider.selectedIndex];
	if (provider == null)
		return;
	operazioneInCorso("Aggiorno l'elenco delle serie tv di " + provider.innerHTML);
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=updateListSeries&provider=" + provider.value,
		dataType : "xml",
		success : function(msg) {
			if (parseBooleanXML(msg)) {
				operazioneInCorso("Aggiornamento dell'elenco delle serie completato con successo");
				caricaSerieByProvider(provider.value);
			}
			else
				showModal("", "Si è verificato un errore durante l'aggiornamento");
			operazioneInCorso("");
			$(bottone).removeAttr("disabled");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("Si è verificato un errore durante l'aggiornamento");
			$(bottone).removeAttr("disabled");
		}
	});
}

function selezionaTutto(selected) {
	$("#accordion").find("input[type=checkbox]").each(function(){
		if(selected)
			$(this).prop('checked', true);
		else
			$(this).removeAttr('checked');
	});
}
function selezionaPerStato(stato){
	$("#accordion").find("input[type=checkbox]").each(function(){
		if($(this).attr("stato_visualizzazione")==stato)
			$(this).prop('checked', true);
		else
			$(this).removeAttr('checked');
	});
}
function operazioneInCorso(messaggio) {
	if (messaggio.length > 0)
		divMessaggioOperazione.innerHTML = "<img src='img/loading.gif' height='16' width='16'/>&nbsp;" + messaggio;
	else
		divMessaggioOperazione.innerHTML = "";
}
function loadSeriePreferite() {
	operazioneInCorso("Invio richiesta mie serie preferite");
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=getSeriePreferite",
		dataType : "xml",
		success : function(msg) {
			var arrayID=new Array();
			var i=0;
			operazioneInCorso("Caricando le serie preferite");
			$(msg).find("serie").each(function() {
				var nome = $(this).find("name").text();
				var id = $(this).find("id").text();
				var provider = $(this).find("provider").text();
				var elem = creaSerieElementoPagina(nome, id, provider);
				$("#accordion").append(elem);
				arrayID[i]=id;
				i++;
			});
			for(var j=0;j<i;j++)
				getEpisodi(arrayID[j]);
			operazioneInCorso("");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("Si è verificato un errore durante l'aggiornamento");
		}
	});
}
function removeSerie(id) {
	$.ajax({
	type : "POST",
	url : "./OperazioniSerieServlet",
	data : "action=remove&id=" + id,
	dataType : "xml",
	success : function(msg) {
		var r = parseBooleanXML(msg);
		if (r) {
			$("#serie" + id).remove();
		}
		operazioneInCorso("");
	},
	error : function(msg) {
		operazioneInCorso("");
		showModal("Si è verificato un errore durante l'aggiornamento");
	}
	});
}
function aggiornaEpisodi(id, provider) {
	operazioneInCorso("Aggiorno episodi serie");
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=updateTorrents&serie=" + id + "&provider=" + provider,
		dataType : "xml",
		success : function(msg) {
			var r = parseBooleanXML(msg);
			operazioneInCorso("");
			getEpisodi(id);
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("Si è verificato un errore durante l'aggiornamento");
		}
	});
}
function getClassStatus(stato){
	switch(stato){
		case 0:
			return " episodioDaScaricare";
		case 2:
			return " episodioVisto";
		case 1:
			return " episodioDaVedere";
		case 3:
			return " episodioRimosso";
		case 4:
			return " episodioIgnorato";
	}
}
function getEpisodi(id) {
	operazioneInCorso("Carico l'elenco degli episodi da scaricare");
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=getEpisodiBySerie&serie=" + id,
		dataType : "xml",
		success : function(msg) {
			var daScaricare = 0;
			var daVedere = 0;
			var visualizzati = 0;
			var ignorati = 0;
			var rimossi = 0;
			var numEpisodi = 0;
			$(msg).find("ep").each(function() {
				numEpisodi++;
				var stagione = $(this).find("stagione").text();
				var episodio = $(this).find("episodio").text();
				var idE = $(this).find("id_episodio").text();
				var stato = parseInt($(this).find("stato").text());
				
				switch(stato){
				case 0:
					daScaricare++;
					break;
				case 1:
					daVedere++;
					break;
				case 2:
					visualizzati++;
					break;
				case 3:
					rimossi++;
					break;
				case 4:
					ignorati++;
					break;
				}
				
				var html="<div class='episodio"+getClassStatus(stato)+"' id='divEP_"+idE+"'>"+
					"<input type='checkbox' value='" + idE + "' id='chkEp_"+idE+"' stato_visualizzazione='"+stato+"'> Episodio <b>" + (episodio == 0 ? "Speciale" : episodio) + "</b></input>" +
					"<div class='episodioButtons'>" +
					generaBottone(stato,idE) +"&nbsp;" +
					"<button class='btn btn-warning' title='Info episodio' onclick='infoEpisodio("+idE+")'><span class='glyphicon glyphicon-info-sign'/></button>&nbsp;" +
					"<button class='btn btn-danger' title='Cancella episodio' onclick='cancellaEpisodio("+idE+")'><span class='glyphicon glyphicon-trash'/></button>&nbsp;" +
					"" +
					"" +
					"</div>" +
					"</div>";
				
	
				var accordionSerie = $("#accordion" + id);
				var accordionStagione = $("#listTorrent" + id + "_" + stagione);
				if (accordionStagione.length == 0) {
					var elemToCreate = createAccordionStagione(stagione, id);
					$(elemToCreate).appendTo(accordionSerie);
					accordionStagione = $("#listTorrent" + id + "_" + stagione);
				}
				$(html).appendTo(accordionStagione);
			});
			if(daScaricare == 0){
				$("#episodiScaricare" + id)	.text("(Nessun episodo da scaricare. Da vedere: "+daVedere+")");
			}
			else {
				$("#episodiScaricare" + id)	.text("(" + daScaricare + " episodi da scaricare. Da vedere: "+daVedere+")");
			}
			selezionaTutto();
			operazioneInCorso("");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("Si è verificato un errore durante l'aggiornamento");
		}
	});
}
function generaBottone(stato, id){
	if(stato==0||stato==3||stato==4)
		return "<button id='btnDown_"+id+"' class='btn btn-primary' title='Scarica' onclick='downloadS("+id+")'><span class='glyphicon glyphicon-download-alt'/></button>";
	else
		return "<button id='btnPlay_"+id+"' class='btn btn-primary' title='Play' onclick='play("+id+")'><span class='glyphicon glyphicon-play' /></button>";
}
function createAccordionStagione(stagione, idserie) {
	var elem = "<div class='panel panel-default'>" + "<div class='panel-heading'>" + "<h4 class='panel-title'>" + "<a data-toggle='collapse' data-parent='#accordion" + idserie + "' href='#collapse" + idserie + "_" + stagione + "'> " + (stagione == 0 ? "Speciali" : "Stagione " + stagione) + " </a>" + "</h4>" + "</div>" + "<div id='collapse" + idserie + "_" + stagione + "' class='panel-collapse collapse'>" + "<div class='panel-body' id='listTorrent" + idserie + "_" + stagione + "'></div>" + "</div>" + "</div>";
	return elem;
}
function infoSerie(id){
	
}
function infoEpisodio(idEp){
	
}
function downloadS(id){
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=download&episodio=" + id,
		dataType : "xml",
		success : function(msg) {
			var resp = parseBooleanXML(msg);
			if(resp){
				var idEpisodio = parseInt($(msg).find("episodio").text());
				$("#btnDown_"+id).replaceWith(generaBottone(1, id));
				if($("#chkSelezionaDown").is(":checked") && $("#chkEp_"+id).is(":checked")){
					$("#chkEp_"+id).removeAttr('checked');
				}
				$("#chkEp_"+id).attr("stato_visualizzazione","1");
				
				if(!$("#divEP_"+id).hasClass("episodioVisto")){
					$("#divEP_"+id).addClass("episodioDaVedere");
				}
			}
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("Si è verificato un errore durante il download");
		}
	});
}
function play(id) {
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=play&episodio=" + id,
		dataType : "xml",
		success : function(msg) {
			var r = parseBooleanXML(msg);
			if(r){
				$("#divEP_"+id).removeClass("episodioDaVedere");
				$("#divEP_"+id).addClass("episodioVisto");
				$("#chkEp_"+id).attr("stato_visualizzazione","2");
			}
			else {
				$("#divEP_"+id).removeClass("episodioDaVedere");
				$("#divEP_"+id).addClass("episodioRimosso");
				$("#chkEp_"+id).attr("stato_visualizzazione","3");
				var bottone = generaBottone(3, id);
				$("#btnPlay_"+id).replaceWith(bottone);
				showModal("File non trovato");
			}
		},
		error : function(msg) {
			
		}
	});
}
function cancellaEpisodio(id){
	
}
