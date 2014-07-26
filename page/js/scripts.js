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
		selectSerie.innerHTML="";
		return;
	}
	alert("valore selezionato: "+provider.value);
}
function caricaProvider(){
	$.ajax({
		type: "POST",
		url: "./OperazioniSerieServlet",
		data: "action=getProviders",
		dataType: "xml",
		success: function(msg){
			groupProvider.innerHTML="<option selected></option>";
			$(msg).find("provider").each(function(){
				var nome = $(this).find("name").text();
				var id = $(this).find("id").text();
				var provider = document.createElement("option");
				provider.value = id;
				provider.innerHTML = nome;
				$("#groupProvider").append(provider);
			});
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