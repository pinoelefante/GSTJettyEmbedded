function showModal(titolo, messaggio) {
	$("#ModalMessaggioLabel").text(titolo);
	$("#TestoModal").text(messaggio.replace("è", "\350"));
	$('#ModalMessaggio').modal('show');
}
function goToDonazione(){
	location.href="https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=pino%2eelefante%40hotmail%2eit&lc=IT&item_name=Gestione%20Serie%20TV&item_number=gst&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted";
}
function goToOpzioni(){
	$.ajax({
		type : "POST",
		url : "./OperazioniSistemaServlet",
		data : "action=showOpzioni",
		dataType : "xml",
		success : function(msg) {
		},
		error : function(msg){
			showModal("","Si è verificato un errore");
			operazioneInCorso("");
		}
	});
}
function cercaUpdate(){
	
}
function chiudiGST(){
	$.ajax({
		type : "POST",
		url : "./OperazioniSistemaServlet",
		data : "action=isAskOnClose",
		dataType : "xml",
		success : function(msg) {
			var ask = parseBooleanXML(msg);
			if(ask){
				bootbox.confirm("Vuoi veramente chiudere GST?", function(r){
					if(r)
						closeGST();
				});
			}
			else {
				closeGST();
			}
		},
		error : function(msg){
			showModal("","Si è verificato un errore");
			operazioneInCorso("");
		}
	});
}
function closeGST(){
	$.ajax({
		type : "POST",
		url : "./OperazioniSistemaServlet",
		data : "action=closeGST",
		dataType : "xml",
		success : function(msg) {
			if(parseBooleanXML(msg)){
				$("body").empty();
				document.getElementsByTagName("body")[0].innerHTML="<h1 class='closeMessage'>Arrivederci!</h1>";
				showModal("","Gestione Serie TV è stato chiuso");
			}
			else
				showModal("","Si è verificato un errore");
		},
		error : function(msg){
			showModal("","Si è verificato un errore");
			operazioneInCorso("");
		}
	});
}