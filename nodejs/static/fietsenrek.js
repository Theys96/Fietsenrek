var listElement;

$(function() {
	listElement = $('#list');
	update();
});

function update() {
	$.getJSON('/list', function (racks) {
		listElement.html("");
		for (i = 0; i < racks.length; i++) {
			var status;
			if (racks[i].last_heartbeat < 1000) {
				status = "<span class='text-success'>OK</span>";
			} else if (racks[i].last_heartbeat < 5000) {
				status = "<span class='text-warning'>please stand by</span>";
			} else {
				status = "<span class='text-danger'>disconnected</span>";
			}
			div = $('<div class="rack">');
			div.append("<h3>" + racks[i].name + "</h3>");
			div.append("<div class='row'>");
			list = $('<div class="row">');
			list.append("<div class='col-2'><p>size: " + racks[i].size + "</p></div>");
			list.append("<div class='col-3'><p>status: " + status + "</p></div>");
			list.append("<div class='col-7'></div>"); // Filler
			for (j = 0; j < racks[i].spots.length; j++) {
				list.append('<div style="background-color: ' + (racks[i].spots[j] > 0 ? "salmon" : "lightgreen") + '" class="col-1 spot">' + (racks[i].spots[j] > 0 ? racks[i].spots[j] : "") + '</div>');
			}
			div.append(list);
			listElement.append(div);
		}
	});
}

setInterval(update, 500);