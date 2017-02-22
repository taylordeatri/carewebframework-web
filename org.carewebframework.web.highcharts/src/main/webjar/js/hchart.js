'use strict';

define('cwf-hchart', ['cwf-core', 'cwf-widget', 'highcharts'], function(cwf) { 
	
	/**
	 * Wrapper for HighCharts
	 */
	cwf.widget.HChart = cwf.widget.UIWidget.extend({
	
		/*------------------------------ Events ------------------------------*/

		handleResize : function() {
			if (!this._resizing && this._chart) {
				var w$ = this.widget$;
				this._chart.setSize(w$.width(), w$.height());
			}
		},
		
		/*------------------------------ Lifecycle ------------------------------*/

		init: function() {
			this._super();
			this._chart = null;
			this._resizing = false;
		},
		
		/*------------------------------ Other ------------------------------*/

		_export : function(func) {
			if (this._chart) {
				func ? func.call(this._chart) : this._chart.exportChart();
			}
		},
	
		_global : function(options) {
			if (options)
				Highcharts.setOptions(options);
		},
		
		_print : function(func) {
			if (this._chart)
				func ? func.call(this._chart) : this._chart.print();
		},
	
		_redraw : function() {
			if (this._chart)
				this._chart.redraw();
		},
	
		_reset : function() {
			if (this._chart) {
				this._chart.destroy();
				this._chart = null;
			}
		},
		
		_run : function(options) {
			this._resizing = true;
			this._reset();
			this._chart = new Highcharts.Chart(options);
			this._resizing = false;
		},
	
		_title : function(options) {
			if (this._chart) {
				this._chart.setTitle(options.title, options.subtitle);
			}
		},
	
		/*------------------------------ Rendering ------------------------------*/

		afterRender: function() {
			this._super();
			this.widget$.on('resize', this.handleResize.bind(this));
		},
		
		render$: function() {
			return $('<div/>')
		}
	});

	return cwf.widget;
});