define('cwf-react-test', ['react', 'react-dom'],  
	function(React, ReactDOM) {
		var i = 0;
		
		return React.createClass({
			getInitialState: function() {
				return {text: 'Hello World #' + ++i};
			},
			
			setText: function(text) {
				this.setState({text: text});
			},
			
			render: function() {
				return React.createElement('h1', {}, this.state.text);
			}
		})
	}
);