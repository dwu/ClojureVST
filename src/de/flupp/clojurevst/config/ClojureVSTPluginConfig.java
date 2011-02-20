package de.flupp.clojurevst.config;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jvst.wrapper.VSTPluginAdapter;
import clojure.lang.APersistentMap;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import de.flupp.clojurevst.PluginConstants;
import de.flupp.clojurevst.ProxyTools;

public class ClojureVSTPluginConfig {

	private String pluginNamespace;
	private String pluginFilename;
	private boolean pluginReload;

	private int uniqueId;
	private String productString;
	private String vendorString;
	private String vendorVersion;
	private int pluginCategory;
	private int numInputs;
	private int numOutputs;
	private boolean canProcessReplacing;
	private boolean canMono;
	private int currentProgram;

	private List<String> features;
	private List<Parameter> parameters;
	private List<Program> programs;
	private Map<String, Var> methods;
	
	private String iniFileName;
	private Var cljLoadString;
	private Var cljProcessReplacing;
	private Var cljNsPublics;

	public ClojureVSTPluginConfig(String iniFileName) throws Exception {
		features = new ArrayList<String>();
		parameters = new ArrayList<Parameter>();
		programs = new ArrayList<Program>();
		methods = new HashMap<String, Var>();
		
		this.iniFileName = iniFileName;
		
		cljLoadString = RT.var("clojure.core", "load-string");
		cljNsPublics = RT.var("clojure.core", "ns-publics");
		
		parseIni();
		parseConfig();
		parseMethods();
		parseParameters();
		
		VSTPluginAdapter.log("[PluginConfig] Successfully configured plugin " 
				+ this.getPluginNamespace()	+ "/" + this.getPluginFilename() + "\n" 
				+ this.toString());
	}

	public Var getProcessReplacing() {
		return cljProcessReplacing;
	}

	public String getPluginNamespace() {
		return pluginNamespace;
	}

	public void setPluginNamespace(String pluginNamespace) {
		this.pluginNamespace = pluginNamespace;
	}

	public String getPluginFilename() {
		return pluginFilename;
	}

	public void setPluginFilename(String pluginFilename) {
		this.pluginFilename = pluginFilename;
	}
	
	public boolean isPluginReload() {
		return pluginReload;
	}

	public void setPluginReload(boolean pluginReload) {
		this.pluginReload = pluginReload;
	}
	
	public int getPluginCategory() {
		return pluginCategory;
	}

	public void setPluginCategory(int pluginCategory) {
		this.pluginCategory = pluginCategory;
	}

	public int getUniqueId() {
		return uniqueId;
	}
	
	public void setUniqueId(String uniqueId) {
		//extract the first 4 chars and compute a 32bit integer unique id
		int id = 0;
		for (int i=0; i<uniqueId.length() && i<4; i++) 
			id |= (uniqueId.charAt(i) << (i*8));
		
		this.uniqueId = id;
	}
	
	public String getProductString() {
		return productString;
	}
	
	public void setProductString(String productString) {
		this.productString = productString;
	}
	
	public String getVendorString() {
		return vendorString;
	}

	public void setVendorString(String vendorString) {
		this.vendorString = vendorString;
	}

	public String getVendorVersion() {
		return vendorVersion;
	}

	public void setVendorVersion(String vendorVersion) {
		this.vendorVersion = vendorVersion;
	}

	public int getNumInputs() {
		return numInputs;
	}
	
	public void setNumInputs(int numInputs) {
		this.numInputs = numInputs;
	}
	
	public int getNumOutputs() {
		return numOutputs;
	}
	
	public void setNumOutputs(int numOutputs) {
		this.numOutputs = numOutputs;
	}
	
	public boolean canProcessReplacing() {
		return canProcessReplacing;
	}
	
	public void setCanProcessReplacing(boolean canProcessReplacing) {
		this.canProcessReplacing = canProcessReplacing;
	}
	
