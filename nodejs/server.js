var amqp = require('amqplib/callback_api');
var prompt = require('prompt');

var mqConnection;
var mqExchange = "fietsenrek_servers";
var mqIP;

var racks = [];
var rackInfo = {};
var spots = {};

boot();

function boot() {
	console.log("Welcome.");
	prompt.start();
	prompt.get({
    	properties: {
    		name: {
    			description: 'Enter your name'
      		},
      		password: {
        		hidden: true
      		}
    	}
  	}, function (err, result) {
  		console.log(err);
  		console.log(result);
  	});
	//connect();
}

function connect() {
	amqp.connect('amqp://localhost', function (err, conn) {
		if (err) {
			console.log("Connecting to RabbitMQ failed.");
			setTimeout(connect, 5000);
		} else {
			mqConnection = conn;
			init();
		}
	});
}

function init() {
	console.log("Initiaizing");
	mqConnection.on("close", function() {
		console.log("Notice: RabbitMQ disconnected, going back to connecting.");
		connect();
	})
	console.log("Creating channel");
	mqConnection.createChannel(function (err, ch) {
		if (err) {
			console.log("Something went wrong trying to create a channel!");
			console.log("Going back to connecting.");
			connect();
		} else {
			ch.assertExchange(mqExchange, 'fanout', { durable: false });
			ch.assertQueue('', { exclusive: true }, function (err, q) {
				ch.bindQueue(q.queue, mqExchange, '');
				ch.consume(q.queue, function (msg) {
					receiveMessage(msg);
				}, { noAck: true });
			});
		}
	});
	console.log("Server started.");
}

function receiveMessage(msg) {
	data = JSON.parse(msg.content);
	switch (data[0]) {
	case "heartbeat":
		checkRack(data.slice(1), ch, q.queue);
		break;
	}
}

function checkRack(data, ch, queue) {
	rackName = data[0];
	if (racks.indexOf(rackName) == -1) {
		racks.push(rackName);
	}
	rackInfo[rackName] = [Date.now(), rackName, data[1].length];
	spots[rackName] = data[1];
}

setInterval(function () {
	for (i = 0; i < racks.length; i++) {
		rack = racks[i];
		if (rackInfo[rack][0] + 5000 < Date.now()) {
			console.log(" # Rack \"%s\" has been removed.", rackInfo[rack][1]);
			rackInfo[rack] = null;
			spots[rack] = null;
			racks.splice(i, 1);
		}
	}
}, 5000);




/*** INTERFACE ***/

function printList() {
	if (racks.length == 0) {
		console.log(" # There are no racks right now.");
		return;
	}
	for (i = 0; i < racks.length; i++) {
		info = rackInfo[racks[i]];
		console.log(" # ", i, "\t", info[1], "\tsize=", info[2], "\tlast_heartbeat=", info[0]);
		console.log(spots[racks[i]]);
	}
}

var stdin = process.openStdin();
stdin.addListener("data", function (d) {
	string = d.toString().trim();
	switch (string) {
		case "list":
			printList();
			break;
	}
});
