require(['cwf-core'], function(cwf) {
	cwf.event.sendToServer({type: 'log', data: 'External script was executed.'});
	console.log('External script was executed.');
});