var listElement;

$(function() {
	listElement = $('#list');
	update();
});

function update() {
	$.getJSON('/list', function (racks) {
		listElement.html("");
		for (i = 0; i < racks.length; i++) {
			div = $('<div class="rack">');
			div.append("<h3>" + racks[i].name + "</h3>");
			div.append("<p>size: " + racks[i].size + ", last heartbeat: " + racks[i].last_heartbeat + " ms ago</p>");
			list = $('<div class="row">');
			for (j = 0; j < racks[i].spots.length; j++) {
				list.append('<div style="background-color: ' + (racks[i].spots[j] ? "salmon" : "lightgreen") + '" class="col-1 spot">');
			}
			div.append(list);
			listElement.append(div);
		}
	});
}

setInterval(update, 5000);