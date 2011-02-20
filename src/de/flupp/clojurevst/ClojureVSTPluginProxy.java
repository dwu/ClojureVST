package de.flupp.clojurevst;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Hashtable;

import jvst.wrapper.VSTPluginAdapter;
import clojure.lang.Var;
import de.flupp.clojurevst.config.ClojureVSTPluginConfig;

public class ClojureVSTPluginProxy extends VSTPluginAdapter {

	private ClojureVSTPluginConfig pluginConfig;
	private Var cljProcessReplacing;
	private boolean suspendProcessing = false;
	private boolean variablesBound = false;
	
	public ClojureVSTPluginProxy(long wrapper) throws Exception {
		super(wrapper);
		
		loadPlugin(ProxyTools.getIniFileName(ProxyTools.getResourcesFolder(getLogBasePath()), getLogFileName()));
		setProgram(0);
		
		if (pluginConfig.isPluginReload()) {
			log("Starting Watcher thread to reload plugin if any .clj file changed");
			new Watcher(this).start();
		}
		
		log("[ClojureVSTPluginProxy] Successfully loaded Clojure VST plugin " + pluginConfig.getPluginNamespace() + " from file " + pluginConfig.getPluginFilename());
	}

	public void loadPlugin(String iniFileName) throws Exception {
		pluginConfig = new ClojureVSTPluginConfig(iniFileName);
		cljProcessReplacing = pluginConfig.getProcessReplacing();
		
		setNumInputs(pluginConfig.getNumInputs());
		setNumOutputs(pluginConfig.getNumOutputs());
		canProcessReplacing(pluginConfig.canProcessReplacing());
		canMono(pluginConfig.canMono());
		setUniqueID(pluginConfig.getUniqueId());
	}
	
