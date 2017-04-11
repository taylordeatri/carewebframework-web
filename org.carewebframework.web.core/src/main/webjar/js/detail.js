/**
 * Polyfill for details/summary elements.
 */
'use strict';

define('cwf-detail', ['cwf-core', 'cwf-widget', 'css!cwf-detail-css'], function(cwf) { 

	/******************************************************************************************************************
	 * A detail widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Detail = cwf.widget.LabeledWidget.extend({
		
		/*------------------------------ Events ------------------------------*/
		
		handleToggle: function(event) {
			var open = this.widget$.attr('open');
			
			if (this.setState('open', open)) {
				this.trigger(open ? 'open' : 'close');
			}
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({open: false});
			this.forwardToServer('open close');
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.widget$.on('toggle.cwf', this.handleToggle.bind(this));
		},
		
		render$: function() {
			 var dom = '<details>'
				    + 	'<summary id="${id}-lbl"/>'
					+ '</details>';

			return $(this.resolveEL(dom));
		},
	
		/*------------------------------ State ------------------------------*/
		
		open: function(v) {
			this.attr('open', v);
		}
	});
	
	if (!hasSupport()) {
		document.documentElement.className += ' cwf-no-details';
		window.addEventListener('click', clickHandler);
	}
	
	/**
	 * Click handler for `<summary>` tags
	 */
	function clickHandler(e) {
		if (e.target.nodeName.toLowerCase() === 'summary') {
			var details = e.target.parentNode;

			if (details) {
				if (details.getAttribute('open')) {
					details.open = false;
					details.removeAttribute('open');
				} else {
					details.open = true;
					details.setAttribute('open', 'open');
				}
			}
		}
	}

	/**
	 * Check for native `<details>` support.
	 */
	function hasSupport() {
		var el = document.createElement('details');

		if (!('open' in el)) {
			return false;
		}

		el.innerHTML = '<summary>a</summary>b';
		document.body.appendChild(el);
		var diff = el.offsetHeight;
		el.open = true;
		var result = (diff !== el.offsetHeight);
		document.body.removeChild(el);
		return result;
	}

	return cwf.widget;
});