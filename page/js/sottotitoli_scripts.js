function loadSeriePreferite() {
	$.ajax({
		type : "POST",
		url : "./OperazioniSerieServlet",
		data : "action=getSeriePreferite",
		dataType : "xml",
		success : function(msg) {
			operazioneInCorso("Caricando le serie preferite");
			$(msg).find("serie").each(function() {
				var nome = $(this).find("name").text();
				var id = $(this).find("id").text();
				var provider = $(this).find("provider").text();
				var provider_name = $(this).find("provider_name").text();
				var elem = creaSerieElementoPagina("<b>"+nome+"</b> - "+provider_name, id, provider);
			});
		},
		error : function(msg) {
			operazioneInCorso("");
			showModal("","Si è verificato un errore durante l'aggiornamento");
		}
	});
}