	public boolean canMono() {
		return canMono;
	}
	
	public void setCanMono(boolean canMono) {
		this.canMono = canMono;
	}
	
	public int getCurrentProgram() {
		return currentProgram;
	}

	public void setProgram(int currentProgram) {
		this.currentProgram = currentProgram;
		for (int i = 0; i < parameters.size(); i++) {
			resetParameter(i, parameters.get(i).getValue(currentProgram));
		}
	}

	public void addProgram(Program program) {
		programs.add(program);
	}

	public Program getProgram(int idx) {
		return programs.get(idx);
	}
	
	public int getNumPrograms() {
		return programs.size();
	}
	
	public void addFeature(String feature) {
		features.add(feature);
	}
	
	public int canDo(String feature) {
		if (features.contains(feature)) {
			return 1;
		} else {
			return -1;
		}
	}
	
	public void addParameter(Parameter param) {
		parameters.add(param);
	}
	
	public void addParameter(String name, String label, String unit) {
		Parameter param = new Parameter(name, label, unit);
		parameters.add(param);
	}

	public void setParameter(int index, float value) {
		resetParameter(index, value);
		parameters.get(index).setValue(currentProgram, value);
	}
	
	public void resetParameter(int index, float value) {
		try {
			Var cljParameter = getCljVarForParameter(index);
			cljParameter.doReset(value);
		} catch (Exception e) {
			VSTPluginAdapter.log("[PluginConfig::setParameter] Cannot set parameter " + parameters.get(0).getName() + " to value " + value);
			VSTPluginAdapter.log(ProxyTools.getStackTraceText(e));
			throw(new IllegalStateException(e));
		}	
	}
	
	public Parameter getParameter(int idx) {
		return parameters.get(idx);
	}
	
	public int getNumParams() {
		return parameters.size();
	}
	
	public void parseIni() {
		try {
			Properties iniProperties = new Properties();
			iniProperties.load(new FileInputStream(iniFileName));
			
			setPluginFilename(iniProperties.getProperty(PluginConstants.INI_PLUGIN_FILENAME));
			setPluginNamespace(iniProperties.getProperty(PluginConstants.INI_PLUGIN_NAMESPACE));
			setPluginReload(Boolean.parseBoolean(iniProperties.getProperty(PluginConstants.INI_PLUGIN_RELOAD)));
			
		} catch (Exception e) {
			VSTPluginAdapter.log("[PluginConfig::parseIni] Error parsing INI file " + iniFileName);
			VSTPluginAdapter.log(ProxyTools.getStackTraceText(e));
			throw(new IllegalStateException(e));
		}		
	}
	
