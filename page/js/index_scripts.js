$(document).ready(function() {
	$('#tallModal').on('hidden.bs.modal', function () {
	    $("#modalDownloadButton").addClass("hidden");
	    $("#modalTVDBUpdate").addClass("hidden");
	});
	loadSeriePreferite();
	caricaSerieNuove();
	getEpisodiDaVedere();
	GetCountToDownload();
	caricaElencoSerieCompleto();
	bootbox.setDefaults({
		locale: "it"
	});
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
function aggiungiSerieBottone(){
	var serie = selectSerie.options[selectSerie.selectedIndex];
	
	if(serie==null || serie=="" || serie=="undefined" || serie.length==0 || serie.value==""){
		showModal("","Devi selezionare una serie da aggiungere");
		return;
	}
	
	var id_serie = serie.value;
	var nome = serie.nomeSerie;
	var provider = serie.provider;
	
	operazioneInCorso("Aggiungo la serie alle preferite");
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=add&serie=" + id_serie + "&provider=" + provider,
		dataType : "xml",
		success : function(msg) {
			var response = parseBooleanXML(msg);
			if (response) {
				var text="<b>"+nome+"</b> - "+serie.providerNome;
				var elem = creaSerieElementoPagina(text, id_serie, provider);
				addSerieInOrder(elem, text);
				operazioneInCorso("");
				aggiornaEpisodi(id_serie, provider);
			}
			else {
				showModal("","Serie non aggiunta");
			}
			operazioneInCorso("");
		},
		error : function(msg){
			showModal("Si è verificato un errore");
			operazioneInCorso("");
		}
	});
}
function aggiungiSerie(provider, serie, nome) {
	if(serie==null || serie=="" || serie <=0){
		showModal("","Devi selezionare una serie da aggiungere");
		return;
	}
	if(provider==null || provider=="" || provider<=0){
		showModal("","La serie selezionata non ha un provider settato");
		return;
	}

	operazioneInCorso("Aggiungo la serie alle preferite");
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=add&serie=" + serie,
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
				showModal("","Serie non aggiunta");
			}
			operazioneInCorso("");
		},
		error : function(msg){
			showModal("Si è verificato un errore");
			operazioneInCorso("");
		}
	});
}
function addSerieInOrder(elem, nomeSerie) {
	var inserita = false;
	$(".seriePreferita").each(function() {
		if (inserita)
			return;
		var nome = $(this).find("a.nomeSerie").text();
		if ($(nomeSerie).text() < nome) {
			$(elem).insertBefore(this);
			inserita = true;
			return;
		}
	});
	if (inserita == false)
		$("#accordion").append(elem);
}
function creaSerieElementoPagina(nome, id, provider, noselect) {
	var element = "<div class='panel panel-default seriePreferita' id='serie" + id + "'>" + "<div class='panel-heading'>" + "<h4 class='panel-title'>" + "<a class='nomeSerie' data-toggle='collapse' data-parent='#accordion' href='#collapse" + id + "' onclick='getEpisodi("+id+","+noselect+")'>" + nome + "</a>" + "</h4>" + "<div class='buttonsAccordion'>" + "<button class='btn btn-warning' title='Aggiorna episodi' onclick='aggiornaEpisodi(" + id + "," + provider + ")'><span class='glyphicon glyphicon-refresh'></span></button>&nbsp;"+ "<button class='btn btn-warning' title='Info sulla serie' onclick='infoSerie("+id+")'><span class='glyphicon glyphicon-info-sign' /></button>&nbsp;"+"<button class='btn btn-warning' title='Apri cartella' onclick='openFolder(" + id + ")'><span class='glyphicon glyphicon-folder-open'></span></button>&nbsp;" + "<button class='btn btn-danger' title='Rimuovi dai preferiti' onclick='removeSerie(" + id + ")'><span class='glyphicon glyphicon-remove'></span></button>" + "</div>" + "<h5 id='episodiScaricare" + id + "'>(0 episodi da scaricare)</h5>" + "</div>" + "<div id='collapse" + id + "' class='panel-collapse collapse'>" + "<div class='panel-body'><div class='panel-group' id='accordion" + id + "'></div></div>" + "</div>" + "</div>";
	return element;
}
function openFolder(id){
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=openFolder&id="+id,
		dataType : "xml",
		success : function(msg) {
			selectSerie.innerHTML = "<option selected></option>";
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
				var provider_name = $(this).find("provider_name").text();
				var serie = document.createElement("option");
				serie.value = id;
				serie.provider = provider;
				serie.providerNome=provider_name;
				serie.nomeSerie = nome;
				serie.innerHTML = nome+"<span style='float:right'> ("+provider_name+")</span>";//"<b>"+nome+"</b> - "+provider_name;
				$("#selectSerie").append(serie);
			});
			$("#selectSerie").trigger("chosen:updated");
			operazioneInCorso("");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("", "Si è verificato un errore durante il caricamento delle serie");
			return;
		}
	});
}
function caricaElencoSerieCompleto(){
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=getElencoSerie",
		dataType : "xml",
		success : function(msg) {
			selectSerie.innerHTML = "<option selected></option>";
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
				var provider_name = $(this).find("provider_name").text();
				var no_select = $(this).find("no_select").text();
				var serie = document.createElement("option");
				serie.value = id;
				serie.provider = provider;
				serie.providerNome=provider_name;
				serie.nomeSerie = nome;
				serie.noselect = no_select;
				serie.innerHTML = nome+"<span style='float:right'> ("+provider_name+")</span>";//"<b>"+nome+"</b> - "+provider_name;
				$("#selectSerie").append(serie);
			});
			$("#selectSerie").trigger("chosen:updated");
			operazioneInCorso("");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("", "Si è verificato un errore durante il caricamento delle serie");
			return;
		}
	});
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
				var provider_name = $(this).find("provider_name").text();
				var serie = document.createElement("div");
				$(serie).addClass("panel-serieNuova");
				serie.innerHTML = "<h4 class='panel-title'><b>" + nome +"</b> - "+provider_name + "</h4>" + "<div class='buttonsAccordion'>" + "<button class='btn btn-warning' title='Aggiungi' onclick=\"aggiungiSerie("+provider+","+id+",'<b>"+nome.replace("'","\\'")+"</b>"+" - "+provider_name+"')\"><span class='glyphicon glyphicon-plus'></span></button>&nbsp;" + "<button class='btn btn-warning' title='Info Serie' onclick='infoSerie("+id+")'><span class='glyphicon glyphicon-info-sign'></span></button>" + "</div>";
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
	GetCountToDownload();
	//showButtonResults();
}
function lookForToDownload(){
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=getEpisodiDaScaricare",
		dataType : "xml",
		success : function(msg) {
			var resp = parseBooleanXML(msg);
			if(resp){
				var numEpisodi = $(msg).find("episodio").length;
				$("#btnShowSelect").removeClass("hidden");
				$("#btnShowSelect").addClass("visible");
				$("#btnShowSelect").text(numEpisodi+" nuovi");
			}
		}
	});
}
function GetCountToDownload(){
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=getNumEpisodiDaScaricare",
		dataType : "xml",
		success : function(msg) {
			var resp = parseBooleanXML(msg);
			if(resp){
				var count=parseInt($(msg).find("Integer").text());
				$("#btnShowSelect").removeClass("hidden");
				$("#btnShowSelect").addClass("visible");
				$("#btnShowSelect").text(count+" nuovi");
			}
		}
	});
}
function showListDownload(){
	showModalInfo("Attendi...", "<center><img src='img/loading.gif' /></center>");
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=getEpisodiDaScaricare",
		dataType : "xml",
		success : function(msg) {
			var resp = parseBooleanXML(msg);
			if(resp){
				var numEpisodi = $(msg).find("episodio").length;
				var html = "<div id='downloadList'><table>";
				var noSelectHTML = "<table>";
				$(msg).find("serie").each(function(){
					var noselect = $(this).attr("noselect");
					$(this).find("episodio").each(function(){
						var titolo = $(this).find("titolo").text();
						var id = $(this).find("id").text();
						if(noselect == "false")
							//html+="<tr><td class='selectionTD'><input type='checkbox' onchange='res_ChangeSelection("+id+")' value='"+id+"' id='resCheck"+id+"' checked /><b>&nbsp;"+titolo+"</b></td></tr>";
							html+="<tr><td class='selectionTD'><input type='checkbox' onchange='res_ChangeSelection("+id+")' value='"+id+"' id='resCheck"+id+"'/><b>&nbsp;"+titolo+"</b></td></tr>";
						else
							noSelectHTML+="<tr><td class='selectionTD'><input type='checkbox' onchange='res_ChangeSelection("+id+")' value='"+id+"' id='resCheck"+id+"'/><b>&nbsp;"+titolo+"</b></td></tr>";
					});
				});
				html+="</table>";
				noSelectHTML+="</table></div>";
				$("#modalDownloadButton").removeClass("hidden");
				showModalInfo("Download", html+"<br><div><b>Episodi da non selezionare automaticamente</b></div>"+noSelectHTML);
			}
		}
	});
}
function downloadList(){
	$("#downloadList").find("input").each(function(){
		if($(this).is(":checked")){
			var id = $(this).val();
			downloadS(id);
			$(this).remove();
		}
	});
	GetCountToDownload();
}

