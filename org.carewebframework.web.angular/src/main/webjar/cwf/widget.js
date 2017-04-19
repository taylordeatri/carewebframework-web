'use strict';

define('cwf-angular', ['cwf-core', 'cwf-widget', 'cwf-angular-bootstrap', 'core-js/client/shim', '@angular/common', '@angular/core', '@angular/platform-browser', '@angular/platform-browser-dynamic', 'zone.js', 'rxjs'], 
	function(cwf, wgt, bootstrap, shim, common, core, platform_browser, platform_browser_dynamic, zone, rxjs) { 

	return { 
		
	AngularWidget: cwf.widget.UIWidget.extend({
		
		/*------------------------------ Rendering ------------------------------*/
			
		afterRender: function() {
			var src = this.getState('src'),
				id = "#" + this.id;
			
			if (src) {
				System.import(src).then(function(module) {
					var appContext = new bootstrap.AppContext(module, id);
					appContext.bootstrap();
				});
			}
		},
		
		render$: function() {
			return $('<div />');
		},
		
		/*------------------------------ State ------------------------------*/
		
		src: function(v) {
			this.rerender();
		}
	
	})};
});