'use strict';

define('cwf-upload', ['cwf-core', 'cwf-widget'], function(cwf) { 
	
	/******************************************************************************************************************
	 * Base class for file uploader
	 ******************************************************************************************************************/ 
	
	cwf.widget.Upload = cwf.widget.UIWidget.extend({

		/*------------------------------ Events ------------------------------*/
		
		changeHandler: function(event) {
			var files = event.target.files,
				self = this;
			
			if (files) {
				this.rerender();
				
				_.forEach(files, function(file) {
					var reader = new FileReader();
					
					reader.onload = function(event) {
						self.trigger('upload', {file: file.name, blob: reader.result});
					};
					
					reader.readAsArrayBuffer(file);
				});
			}
			
			return false;
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({multiple: false, accept: null});
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.widget$.on('change', this.changeHandler.bind(this));
		},
		
		render$: function() {
			return $('<input type="file">');
		},
		
		/*------------------------------ State ------------------------------*/

		accept: function(v) {
			this.attr('accept', v);
		},
		
		multiple: function(v) {
			this.attr('multiple', v);
		},
		
		maxSize: function(v) {
		}
		
	});
		
	return cwf.widget;
});