function ignora() {
	operazioneInCorso("Ignoro episodi");
	$("#accordion").find("input[type=checkbox]").each(function(){
		if($(this).is(":checked")){
			var idEp = $(this).val();
			ignoraS(idEp);
		}
	}); 
	//showButtonResults();
	operazioneInCorso("");
}
function ignoraS(id){
	if($("#chkEp_"+id).attr("stato_visualizzazione")!=0){
		return;
	}
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=ignora&id=" + id,
		dataType : "xml",
		success : function(msg) {
			var resp = parseBooleanXML(msg);
			if(resp){
				$("#btnDown_"+id).replaceWith(generaBottone(4, id));
				if($("#chkEp_"+id).is(":checked")){
					$("#chkEp_"+id).removeAttr('checked');
				}
				$("#chkEp_"+id).attr("stato_visualizzazione","4");
				
				$("#divEP_"+id).removeClass();
				$("#divEP_"+id).addClass("episodio episodioIgnorato");
			}
			else
				showModal("","Episodio non ignorato");
			//showButtonResults();
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("","Si è verificato un errore durante il download");
		}
	});
}
function aggiornaSerie(bottone) {
	$(bottone).prop("disabled", "true");
	operazioneInCorso("Aggiorno l'elenco delle serie tv");
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=updateListSeries",
		dataType : "xml",
		success : function(msg) {
			if (parseBooleanXML(msg)) {
				operazioneInCorso("Aggiornamento dell'elenco delle serie completato con successo");
				caricaElencoSerieCompleto();
				caricaSerieNuove();
			}
			else
				showModal("", "Si è verificato un errore durante l'aggiornamento");
			operazioneInCorso("");
			$(bottone).removeAttr("disabled");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("","Si è verificato un errore durante l'aggiornamento");
			$(bottone).removeAttr("disabled");
		}
	});
}
function selezionaPerStato(stato){
	var trovati = 0;
	$("#accordion").find("input[type=checkbox]").each(function(){
		if($(this).attr("stato_visualizzazione")==stato && $(this).attr("noselect")=='false'){
			$(this).prop('checked', true);
			trovati++;
		}
		else if($(this).attr("stato_visualizzazione")==stato && $(this).attr("noselect")=='true'){
			trovati++;
		}
		else
			$(this).removeAttr('checked');
	});
	if(trovati==0){
		showModal("","Non ci sono episodi da scaricare");
	}
	//showButtonResults();
}
function selezionaTutto(selected) {
	$("#accordion").find("input[type=checkbox]").each(function(){
    	if(selected)
    		$(this).prop('checked', true);
    	else
    		$(this).removeAttr('checked');
	});
	//showButtonResults();
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
				var provider_name = $(this).find("provider_name").text();
				var noselect = $(this).find("no_select").text();
				var rating = $(this).find("voto").text();
				var ratingS = rating ? "(" + rating + ")" : "";
				var elem = creaSerieElementoPagina("<b>"+nome+"</b> " + ratingS + " - "+provider_name, id, provider, noselect);
				$("#accordion").append(elem);
				//getEpisodi(id, noselect);
			});
				
			operazioneInCorso("");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("","Si è verificato un errore durante l'aggiornamento");
		}
	});
}
function removeSerie(id) {
	bootbox.confirm("Vuoi rimuovere la serie dai preferiti?",function(res){
		if(res){
    		$.ajax({
    			type : "POST",
    			url : "./OperazioniSerieServlet",
    			data : "action=remove&id=" + id,
    			dataType : "xml",
    		   	success : function(msg) {
    		   		var r = parseBooleanXML(msg);
    		   		if (r) {
    		   			$("#serie" + id).remove();
    		   			deleteFolder(id);
    		   			$("#serie_vedere_"+id).remove();
    		   			//showButtonResults();
    		   		}
    		   		operazioneInCorso("");
    		   	},
    		   	error : function(msg) {
    		   		operazioneInCorso("");
    		   		showModal("","Si è verificato un errore durante l'aggiornamento");
    		   	}
    		});
		}
	});
}
function deleteFolder(id){
	bootbox.confirm("Vuoi cancellare la cartella contenente gli episodi?",function(res){
		if(res){
    		$.ajax({
    			type : "POST",
    			url : "./OperazioniSerieServlet",
    			data : "action=deleteFolder&idSerie=" + id,
    			dataType : "xml",
    		   	success : function(msg) {
    		   		var r = parseBooleanXML(msg);
    		   		if (r) {
    		   			showModal("Cancellazione serie", "Serie cancellata con successo!");
    		   		}
    		   		else {
    		   			showModal("Cancellazione serie", "Potrebbero essere presenti ancora alcuni file");
    		   		}
    		   		operazioneInCorso("");
    		   	},
    		   	error : function(msg) {
    		   		operazioneInCorso("");
    		   		showModal("","Si è verificato un errore durante la richiesta");
    		   	}
    		});
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
			showModal("","Si è verificato un errore durante l'aggiornamento");
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
function getEpisodi(id, noselect) {
	if(noselect===undefined)
		noselect=false;
	operazioneInCorso("Carico l'elenco degli episodi da scaricare");
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=getEpisodiBySerie&serie=" + id,
		dataType : "xml",
		success : function(msg) {
			$("#accordion" + id).empty();
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
					"<input type='checkbox' noselect='"+noselect+"' value='" + idE + "' id='chkEp_"+idE+"' stato_visualizzazione='"+stato+"'> Episodio <b>" + (episodio == 0 ? "Speciale" : episodio) + "</b></input>" +
					"<div class='episodioButtons'>" +
					generaBottone(stato,idE) +"&nbsp;" +
					"<button class='btn btn-warning' title='Info episodio' onclick='infoEpisodio("+id+","+idE+")'><span class='glyphicon glyphicon-info-sign'/></button>&nbsp;" +
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
				$("#episodiScaricare" + id)	.text("(Nessun episodo da scaricare. Vedere: "+daVedere+")");
			}
			else {
				$("#episodiScaricare" + id)	.text("(" + daScaricare + " da scaricare. Vedere: "+daVedere+")");
			}
			//showButtonResults(); /* mostra il tasto di download */
			operazioneInCorso("");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("","Si è verificato un errore durante l'aggiornamento");
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
function infoSerieButton(){
	var serie = selectSerie.options[selectSerie.selectedIndex];
	if(serie==null || serie=="" || serie=="undefined" || serie.length==0 || serie.value==""){
		showModal("","Devi selezionare una serie");
		return;
	}
	var id_serie = serie.value;
	infoSerie(id_serie);
}
function infoSerie(idSerie){
	$.ajax({
		type : "POST",
		url : "./OperazioniInfoServlet",
		data : "action=getIdTVDB&idSerie="+idSerie,
		dataType : "xml",
		success : function(msg) {
			var resp = parseBooleanXML(msg);
			if(resp){
				var idTVDB=parseInt($(msg).find("Integer").text());
				if(idTVDB>0){
					showInfoTVDB(idTVDB);
				}
				else {
					showAssociaSerie(idSerie);
				}
			}
			else
				showModal("","Si è verificato un errore");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("","Si è verificato un errore");
		}
	});
}
function associaBottone(){
	var val=$("input[name='associaSerie']:checked").val();
	if(val==null || val=="undefined" || val.length==0){
		showModal("Associa", "Devi selezionare una serie dall'elenco");
		return;
	}
	var idSerie = $("#associaSerieIDSerie").val();
	associaSerieTVDB(idSerie, val);
	$("#associaTVDBModal").modal('hide');
	var idEpisodio = $("#mustViewInfoEpisode").val();
	if(idEpisodio!=null || idEpisodio.length>0 || idEpisodio!=undefined)
		showInfoEpisodioTVDB(idEpisodio);
	else
		showInfoTVDB(val);
}
function showAssociaSerie(idSerie, idEp){
	showModalAssociaTVDB("Attendi...", "<center><img src='img/loading.gif' /></center>");
	$.ajax({
		type : "POST",
		url : "./OperazioniInfoServlet",
		data : "action=cercaSerieAssociabili&id="+idSerie,
		dataType : "xml",
		success : function(msg) {
			var resp = parseBooleanXML(msg);
			if(resp){
				var num_serie=$(msg).find("serie").size();
				if(num_serie==0){
					showModalAssociaTVDB("Associa serie", "<center>Non ci sono serie che soddisfano i requisiti</center>");
				}
				else if(num_serie == 1){
					var serie = $(msg).find("serie");
					var idTVDB = parseInt($(serie).find("id_serie").text());
					var nomeSerie = $(serie).find("nome_serie").text();
					associaSerieTVDB(idSerie, idTVDB);
					$("#associaTVDBModal").modal('hide');
					//showModal("Associa", "La serie è stata associata automaticamente a "+nomeSerie);
					if(idEp==undefined)
						showInfoTVDB(idTVDB);
					else
						showInfoEpisodioTVDB(idEp);
				}
				else {
					var bodyAssocia = "<br>";
					$(msg).find("serie").each(function(){
						var nome=$(this).find("nome_serie").text();
						var id = $(this).find("id_serie").text();
						var anno = $(this).find("anno_inizio").text();
						var input="<p class='pAssociatore'><input type='radio' name='associaSerie' value='"+id+"'>"+nome+" ("+anno+")</input><button class='btn btn-warning btnViewAssocia' onclick='showInfoTVDB("+id+")'>Visualizza</button></p><br>";
						bodyAssocia+=input;
						if(idEp!=undefined)
							bodyAssocia+="<input type='hidden' value='"+idEp+"' id='mustViewInfoEpisode' />";
					});
					$("#associaSerieIDSerie").val(idSerie);
					showModalAssociaTVDB("Associa serie", bodyAssocia);
					//$("#associaTVDBModal").modal('hide');
					//showModal("","Attendere implementazione");
				}
			}
			else
				showModal("","Si è verificato un errore");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("","Si è verificato un errore");
		}
	});
}
function associaSerieTVDB(idSerie, idTVDB){
	$.ajax({
		type : "POST",
		url : "./OperazioniInfoServlet",
		data : "action=associa&idSerie="+idSerie+"&id_tvdb="+idTVDB,
		dataType : "xml",
		success : function(msg) {
			var resp = parseBooleanXML(msg);
			if(resp){
				
			}
			else 
				showModal("","Errore durante l'associazione");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("","Si è verificato un errore");
		}
	});
}
function infoEpisodio(idSerie, idEp){
	$.ajax({
		type : "POST",
		url : "./OperazioniInfoServlet",
		data : "action=getIdTVDB&idSerie="+idSerie,
		dataType : "xml",
		success : function(msg) {
			var resp = parseBooleanXML(msg);
			if(resp){
				var idTVDB=parseInt($(msg).find("Integer").text());
				if(idTVDB>0){
					showInfoEpisodioTVDB(idEp);
				}
				else {
					showAssociaSerie(idSerie, idEp);
				}
			}
			else
				showModal("","Si è verificato un errore");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("","Si è verificato un errore");
		}
	});
}
function showInfoEpisodioTVDB(idEp){
	showModalInfo("Attendi...", "<center><img src='img/loading.gif' /></center>");
	$.ajax({
		type : "POST",
		url : "./OperazioniInfoServlet",
		data : "action=getInfoEpisodio&id="+idEp,
		dataType : "xml",
		success : function(msg) {
			var resp = parseBooleanXML(msg);
			if(resp){
				var episodio = $(msg).find("EpisodioTVDB");
				var nomeSerie = $(msg).find("nomeSerie").text();
				var st = parseInt($(episodio).find("stagione").text());
				var ep = parseInt($(episodio).find("episodio").text());
				var titolo  = $(episodio).find("titolo").text();
				var descr  = $(episodio).find("descrizione").text();
				var img  = $(episodio).find("immagine").text();
				var attori  = $(episodio).find("attori").text()
				var gs  = $(episodio).find("guestStars").text();
				var air  = $(episodio).find("data_air").text();
				var regista = $(episodio).find("regista").text();
				var sceneggiatori = $(episodio).find("sceneggiatori").text();
				var rating = $(episodio).find("rating").text();
				var body ="";
				body+="<p><center>"+creaImmagineGallery(img)+"</center></p><br>";
				body+="<p><b>Titolo: </b>"+titolo+"</p>";
				body+="<p><b>Trama: </b>"+descr+"</p>";
				body+="<p><b>Media voti: </b>"+rating+"</p>";
				body+="<p><b>Regista: </b>"+regista+"</p>";
				body+="<p><b>Sceneggiatori: </b>"+sceneggiatori+"</p>";
				body+="<p><b>Attori: </b>"+attori+"</p>";
				body+="<p><b>Guest stars: </b>"+gs+"</p>";
				body+="<p><b>Messa in onda: </b>"+air+"</p>";
				
				var title = nomeSerie + " - S"+(st<10?"0"+st:st)+"E"+(ep<10?"0"+ep:ep)+" - "+titolo;
				
				showModalInfo(title, body);
				$("img").error(function () { 
				    $(this).attr("src", "img/no_image.gif");
				    $(this).parent().attr("href", "img/no_image.gif").attr("data-lightbox", "image-error");
				});
			}
			else {
				showModal("","Errore");
			}
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("","Si è verificato un errore");
		}
	});
}
function showInfoTVDB(idTVDB, force){
	showModalInfo("Attendi...", "<center><img src='img/loading.gif' /></center>");
	if(force==undefined)
		force=false;
	$.ajax({
		type : "POST",
		url : "./OperazioniInfoServlet",
		data : "action=getInfoSerie&id="+idTVDB+"&force="+force,
		dataType : "xml",
		success : function(msg) {
			$('#modalTVDBUpdate').removeClass('hidden');
			var resp = parseBooleanXML(msg);
			if(resp){
				var body = "";
				var nome = $(msg).find("nome_serie").text();
				var id=$(msg).find("id_serie").text();
				var primo_air = $(msg).find("first_air").text();
				var rating=$(msg).find("rating").text();
				var network=$(msg).find("network").text();
				var airDay=$(msg).find("air_day").text();
				var airHour=$(msg).find("air_hour").text();
				var durataEpisodi=$(msg).find("durata_episodi").text();
				var stato=$(msg).find("stato_serie").text();
				var banner=$(msg).find("banner_url").text();
				var poster=$(msg).find("poster_url").text();
				var descrizione = $(msg).find("descrizione").text();
				var generi = "";
				$(msg).find("generi").find("genere").each(function(){
					generi+=$(this).text()+" ";
				});
				var attori="<br><table class='tableAct'>";
				
				var aperto=false;
				var c = 0;
				$(msg).find("attori").find("attore").each(function(){
					var nome=$(this).find("nome_attore").text();
					var ruolo=$(this).find("ruolo_attore").text();
					var img=$(this).find("img_attore").text();
					var g_img = creaImmagineAttore(img, nome, ruolo);
					if(aperto==false){
						attori+="<tr>";
						aperto=true;
					}
					attori+="<td>"+g_img+" "+nome+"<br> <b>"+ruolo+"</b></td>";
					c++;
					if(c%2==0){
						attori+="</tr>";
						aperto=false;
					}
				});
				if(aperto==true){
					attori+="</tr>";
				}
				attori+="</table>";
				
				var info= "<br><p><b>Titolo: </b>"+nome+"</p>" +
						"<p><b>Rating: </b>"+rating+"</p>" +
						"<p><b>Genere: </b>"+generi+"</p>" +
						"<p><b>Network: </b>"+network+"</p>" +
						"<p><b>Inizio: </b>"+primo_air+"</p>" +
						"<p><b>Giorno: </b>"+airDay+"</p>" +
						"<p><b>Ora: </b>"+airHour+"</p>" +
						"<p><b>Durata: </b>"+durataEpisodi+" minuti</p>" +
						"<p><b>Stato: </b>"+stato+"</p>" +
						"<p><b>Descrizione: </b>"+descrizione+"</p>"+
						"<input type='hidden' value='"+id+"' id='modalTVDBSerieID'/>";
    			
				var item_count = 0;
				var images = new Array();
				$(msg).find("posters").find("poster").each(function(){
					var image = $(this).text();
					images[item_count]=image;
					item_count++;
					
				});
				
				var listPage = "<br>";
				var listDiv = "";
				var pagina = 1;
				var i=0;
				var curDiv = "";
				var aperto=false;
				for(i=0;i<images.length;i++){
					if(aperto==false){
						listPage+="<li class='"+(pagina==1?"active":"")+"'><a href='#GalPag"+pagina+"' role='tab' data-toggle='tab'>"+pagina+"</a></li>";
						curDiv+="<div class='tab-pane fade' id='GalPag"+pagina+"'>";
						aperto=true;
					}
					
					curDiv+=creaImmagineGallery(images[i]);
					if(i>0 && i%5==0 && aperto==true){
						curDiv+="</div>";
						listDiv+=curDiv;
						pagina++;
						curDiv="";
						aperto=false;
					}
				}
				if(aperto==true){
					curDiv+="</div>";
					listDiv+=curDiv;
					curDiv="";
					aperto=false;
				}
							
				var gallery= "<div class='bs-example bs-example-tabs'>"+
			    				"<ul id='myTabGallery' class='nav nav-tabs' role='tablist'>"+
				    				listPage +
				    			"</ul>"+
				    			"<div id='myTabGalleryContent' class='tab-content'>"+
			    					listDiv+
			    				"</div>"+
		    				"</div>";
				
				body+="<p><center><img src='"+banner+"'></center></p>";
				body+= "<div class='bs-example bs-example-tabs'>"+
			    "<ul id='myTab' class='nav nav-tabs' role='tablist'>"+
			      "<li class='active'><a href='#Info' role='tab' data-toggle='tab'>Informazioni</a></li>"+
			      "<li class=''><a href='#Attori' role='tab' data-toggle='tab'>Attori</a></li>"+
			      "<li class=''><a href='#Gallery' role='tab' data-toggle='tab'>Gallery</a></li>"+
			    "</ul>"+
			    "<div id='myTabContent' class='tab-content'>"+
			      "<div class='tab-pane fade active in' id='Info'>"+
			        info +
			      "</div>"+
			      "<div class='tab-pane fade' id='Attori'>"+
			      	attori + //"Qui vanno gli attori" +
			      "</div>"+
			      "<div class='tab-pane fade' id='Gallery'>"+
			        gallery +
			      "</div>"+
			    "</div>"+
			"</div>";
				showModalInfo(nome, body);
				$("#GalPag1").addClass("active in");
				$("img").error(function () { 
				    $(this).attr("src", "img/no_image.gif");
				    $(this).parent().attr("href", "img/no_image.gif").attr("data-lightbox", "image-error");
				});
			}
			else
				showModal("","Si è verificato un errore");
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("","Si è verificato un errore");
		}
	});
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
				if($("#chkEp_"+id).is(":checked")){
					$("#chkEp_"+id).removeAttr('checked');
				}
				$("#chkEp_"+id).attr("stato_visualizzazione","1");
				
				if(!$("#divEP_"+id).hasClass("episodioVisto")){
					$("#divEP_"+id).removeClass();
					$("#divEP_"+id).addClass("episodio episodioDaVedere");
				}
			}
			else
				showModal("","Episodio non scaricato");
			//showButtonResults()
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("","Si è verificato un errore durante il download");
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
				if($("#tdEpVedere"+id).parent().find("tr").length==1){
					$("#tdEpVedere"+id).parents("div[id^='serie_vedere_']").remove();
				}
				else
					$("#tdEpVedere"+id).remove();
			}
			else {
				$("#divEP_"+id).removeClass("episodioDaVedere");
				$("#divEP_"+id).addClass("episodioRimosso");
				$("#chkEp_"+id).attr("stato_visualizzazione","3");
				var bottone = generaBottone(3, id);
				$("#btnPlay_"+id).replaceWith(bottone);
				showModal("","File non trovato");
			}
		},
		error : function(msg) {
			
		}
	});
}
function cancellaEpisodio(id){
	var stato=$("#chkEp_"+id).attr("stato_visualizzazione");
	if(stato=="0" || stato=="3"){
		showModal("","Non credo tu voglia eliminare qualcosa che non esiste. Potrebbe crearsi un buco nero!");
		return;
	}
	else {
		bootbox.confirm("Vuoi davvero eliminare l'episodio?", function(res){
			if(res){
    			$.ajax({
    				type : "POST",
    				url : "./OperazioniSerieServlet",
    				data : "action=deleteFile&episodio=" + id,
    				dataType : "xml",
    				success : function(msg) {
    					var r = parseBooleanXML(msg);
    					if(r){
    						$("#divEP_"+id).removeClass("episodioIgnorato episodioDaVedere");
    						$("#divEP_"+id).addClass("episodioRimosso");
    						$("#chkEp_"+id).attr("stato_visualizzazione","3");
    						var bottone = generaBottone(3, id);
    						$("#btnPlay_"+id).replaceWith(bottone);
    					}
    					else {
    						showModal("","Non è stato possibile eliminare il file");
    					}
    				},
    				error : function(msg) {
    					showModal("","Non è stato possibile eliminare il file");
    				}
    			});
			}
		});
	}
}