	public void parseConfig() {
		try {
			RT.loadResourceScript(getPluginFilename());
			Var cljConfig = RT.var(getPluginNamespace(), PluginConstants.PLUGIN_CONFIG);
			
			Object result = cljConfig.get();
			if (result instanceof APersistentMap) {
				APersistentMap config = (APersistentMap) result;
				
				setUniqueId(config.get(Keyword.intern(Symbol.intern(PluginConstants.PLUGIN_CONFIG_UNIQUE_ID))).toString());
				setProductString(config.get(Keyword.intern(Symbol.intern(PluginConstants.PLUGIN_CONFIG_PRODUCT_STRING))).toString());
				setNumInputs(Integer.valueOf(config.get(Keyword.intern(Symbol.intern(PluginConstants.PLUGIN_CONFIG_NUM_INPUTS))).toString()));
				setNumOutputs(Integer.valueOf(config.get(Keyword.intern(Symbol.intern(PluginConstants.PLUGIN_CONFIG_NUM_OUTPUTS))).toString()));
				setCanProcessReplacing(Boolean.valueOf(config.get(Keyword.intern(Symbol.intern(PluginConstants.PLUGIN_CONFIG_CAN_PROCESS_REPLACING))).toString()));
				setCanMono(Boolean.valueOf(config.get(Keyword.intern(Symbol.intern(PluginConstants.PLUGIN_CONFIG_CAN_MONO))).toString()));
				setVendorString(config.get(Keyword.intern(Symbol.intern(PluginConstants.PLUGIN_CONFIG_VENDOR_STRING))).toString());
				setPluginCategory(Integer.valueOf(config.get(Keyword.intern(Symbol.intern(PluginConstants.PLUGIN_CONFIG_PLUGIN_CATEGORY))).toString()));

				result = config.get(Keyword.intern(Symbol.intern(PluginConstants.PLUGIN_CONFIG_CAN_DO)));
				if (result instanceof PersistentVector) {
					PersistentVector canDo = (PersistentVector) result;
					for (Object o : canDo) {
						if (o instanceof String) {
							this.addFeature((String)o);
						}
					}
				}
				else throw new IllegalStateException("can-do not of type PersistentVector");
				
				result = config.get(Keyword.intern(Symbol.intern(PluginConstants.PLUGIN_CONFIG_PROGRAMS)));
				if (result instanceof PersistentVector) {
					PersistentVector progs = (PersistentVector) result;
					for (Object o : progs) {
						if (o instanceof String) {
							Program p = new Program();
							p.setName(o.toString());
							programs.add(p);
						}
					}
				} 
				else {
					Program p = new Program();
					p.setName("Default");
					programs.add(p);
				}
			}
			else throw new IllegalStateException("var plugin-config not of type APersistentMap");
		} 
		catch (IllegalStateException ix) {
			throw ix;
		}
		catch (Exception e) {
			VSTPluginAdapter.log("[PluginConfig::parseConfig] Error parsing config in plugin " + getPluginNamespace() + "/" + getPluginFilename());
			VSTPluginAdapter.log(ProxyTools.getStackTraceText(e));
			throw(new IllegalStateException(e));
		}
	}

	private float getParameterFloatInternal(int index) {
		try {
			Var cljParam = getCljVarForParameter(index);
			Object result = cljParam.get();
			if (result instanceof Double) {
				return ((Double)result).floatValue();
			}
		} catch (Exception e) {
			VSTPluginAdapter.log("[PluginConfig::getParameterFloatInternal] Cannot get value for parameter " + parameters.get(0).getName() + " in plugin " + getPluginFilename());
			VSTPluginAdapter.log(ProxyTools.getStackTraceText(e));
			throw(new IllegalStateException(e));
		}
		return -1;
	}
	
	/**
	 * Get current parameter value from cache without querying the clojure runtime.
	 * Used e.g. for displaying purposes.
	 * 
	 * @param index Index of queried parameter
	 * @return Float value of parameter
	 */
	public float getParameterFloatCached(int index) {
		return parameters.get(index).getValue(currentProgram);
	}

