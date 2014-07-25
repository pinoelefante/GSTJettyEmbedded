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
	
}
function caricaProvider(){
	
}
function parseBooleanXML(xml){
	var valore = new Boolean($(xml).find("booleanResponse").text());
	return valore;
}
function download(){
	
}
function ignora(){ 
	
}