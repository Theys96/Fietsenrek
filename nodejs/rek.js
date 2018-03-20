var amqp = require('amqplib/callback_api');

if (!process.argv[2]) {
	console.log("Enter rek name!");
	process.exit(-1);
}
if (!process.argv[3]) {
	console.log("Enter rek size!");
	process.exit(-1);
}

var rackNaam = process.argv[2];
var rackSize = process.argv[3];
var spots = [];
for (i = 0; i < rackSize; i++) {spots[i] = 0;}
var toServerExchange = "fietsenrek_servers";
var fromServerExchange = "fietsenrek_racks";
var paused = false;

function sendInfo(data, ch) {
	var queue = data[1];
	// console.log(" # Info requested about rack \"%s\" by server at %s.", data[0], queue);
	if (data[0] == rackNaam) {
		// console.log(" # Sending info.");
		ch.sendToQueue(queue, new Buffer(JSON.stringify(["rackInfo", rackNaam, rackSize])));
	}
}

amqp.connect('amqp://localhost', function(err, conn) {
  conn.createChannel(function(err, ch) {
    ch.assertExchange(toServerExchange, 'fanout', {durable: false});
    ch.assertExchange(fromServerExchange, 'fanout', {durable: false});
    
    setInterval(function() {
    	if (paused == false) {
    		ch.publish(toServerExchange, '', new Buffer(JSON.stringify(["heartbeat", rackNaam, spots])));
    	}
    }, 1000);

    ch.assertQueue('', {exclusive: true}, function(err, q) {
      ch.bindQueue(q.queue, fromServerExchange, '');
      ch.consume(q.queue, function(msg) {
        data = JSON.parse(msg.content);
        switch(data[0]) {
          case "requestInfo":
            sendInfo(data.slice(1), ch);
            break;
        }
      }, {noAck: true});
  	});
  });
});

var stdin = process.openStdin();
stdin.addListener("data", function(d) {
    command = d.toString().trim().split(' ');
    switch(command[0]) {
    	case "set":
    		spots[parseInt(command[1])] = parseInt(command[2]);
    		console.log(" # Set spot %s to %s!", command[1], command[2]);
    		break;

    	case "pause":
    		paused = true;
    		console.log(" # Paused heartbeats!");
    		break;

    	case "unpause":
    		paused = false;
    		console.log(" # Unpaused heartbeats!");
    		break;
    }
 });