	public void parseParameters() {
		int index = 0;
		try {
			Object result = cljNsPublics.invoke(Symbol.intern(getPluginNamespace()));
			if (result instanceof APersistentMap) {
				APersistentMap publics = (APersistentMap) result;
				for (Object publicElement : publics.values()) {
					if (publicElement instanceof Var) {
						IPersistentMap meta = ((Var) publicElement).meta();
						if (meta.containsKey(Keyword.intern(Symbol.intern(PluginConstants.PARAM_NAME)))) {
							Parameter parameter = new Parameter();
							parameters.add(index, parameter);
							parameter.setVariableName(meta.valAt(Keyword.intern(Symbol.intern("name"))).toString());
							parameter.setName(meta.valAt(Keyword.intern(Symbol.intern(PluginConstants.PARAM_NAME))).toString());
							parameter.setLabel(meta.valAt(Keyword.intern(Symbol.intern(PluginConstants.PARAM_LABEL))).toString());
							
							// check whether parameter multiplier exists
							if (meta.containsKey(Keyword.intern(Symbol.intern(PluginConstants.PARAM_MULTIPLIER)))) {
								parameter.setMultiplier(Float.valueOf(meta.valAt(Keyword.intern(Symbol.intern(PluginConstants.PARAM_MULTIPLIER))).toString()));
								parameter.setHasMultiplier(true);
							}
							
							// check whether there are program values defined
							if (meta.containsKey(Keyword.intern(Symbol.intern(PluginConstants.PARAM_VALUE_IN_PROGRAMS)))) {
								result = meta.valAt(Keyword.intern(Symbol.intern(PluginConstants.PARAM_VALUE_IN_PROGRAMS)));
								if (result instanceof PersistentVector) {
									PersistentVector values = (PersistentVector) result;
									
									for (int programIndex = 0; programIndex < values.size(); programIndex++) {
										Object o = values.get(programIndex);
										if (o instanceof Double) {
											parameter.addValue(programIndex, ((Double)o).floatValue());
										}
									}
								}
							} else {
								parameter.addValue(currentProgram, getParameterFloatInternal(index));
							}
							
							index++;				
						}
					}
				}
			}
		} catch (Exception e) {
			VSTPluginAdapter.log("[PluginConfig::parseParameters] Cannot parse params in plugin " + getPluginNamespace() + "/" + getPluginFilename());
			VSTPluginAdapter.log(ProxyTools.getStackTraceText(e));
			throw(new IllegalStateException(e));
		}
	}

