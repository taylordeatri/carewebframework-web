'use strict';

define('cwf-angular', ['cwf-core', 'cwf-widget', 'cwf-angular-bootstrap', 'core-js/client/shim', '@angular/common', '@angular/core', '@angular/platform-browser', '@angular/platform-browser-dynamic', 'zone.js', 'rxjs'], 
	function(cwf, wgt, bootstrap, shim, common, core, platform_browser, platform_browser_dynamic, zone, rxjs) { 

	cwf.debug ? null : core.enableProdMode();
	
	return { 
		
	AngularWidget: cwf.widget.UIWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		destroy: function() {
			this._ngModuleRef ? this._ngModuleRef.destroy() : null;
			this._super();
		},
		
		/*------------------------------ Other ------------------------------*/
		
		ngInvoke: function(functionName, args) {
			return this._appContext.invoke(functionName, args);
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			var src = this.getState('src'),
				id = "#" + this.id,
				self = this;
			
			if (src) {
				System.import(src).then(function(module) {
					self._appContext = new bootstrap.AppContext(module, id);
					self._appContext.bootstrap().then(function(ngModuleRef) {
						self._ngModuleRef = ngModuleRef
					});;
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