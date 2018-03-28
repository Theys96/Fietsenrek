var listElement;

$(function() {
	listElement = $('#list');
	update();
});

function lead0(n) {
	return n < 10 ? "0"+n : n;
}

function ms2time(ms) {
	var s = Math.floor(ms/1000);
	var d = Math.floor(s / (60*60*24)); s -= d*(60*60*24);
	var h = Math.floor(s / (60*60)); s -= h*(60*60);
	var m = Math.floor(s / 60); s -= m*60;
	if (d > 0)
		return d + " days, " + h + ":" + lead0(m) + ":" + lead0(s);
	else if (h > 0)
		return h + ":" + lead0(m) + ":" + lead0(s);
	else
		return m + ":" + lead0(s);
}

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
				color = racks[i].spots[j] > 0 ? "salmon" : (racks[i].spots[j] < 0 ? "steelblue" : "lightgreen");
				content = racks[i].spots[j] > 0 ? ms2time(racks[i].spots[j]) : "";
				list.append('<div style="background-color: ' + color + '" class="col-1 spot">' + content + '</div>');
			}
			div.append(list);
			listElement.append(div);
		}
	});
}

setInterval(update, 500);