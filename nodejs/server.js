var amqp = require('amqplib/callback_api');
var prompt = require('prompt');
var express = require('express');

var mqServer = "localhost";
var mqConnection;
var mqUsername;
var mqPassword;
var mqExchange = "fietsenrek_servers";
var mqIP;

var api;
var server;
var port = 3000;

var racks = [];
var rackInfo = {};
var spots = {};

boot();

function boot() {
	console.log(" # Welcome.");
	prompt.start();
	prompt.get({
    	properties: {
    		ip: {
    			description: "Enter the RabbitMQ server",
      		},
      		username: {
    			description: "Enter RabbitMQ username"
      		},
      		password: {
    			description: "Enter RabbitMQ password",
    			hidden: true
      		},
      		port: {
      			description: "Enter REST API port",
      			message: "Enter a number.",
      			type: "integer"
      		},
      		freq: {
      			description: "Enter heartbeat frequency in seconds",
      			message: "Enter a number.",
      			type: "integer"
      		}
    	}
  	}, function (err, result) {
  		if (err) {
  			console.log("\n # Something went wrong while entering startup information.");
  			console.log(" # Please try again, shutting down.");
  			process.exit();
  		}
  		port = result.port == 0 ? port : result.port;
  		startTimeout(result.freq == 0 ? 5 : result.freq);
  		mqServer = result.ip == "" ? mqServer : result.ip;
  		mqUsername = result.username == "" ? null : result.username;
  		mqPassword = result.password == "" ? null : result.password;
  		console.log(" # Will now try to connect to RabbitMQ at %s", mqServer);
  		connect(startInterface);
  	});
}

function connect(callback) {
	conString = "amqp://";
	if (mqUsername) {
		conString += mqUsername + ":" + mqPassword;
	}
	conString += '@' + mqServer;
	amqp.connect(conString, function (err, conn) {
		if (err) {
			if (callback) {
				console.log(" # Note: not yet connected to RabbitMQ.");
				callback();
			}
			setTimeout(connect, 5000);
		} else {
			console.log(" # Succesfully connected to RabbitMQ at %s", mqServer);
			mqConnection = conn;
			init(callback);
		}
	});
}

function init(callback) {
	console.log(" # Initializing.");
	mqConnection.on("close", function() {
		console.log(" # RabbitMQ disconnected, going back to connecting.");
		connect();
	})
	console.log(" # Creating channel.");
	mqConnection.createChannel(function (err, ch) {
		if (err) {
			console.log(" # Something went wrong trying to create a channel!");
			console.log(" # Going back to connecting.");
			connect();
		} else {
			ch.assertExchange(mqExchange, 'fanout', { durable: false });
			ch.assertQueue('', { exclusive: true }, function (err, q) {
				ch.bindQueue(q.queue, mqExchange, '');
				ch.consume(q.queue, function (msg) {
					receiveMessage(ch, q, msg);
				}, { noAck: true });
			});
		}
	});
	console.log(" # Server started.");
	if (callback) {
		callback();
	}
}

function receiveMessage(ch, q, msg) {
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

function startTimeout(freq) {
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
	}, freq*1000);
}

function exit() {
	console.log(" # Goodbye.");
	mqConnection.close();
	server.close();
	process.exit();
}



/*** INTERFACE ***/

function getList() {
	list = [];
	time = Date.now();
	for (i = 0; i < racks.length; i++) {
		info = rackInfo[racks[i]];
		list[i] = {
			name: info[1],
			size: info[2],
			last_heartbeat: time - info[0],
			spots: spots[racks[i]]
		}
	}
	return list;
}

function printList() {
	list = getList();
	if (list.length == 0) {
		console.log(" # There are no racks right now.");
		return;
	}
	for (i = 0; i < list.length; i++) {
		console.log(" # ", i, "\t", list[i].name, "\tsize: ", list[i].size, "\tlast heartbeat: ", list[i].last_heartbeat, " ms ago");
		console.log(list[i].spots);
	}
}

function startInterface() {
	startRESTAPI();
	startCommandLineInterface();
}

function startCommandLineInterface() {
	var stdin = process.openStdin();
	stdin.addListener("data", function (d) {
		string = d.toString().trim();
		switch (string) {
			case "list":
				printList();
				break;
			case "exit":
				exit();
				break;
			default:
				console.log(" # That command was not understood. Supported commands:");
				console.log("\tlist\texit");
				break;
		}
		console.log(" # Enter a command.");
	});
	console.log(" # Enter a command.");
}

function startRESTAPI() {
	api = express();
	api.use(express.static(__dirname + "/static"));

	api.get("/", function (req, res) {
		res.type('html');
		res.sendFile('index.html');
	});

	api.get(["/list", "/list.json"], function (req, res) {
		res.type('json');
		res.send(JSON.stringify(getList(), null, 3));
	});

	server = api.listen(port);
	console.log(" # REST API started listening on port %d.", port);
}