	public int canDo(String feature) {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_CAN_DO);
		if (func == null) {
			// use generic method implementation
			return pluginConfig.canDo(feature);
		} else {
			try {
				// use method implementation in plugin
				Object result = func.invoke(feature);
				return (Integer)result;
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::canDo] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}
	}

	public int getPlugCategory() {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_GET_PLUG_CATEGORY);
		if (func == null) {
			// use generic method implementation
			return pluginConfig.getPluginCategory();
		} else {
			try {
				// use method implementation in plugin
				Object result = func.invoke();
				return (Integer)result;
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::getPlugCategory] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}
	}

	public String getProductString() {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_GET_PRODUCT_STRING);
		if (func == null) {
			// use generic method implementation
			return pluginConfig.getProductString();
		} else {
			try {
				// use method implementation in plugin
				Object result = func.invoke();
				return result.toString();
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::getProductString] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}
	}

	// TODO Only needed if plugin supports more than one category
	public String getProgramNameIndexed(int arg0, int arg1) {
		return "TODO";
	}

	public String getVendorString() {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_GET_VENDOR_STRING);
		if (func == null) {
			// use generic method implementation
			return pluginConfig.getVendorString();
		} else {
			try {
				// use method implementation in plugin
				Object result = func.invoke();
				return result.toString();
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::getVendorString] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}
	}

	public boolean setBypass(boolean arg0) {
		// TODO Currently no support for software bypass
		return false;
	}

	public boolean string2Parameter(int idx, String value) {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_STRING2PARAMETER);
		if (func == null) {
			// use generic method implementation
			try {
				setParameter(idx, Float.parseFloat(value));
				return true;
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::string2Parameter] Unable to set value " + value + " for parameter index " + idx);
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		} else {
			try {
				// use method implementation in plugin
				Object result = func.invoke(idx, value);
				return (Boolean)result;
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::string2Parameter] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}
	}

	public int getNumParams() {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_GET_NUM_PARAMS);
		if (func == null) {
			// use generic method implementation
			return pluginConfig.getNumParams();
		} else {
			try {
				// use method implementation in plugin
				Object result = func.invoke();
				return (Integer)result;
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::getNumParams] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}
	}

	public int getNumPrograms() {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_GET_NUM_PROGRAMS);
		if (func == null) {
			// use generic method implementation
			return pluginConfig.getNumPrograms();
		} else {
			try {
				// use method implementation in plugin
				Object result = func.invoke();
				return (Integer)result;
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::getNumPrograms] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}
	}

	public float getParameter(int idx) {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_GET_PARAMETER);
		if (func == null) {
			// use generic method implementation
			return pluginConfig.getParameterFloatCached(idx);
		} else {
			try {
				// use method implementation in plugin
				Object result = func.invoke();
				return ((Double)result).floatValue();
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::getParameter] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}
	}

	public String getParameterDisplay(int idx) {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_GET_PARAMETER_DISPLAY);
		if (func == null) {
			// use generic method implementation
			if (pluginConfig.getParameter(idx).hasMultiplier()) {
				return Float.toString(pluginConfig.getParameterFloatCached(idx) * pluginConfig.getParameter(idx).getMultiplier());
			} else {
				return Float.toString(pluginConfig.getParameterFloatCached(idx));
			}
		} else {
			try {
				// use method implementation in plugin
				Object result = func.invoke(idx);
				return result.toString();
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::getParameterDisplay] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}
	}

	public String getParameterLabel(int idx) {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_GET_PARAMETER_LABEL);
		if (func == null) {
			// use generic method implementation
			return pluginConfig.getParameter(idx).getLabel();
		} else {
			try {
				// use method implementation in plugin
				Object result = func.invoke(idx);
				return result.toString();
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::getParameterLabel] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}	
	}

	public String getParameterName(int idx) {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_GET_PARAMETER_NAME);
		if (func == null) {
			// use generic method implementation
			return pluginConfig.getParameter(idx).getName();
		} else {
			try {
				// use method implementation in plugin
				Object result = func.invoke(idx);
				return result.toString();
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::getParameterName] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}	
	}

	public int getProgram() {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_GET_PROGRAM);
		if (func == null) {
			// use generic method implementation
			return pluginConfig.getCurrentProgram();
		} else {
			try {
				// use method implementation in plugin
				Object result = func.invoke();
				return (Integer)result;
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::getProgram] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}	
	}

	public String getProgramName() {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_GET_PROGRAM_NAME);
		if (func == null) {
			// use generic method implementation
			return pluginConfig.getProgram(pluginConfig.getCurrentProgram()).getName();
		} else {
			try {
				// use method implementation in plugin
				Object result = func.invoke();
				return result.toString();
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::getProgramName] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}
	}

	public void processReplacing(float[][] inputs, float[][] outputs, int samples) {
		if (suspendProcessing)
			return;
		if (!variablesBound)
			pluginConfig.bindThreadBoundVariables();
		
		try { 
			cljProcessReplacing.invoke(inputs, outputs);
		} catch (Exception e) {
			log("[CloureVSTPluginProxy::processReplacing] Cannot execute process-replacing.");
			log(ProxyTools.getStackTraceText(e));
			throw(new IllegalStateException(e));
		}
	}

	/** 
	 * Generic implementation of accumulating process. Should only be used
	 * if the plugin does not implement "process" itself and the host
	 * application demands for it.
	 * 
	 * TODO: needs testing
	 * 
	 * @see jvst.wrapper.VSTPluginAdapter#process(float[][], float[][], int)
	 */
	public void process(float[][] inputs, float[][] outputs, int samples) {
		if (suspendProcessing)
			return;
		if (!variablesBound)
			pluginConfig.bindThreadBoundVariables();

		float[][] originalOutputs = new float[outputs.length][outputs[0].length]; //assume quadratic (note: not rectangular!) array
		System.arraycopy(outputs, 0, originalOutputs, 0, outputs.length);
		
		//fake process() through a call to processReplacing
		processReplacing(inputs, outputs, samples);
		
		// accumulate output and original output
		for (int i = 0; i < outputs.length; i++) {
			for (int j = 0; j < outputs[i].length; j++) {
				outputs[i][j] = outputs[i][j] + originalOutputs[i][j];
				outputs[i][j] = outputs[i][j] + originalOutputs[i][j];
			}
		}
	}
	
	public void setParameter(int idx, float value) {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_SET_PARAMETER);
		if (func == null) {
			// use generic method implementation
			pluginConfig.setParameter(idx, value);
		} else {
			try {
				// use method implementation in plugi
				func.invoke(idx, value);
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::setParameter] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}	
	}
	
	public void setProgram(int currentProgram) {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_SET_PROGRAM);
		if (func == null) {
			// use generic method implementation
			pluginConfig.setProgram(currentProgram);
		} else {
			try {
				// use method implementation in plugi
				func.invoke(currentProgram);
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::setProgram] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}	
	}
	
	public void setProgramName(String programName) {
		Var func = pluginConfig.getMethod(PluginConstants.METHOD_SET_PROGRAM_NAME);
		if (func == null) {
			// use generic method implementation
			pluginConfig.setCurrentProgramName(programName);
		} else {
			try {
				// use method implementation in plugi
				func.invoke(programName);
			} catch (Exception e) {
				log("[ClojureVSTPluginProxy::setProgramName] Exception during method execution");
				log(e.getStackTrace().toString());
				throw(new IllegalStateException(e));
			}
		}
	}
	
	
	
	private class Watcher extends Thread {
		Hashtable<File, Long> toWatch = new Hashtable<File, Long>();

		ClojureVSTPluginProxy proxy = null;

		public Watcher(ClojureVSTPluginProxy proxy) throws Exception {
			this.proxy = proxy;

			//find the location where we loaded the main plugin file from...
			File f = new File(this.getContextClassLoader().getResource(proxy.pluginConfig.getPluginFilename()).toURI()).getParentFile();
			log("Watching all .clj files in path: " + f + " for changes");
			File[] files = f.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {return name.toLowerCase().endsWith(".clj");}
			});
			for (File file : files) {
				toWatch.put(file, file.lastModified());
			}
		}
		    
		public void run() {
			boolean modified = false;    	  

			while(true) {
				try {
					//log("### +++ ### next round...");
					for (File file : toWatch.keySet()) {
						if (file.lastModified() > toWatch.get(file)) {
							log("### File: " + file.getName() + " was just modified!");
							toWatch.put(file, file.lastModified());
							modified = true;
						}
					}
					if (modified) reloadPlugin();    		  
					modified = false;
					Thread.sleep(1000);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		    
		private void reloadPlugin() throws Exception {
			int currentProgram = proxy.getProgram();
			
			try {
				log("Reloading Plugin!");

				proxy.suspendProcessing = true;
				proxy.loadPlugin(ProxyTools.getIniFileName(ProxyTools.getResourcesFolder(getLogBasePath()), getLogFileName()));
				proxy.setProgram(currentProgram);

				log("all good :-)");
				proxy.suspendProcessing = false;
			} 
			catch (Throwable t) {
				log(ProxyTools.getStackTraceText(t));
				// enable process() calls on the old plugin again
				log("### Plugin problem detected, using the version that ran before. Check the error log below for details");
				proxy.suspendProcessing = false;
			}
		}
	}
	
}
