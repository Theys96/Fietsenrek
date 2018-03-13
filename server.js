var amqp = require('amqplib/callback_api');

var toServerExchange = "fietsenrek_servers";
var fromServerExchange = "fietsenrek_racks";

var racks = [];
var rackInfo = {};
var spots = {};

function checkRack(data, ch, queue) {
  rackName = data[0];
  if (racks.indexOf(rackName) == -1) {
    racks.push(rackName);
  }
  rackInfo[rackName] = [Date.now(), rackName, data[1].length];
  spots[rackName] = data[1];
}

amqp.connect('amqp://localhost', function(err, conn) {
  conn.createChannel(function(err, ch) {

    ch.assertExchange(toServerExchange, 'fanout', {durable: false});

    ch.assertQueue('', {exclusive: true}, function(err, q) {
      ch.bindQueue(q.queue, toServerExchange, '');
      ch.consume(q.queue, function(msg) {
        data = JSON.parse(msg.content);
        switch(data[0]) {
          case "heartbeat":
            checkRack(data.slice(1), ch, q.queue);
            break;
        }
      }, {noAck: true});
    });
  });
});

setInterval(function() {
	for (i = 0; i < racks.length; i++) {
		rack = racks[i][0];
		if (rackInfo[racks][0] + 5000 < Date.now()) {
			console.log(" # Rack \"%s\" has been removed.", rackInfo[racks][1]);
			rackInfo[rack] = null;
			spots[rack] = null;
			racks.splice(i, 1);
		}
	}
}, 5000);

var stdin = process.openStdin();
stdin.addListener("data", function(d) {
    string = d.toString().trim();
    switch(string) {
    	case "list":
    		if (racks.length == 0) {
    			console.log(" # There are no racks right now.");
    		}
    		for (i = 0; i < racks.length; i++) {
    			info = rackInfo[racks[i]];
    			console.log(" # ", i, "\t", info[1], "\tsize=", info[2], "\tlast_heartbeat=", info[0]);
    			console.log(spots[racks[i]]);
    		}
    		break;
    }
 });
