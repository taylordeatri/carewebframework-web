'use strict';

// CodeMirror web jar incorrectly configures requirejs, so we fix it below.

var root = require.s.contexts._.config.paths.codemirror[0];

if (root) {
	var i = root.indexOf('/lib/'),
		main = root.substring(i + 1);
	root = root.substring(0, i);
	require.undef('codemirror');
	require.config({packages:[{name: 'codemirror', location: root, main: main}]});
}

define('cwf-codemirror', ['cwf-core', 'cwf-widget', 'codemirror', 'css!cwf-codemirror-css.css', 'css!codemirror.css'], function(cwf, Widget, CodeMirror) { 
	
	/**
	 * Wrapper for CodeMirror
	 */
	Widget.CodeMirror = Widget.Memobox.extend({
	
		/*------------------------------ Containment ------------------------------*/

		_detach: function(destroy) {
			if (destroy && this._cm) {
				this._cm.toTextArea();
				this._cm = null;
			}
			
			this._super(destroy);
		},
		
		/*------------------------------ Lifecycle ------------------------------*/

		init: function() {
			this._super();
			this._cm = null;
		},
		
		/*------------------------------ Other ------------------------------*/
	
		format: function() {
		    var cm = this._cm,
		    	from = cm.getCursor('from'),
		    	to = cm.getCursor('to');
		    
		    if (from.line == to.line && from.ch == to.ch) {
		    	from.line = 0;
		    	to.line = cm.lastLine();
		    }
		        
    		cm.operation(function() {
				for (var i = from.line; i <= to.line; i++) {
					cm.indentLine(i);
			    }
			});
    		
    		from.ch = 0;
    		cm.setSelection(from, from);
    		cm.focus();
		},
		
		load: function(path, callback) {
			return cwf.load('codemirror/' + path, callback);
		},
		
		resync: function() {
		 	this._cm.setValue(this._value());   
		},
		
		focus: function() {
			this._cm.focus();    
		},
		
		clear: function() {
		    this._cm.setValue('');
		    this._cm.focus();
		},
		
		/*------------------------------ Rendering ------------------------------*/

		afterRender: function() {
			var cm = this._cm;
			
			setTimeout(function() {
			    cm.refresh();
			}, 1);

		},
		
		beforeRender: function() {
			var self = this;
			this._super();
			this._cm = CodeMirror.fromTextArea(this.input$()[0]);
			
			this._cm.on('changes', function() {
			    self.updateState('value', self._cm.getValue(), true);
			    self.fireChanged();
			});
			
			this._cm.setSize('100%', '100%');
		},
		
		render$: function() {
			var dom =
				'<div>'
			  + 	'<textarea id="${id}-inp" />'
			  + '</div';
			return $(this.resolveEL(dom));
		},
		
		/*------------------------------ State ------------------------------*/
		lineNumbers: function(v) {
			this._cm.setOption('lineNumbers', v);
		},
		
		mode: function(v) {
			var self = this;
			v = v ? v.toLowerCase() : null;
			
			if (!v) {
				_mode();
			} else {
				this.load('mode/' + v + '/' + v, _mode);
			}
			
			function _mode() {
				self._cm.setOption('mode', v);
			}
		},
		
		placeholder: function(v) {
			var self = this;
			this.load('addon/display/placeholder', _placeholder);
			
			function _placeholder() {
				self._cm.setOption('placeholder', v);
			}
		}
	});

	return cwf.widget;
});