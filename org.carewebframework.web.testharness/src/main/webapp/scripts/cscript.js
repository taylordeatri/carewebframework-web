require(['cwf-core'], function(cwf) {
	var msg = 'External client script was executed.';
	cwf.event.sendToServer({type: 'log', data: msg});
	console.log(msg);
});