function getEpisodiDaVedere() {
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=getEpisodiDaVedere",
		dataType : "xml",
		success : function(msg) {
			var r = parseBooleanXML(msg);
			if(r){
				$("#accordionVedere").empty();
				$(msg).find("serie").each(function(){
					var id = $(this).attr("id");
					var nome = $(this).attr("nome");
					var element = "<div class='panel panel-default seriePreferita' id='serie_vedere_" + id + "'>" + "<div class='panel-heading'>" + "<h4 class='panel-title'>" + "<a class='nomeSerie' data-toggle='collapse' data-parent='#accordionVedere' href='#collapse_vedere" + id + "'>" + nome + "</a>" + "</h4>" + "</div>" + "<div id='collapse_vedere" + id + "' class='panel-collapse collapse'>" + "<div class='panel-body'><div class='panel-group' id='accordion_vedere_" + id + "'></div></div>" + "</div>" + "</div>";
					$(element).appendTo("#accordionVedere");
					var tabellaEpisodi = "<table id='table_vedere_"+id+"' main-container='serie_vedere_"+id+"'></table>";
					$(tabellaEpisodi).appendTo("#accordion_vedere_"+id);
					$(this).find("episodio").each(function(){
						var titolo = $(this).find("titolo").text();
						var idEp = $(this).find("id").text();
						
						var tr = document.createElement("tr");
						$(tr).addClass("EpisodioDaVedere");
						$(tr).attr("id", "tdEpVedere"+idEp);
						tr.innerHTML="<td class='titoloVedere'>"+titolo+"</td><td class='bottoneVedere'><button class='btn btn-primary' onclick='play("+idEp+")'><span class='glyphicon glyphicon-play' /></button></td>";
						$(tr).appendTo("#table_vedere_"+id);
					});
					
					//accordion_vedere_+id
				});
				/*
				$(msg).find("episodio").each(function(){
					var nome = $(this).find("titolo").text();
					var id = $(this).find("id").text();
					
					var tr = document.createElement("tr");
					$(tr).addClass("EpisodioDaVedere");
					$(tr).attr("id", "tdEpVedere"+id);
					tr.innerHTML="<td class='titoloVedere'>"+nome+"</td><td class='bottoneVedere'><button class='btn btn-primary' onclick='play("+id+")'><span class='glyphicon glyphicon-play' /></button></td>";
					$(tr).appendTo("#listEpisodiDaVedere");
				});
				*/
			}
			else {
				showModal("","Si è verificato un errore");
			}
		},
		error : function(msg) {
			showModal("","Si è verificato un errore");
		}
	});
}
function creaImmagineGallery(imageURL){
	var image = "<a href='"+imageURL+"' data-lightbox='image-gallery'><img class='image-thumb' src='"+imageURL+"'></a>";
	return image;
}
function creaImmagineAttore(imageURL, nomeAttore, ruolo){
	var image = "<a href='"+imageURL+"' data-lightbox='image-attore' data-title='"+nomeAttore+" \350 "+ruolo+"'><img class='image-thumb2' src='"+imageURL+"'></a>";
	return image;
}
function showModalInfo(title, body){
	$(".modal-wide-info").on("show.bs.modal", function() {
		var height = $(window).height() - 200;
		$(this).find(".modal-body-info").css("max-height", height);
	});
	$("#tallModal").modal('show');
	$("#info-modal-title").text(title);
	$("#modalInfoBody").html(body);
}
function showModalAssociaTVDB(titolo, corpo) {
	$(".modal-wide-info").on("show.bs.modal", function() {
		var height = $(window).height() - 200;
		$(this).find(".modal-body-info").css("max-height", height);
	});
	$("#associaTVDBModal").modal('show');
	$("#associaTVDBtitle").text(titolo);
	$("#associaTVDBBody").html(corpo);
}
function showSelezione() {
	var trovati = 0;
	var trovatiN = 0;
	var html = "<table>";
	var noSelect = "";
	$(".seriePreferita").each(function(){
		var nomeSerie = $(this).find(".nomeSerie b").text();
		$(this).find(".panel-body .panel-group").find(".panel").each(function(){
			var stagione = $(this).find(".panel-title a").text();
			$(this).find("input[stato_visualizzazione=0]").each(function(){
				var episodio = $(this)[0].nextSibling.nodeValue + $(this).next().text();
				var id = $(this).val();
				var checked = ($(this).is(":checked"))?"checked":"";
				var noselectAttr = $(this).attr("noselect");
				if(noselectAttr=='true'){
					noSelect+="<tr><td class='selectionTD'><input type='checkbox' onchange='res_ChangeSelection("+id+")' id='resCheck"+id+"' "+checked+"/><b>&nbsp;"+nomeSerie+"</b></td><td class='selectionTD'>"+stagione+"</td><td class='selectionTD'>"+episodio+"</td></tr>";
					trovatiN++;
				}
				else {
					html+="<tr><td class='selectionTD'><input type='checkbox' onchange='res_ChangeSelection("+id+")' id='resCheck"+id+"' "+checked+"/><b>&nbsp;"+nomeSerie+"</b></td><td class='selectionTD'>"+stagione+"</td><td class='selectionTD'>"+episodio+"</td></tr>";
					trovati++;
				}
			});
		});
	});
	if(noSelect.length>0)
		html+="<tr><td><span class='takespace'>_</span></tr></td>"+
			  "<tr><td><b>Episodi da non selezionare automaticamente</b></td></tr>";
	html += noSelect;
	html += "</table>";
	if((trovati+trovatiN) > 0)
		showModalInfo("Selezione", html);
	else
		showModalInfo("Selezione", "Nessun episodio selezionato");
}
function showButtonResults(){
	var toDownload = $("#accordion").find("input[stato_visualizzazione=0]").length;
	if(toDownload>0){
		$("#btnShowSelect").removeClass("hidden");
		$("#btnShowSelect").addClass("visible");
		$("#btnShowSelect").text(toDownload+" nuovi");
	}
	else {
		$("#btnShowSelect").removeClass("visible");
		$("#btnShowSelect").addClass("hidden");
		$("#btnShowSelect").text("0 nuovi");
	}
}
function res_ChangeSelection(id){
	var check = $("#resCheck"+id).is(":checked");
	$("#chkEp_"+id).prop('checked', check);
}
function aggiornaTVDB(){
	var idSerie = $("#modalTVDBSerieID").val();
	showInfoTVDB(idSerie, true);
}