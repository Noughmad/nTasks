default: coffee

coffee:
	coffee --output js/ --join ntasks --compile src/{secret,base,models,views}.coffee

optimize:
	closure --compilation_level ADVANCED_OPTIMIZATIONS --js_output_file lib/ntasks.js --js js/underscore.js --js js/backbone.js --js js/ntasks.js

