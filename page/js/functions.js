function showModal(titolo, messaggio) {
	$("#ModalMessaggioLabel").text(titolo);
	$("#TestoModal").text(messaggio.replace("è", "\350"));
	$('#ModalMessaggio').modal('show');
}