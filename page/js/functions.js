function showModal(titolo, messaggio) {
	$("#ModalMessaggioLabel").text(titolo);
	$("#TestoModal").text(messaggio);
	$('#ModalMessaggio').modal('show');
}