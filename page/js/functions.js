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
		dataType : "text",
		success : function(msg) {
			showModal("Opzioni","La finestra delle operazioni si è aperta");
		},
		error : function(msg){
			showModal("Si è verificato un errore");
			operazioneInCorso("");
		}
	});
}