	public void bindThreadBoundVariables() {
		try {
			Object result = cljNsPublics.invoke(Symbol.intern(getPluginNamespace()));
			if (result instanceof APersistentMap) {
				APersistentMap publics = (APersistentMap) result;
				for (Object publicElement : publics.values()) {
					if (publicElement instanceof Var) {
						IPersistentMap meta = ((Var) publicElement).meta();
						if (meta.containsKey(Keyword.intern(Symbol.intern(PluginConstants.PARAM_THREAD_BOUND)))) {
							if ("1".equals(meta.valAt(Keyword.intern(Symbol.intern(PluginConstants.PARAM_THREAD_BOUND))).toString())) {
								Var.pushThreadBindings(RT.map(publicElement, ((Var)publicElement).get()));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			VSTPluginAdapter.log("[PluginConfig::parseParameters] Cannot parse params in plugin " + getPluginNamespace() + "/" + getPluginFilename());
			VSTPluginAdapter.log(ProxyTools.getStackTraceText(e));
			throw(new IllegalStateException(e));
		}
	}
	
	private Var getCljVar(String namespace, String variableName) {
		try {
			return RT.var(namespace, variableName);
		} catch (Exception e) {
			VSTPluginAdapter.log("[PluginConfig::getCljVar] Cannot retrieve variable " + namespace + "/" +  variableName);
			VSTPluginAdapter.log(ProxyTools.getStackTraceText(e));
			throw(new IllegalStateException(e));
		}
	}
	
	private Var getCljVarForParameter(int index) {
		return getCljVar(getPluginNamespace(), parameters.get(index).getVariableName());
	}
	
	private String getParameterName(int index) {
		return parameters.get(index).getName();
	}
	
	private String getParameterVariableName(int index) {
		return parameters.get(index).getName();
	}
	
	private void parseMethods() {
		try {
			Object result = cljNsPublics.invoke(Symbol.intern(getPluginNamespace()));
			if (result instanceof APersistentMap) {
				APersistentMap publics = (APersistentMap) result;
				for (Object publicElement : publics.values()) {
					if (publicElement instanceof Var) {
						IPersistentMap meta = ((Var) publicElement).meta();
						if (meta.containsKey(Keyword.intern(Symbol.intern("arglists")))) {
							String methodName = meta.valAt(Keyword.intern(Symbol.intern("name"))).toString(); 
							if (PluginConstants.METHOD_PROCESS_REPLACING.equals(methodName)) {
								// process-replacing
								cljProcessReplacing = RT.var(getPluginNamespace(), PluginConstants.METHOD_PROCESS_REPLACING);
							} else if (PluginConstants.METHOD_GET_PARAMETER_DISPLAY.equals(methodName)) {
								// get-parameter-display
								methods.put(PluginConstants.METHOD_GET_PARAMETER_DISPLAY, RT.var(getPluginNamespace(), PluginConstants.METHOD_GET_PARAMETER_DISPLAY));
							} else if (PluginConstants.METHOD_GET_PARAMETER_LABEL.equals(methodName)) {
								// get-parameter-label
								methods.put(PluginConstants.METHOD_GET_PARAMETER_LABEL, RT.var(getPluginNamespace(), PluginConstants.METHOD_GET_PARAMETER_LABEL));
							} else if (PluginConstants.METHOD_CAN_DO.equals(methodName)) {
								// can-do
								methods.put(PluginConstants.METHOD_CAN_DO, RT.var(getPluginNamespace(), PluginConstants.METHOD_CAN_DO));
							} else if (PluginConstants.METHOD_GET_PLUG_CATEGORY.equals(methodName)) {
								// get-plug-category
								methods.put(PluginConstants.METHOD_GET_PLUG_CATEGORY, RT.var(getPluginNamespace(),PluginConstants.METHOD_GET_PLUG_CATEGORY));
							} else if (PluginConstants.METHOD_GET_PRODUCT_STRING.equals(methodName)) {
								// get-product-string
								methods.put(PluginConstants.METHOD_GET_PRODUCT_STRING, RT.var(getPluginNamespace(), PluginConstants.METHOD_GET_PRODUCT_STRING));
							} else if (PluginConstants.METHOD_GET_PROGRAM_NAME_INDEXED.equals(methodName)) {
								// get-program-name-indexed
								methods.put(PluginConstants.METHOD_GET_PROGRAM_NAME_INDEXED, RT.var(getPluginNamespace(), PluginConstants.METHOD_GET_PROGRAM_NAME_INDEXED));
							} else if (PluginConstants.METHOD_GET_VENDOR_STRING.equals(methodName)) {
								// get-vendor-string
								methods.put(PluginConstants.METHOD_GET_VENDOR_STRING, RT.var(getPluginNamespace(), PluginConstants.METHOD_GET_VENDOR_STRING));
							} else if (PluginConstants.METHOD_SET_BYPASS.equals(methodName)) {
								// set-bypass
								methods.put(PluginConstants.METHOD_SET_BYPASS, RT.var(getPluginNamespace(), PluginConstants.METHOD_SET_BYPASS));
							} else if (PluginConstants.METHOD_STRING2PARAMETER.equals(methodName)) {
								// string2parameter
								methods.put(PluginConstants.METHOD_STRING2PARAMETER, RT.var(getPluginNamespace(), PluginConstants.METHOD_STRING2PARAMETER));
							} else if (PluginConstants.METHOD_GET_NUM_PARAMS.equals(methodName)) {
								// get-num-params
								methods.put(PluginConstants.METHOD_GET_NUM_PARAMS, RT.var(getPluginNamespace(), PluginConstants.METHOD_GET_NUM_PARAMS));
							} else if (PluginConstants.METHOD_GET_NUM_PROGRAMS.equals(methodName)) {
								// get-num-programs
								methods.put(PluginConstants.METHOD_GET_NUM_PROGRAMS, RT.var(getPluginNamespace(), PluginConstants.METHOD_GET_NUM_PROGRAMS));
							} else if (PluginConstants.METHOD_GET_PARAMETER.equals(methodName)) {
								// get-parameter
								methods.put(PluginConstants.METHOD_GET_PARAMETER, RT.var(getPluginNamespace(), PluginConstants.METHOD_GET_PARAMETER));
							} else if (PluginConstants.METHOD_GET_PARAMETER_NAME.equals(methodName)) {
								// get-parameter-name
								methods.put(PluginConstants.METHOD_GET_PARAMETER_NAME, RT.var(getPluginNamespace(), PluginConstants.METHOD_GET_PARAMETER_NAME));
							} else if (PluginConstants.METHOD_GET_PROGRAM.equals(methodName)) {
								// get-program
								methods.put(PluginConstants.METHOD_GET_PROGRAM, RT.var(getPluginNamespace(), PluginConstants.METHOD_GET_PROGRAM));
							} else if (PluginConstants.METHOD_GET_PROGRAM_NAME.equals(methodName)) {
								// get-program-name
								methods.put(PluginConstants.METHOD_GET_PROGRAM_NAME, RT.var(getPluginNamespace(), PluginConstants.METHOD_GET_PROGRAM_NAME));
							} else if (PluginConstants.METHOD_SET_PARAMETER.equals(methodName)) {
								// set-parameter
								methods.put(PluginConstants.METHOD_SET_PARAMETER, RT.var(getPluginNamespace(), PluginConstants.METHOD_SET_PARAMETER));
							} else if (PluginConstants.METHOD_SET_PROGRAM.equals(methodName)) {
								// set-program
								methods.put(PluginConstants.METHOD_SET_PROGRAM, RT.var(getPluginFilename(), PluginConstants.METHOD_SET_PROGRAM));
							} else if (PluginConstants.METHOD_SET_PROGRAM_NAME.equals(methodName)) {
								// set-program-name
								methods.put(PluginConstants.METHOD_SET_PROGRAM_NAME, RT.var(getPluginNamespace(), PluginConstants.METHOD_SET_PROGRAM_NAME));
							}
						}
					}
				}
			}
			
			// check for required plugin methods
			if (cljProcessReplacing == null) {
				VSTPluginAdapter.log("[PluginConfig::parseMethods] Plugin does not define method process-replacing.");
				throw(new IllegalStateException("[PluginConfig::parseMethods] Plugin does not define method process-replacing."));
			}
		}  catch (Exception e) {
			VSTPluginAdapter.log("[PluginConfig::parseMethods] Cannot parse methods in plugin " + getPluginNamespace() + "/" + getPluginFilename());
			VSTPluginAdapter.log(ProxyTools.getStackTraceText(e));
			throw(new IllegalStateException(e));
		}
	}
	
	public void setCurrentProgramName(String programName) {
		programs.get(currentProgram).setName(programName);
	}

	public Var getMethod(String methodName) {
		return methods.get(methodName);
	}
	
	public String toString() {
	    final String TAB = "    ";
	    
	    String retValue = "";
	    
	    retValue = "PluginConfig ( "
	        + super.toString() + TAB
	        + "pluginNamespace = " + this.pluginNamespace + TAB
	        + "pluginFilename = " + this.pluginFilename + TAB
	        + "pluginCategory = " + this.pluginCategory + TAB
	        + "uniqueId = " + this.uniqueId + TAB
	        + "productString = " + this.productString + TAB
	        + "vendorString = " + this.vendorString + TAB
	        + "vendorVersion = " + this.vendorVersion + TAB
	        + "numInputs = " + this.numInputs + TAB
	        + "numOutputs = " + this.numOutputs + TAB
	        + "canProcessReplacing = " + this.canProcessReplacing + TAB
	        + "canMono = " + this.canMono + TAB
	        + "currentProgram = " + this.currentProgram + TAB
	        + "features = " + this.features + TAB
	        + "parameters = " + this.parameters + TAB
	        + "programs = " + this.programs + TAB
	        + "iniFileName = " + this.iniFileName + TAB
	        + "cljLoadString = " + this.cljLoadString + TAB
	        + "cljProcessReplacing = " + this.cljProcessReplacing + TAB
	        + "cljNsPublics = " + this.cljNsPublics + TAB
	        + " )";
	
	    return retValue;
	}

}
