function aggiungiSerie(){
	var provider=selectProvider.options[selectProvider.selectedIndex].value;
	var serie=selectSerie.options[selectSerie.selectedIndex].value;
	$.ajax({
		type: "POST",
		url: "./OperazioniSerieServlet",
		data: "action=add&provider="+provider+"&serie="+serie,
		dataType: "xml",
		success: function(msg){
			var response=parseBooleanXML(msg);
		}
	});
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
				var serie = document.createElement("option");
				serie.value = id;
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
				var serie = document.createElement("div");
				$(serie).addClass("panel-serieNuova");
				serie.innerHTML="<h4 class='panel-title'>"+nome+"</h4>"+
					"<div class='buttonsAccordion'>"+
						"<button class='btn btn-warning'>Aggiungi</button>"+
						"<button class='btn btn-warning'>Info</button>"+
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
	var valore = new Boolean($(xml).find("booleanResponse").text());
	return valore;
}
function download(){
	
}
function ignora(){ 
	
}
function selezionaTutto(){
	
}
function operazioneInCorso(messaggio){
	if(messaggio.length>0)
		divMessaggioOperazione.innerHTML="<img src='img/loading.gif' height='16' width='16'/>&nbsp;"+messaggio;
	else
		divMessaggioOperazione.innerHTML="";
}