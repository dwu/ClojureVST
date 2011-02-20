package de.flupp.clojurevst;

public class PluginConstants {

	// Plugin configuration (Clojure)
	public static final String PLUGIN_CONFIG = "plugin-config";
	public static final String PLUGIN_CONFIG_CAN_DO = "can-do";
	public static final String PLUGIN_CONFIG_PARAMETERS = "params";
	public static final String PLUGIN_CONFIG_UNIQUE_ID = "unique-id";
	public static final String PLUGIN_CONFIG_PRODUCT_STRING = "product";
	public static final String PLUGIN_CONFIG_NUM_INPUTS = "num-inputs";
	public static final String PLUGIN_CONFIG_NUM_OUTPUTS = "num-outputs";
	public static final String PLUGIN_CONFIG_CAN_PROCESS_REPLACING = "can-process-replacing";
	public static final String PLUGIN_CONFIG_CAN_MONO = "can-mono";
	public static final String PLUGIN_CONFIG_VENDOR_STRING = "vendor";
	public static final String PLUGIN_CONFIG_PLUGIN_CATEGORY = "plugin-category";
	public static final String PLUGIN_CONFIG_PROGRAMS = "programs";
	
	// Plugin configuration (INI-File)
	public static final String INI_PLUGIN_FILENAME = "ClojureVSTPluginFile";
	public static final String INI_PLUGIN_NAMESPACE = "ClojureVSTPluginNamespace";
	public static final String INI_PLUGIN_RELOAD = "ClojureVSTPluginReload";
	
	// Parameter metadata
	public static final String PARAM_NAME = "parameter-name";
	public static final String PARAM_LABEL = "parameter-label";
	public static final String PARAM_MULTIPLIER = "parameter-display-multiplier";
	public static final String PARAM_VALUE_IN_PROGRAMS = "value-in-programs";
	public static final String PARAM_THREAD_BOUND = "thread-bound";
	
	// Plugin methods
	public static final String METHOD_PROCESS_REPLACING = "process-replacing";
	public static final String METHOD_GET_PARAMETER_DISPLAY = "get-parameter-display";
	public static final String METHOD_GET_PARAMETER_LABEL = "get-parameter-label";
	public static final String METHOD_CAN_DO = "can-do";
	public static final String METHOD_GET_PLUG_CATEGORY = "get-plug-category";
	public static final String METHOD_GET_PRODUCT_STRING = "get-product-string";
	public static final String METHOD_GET_PROGRAM_NAME_INDEXED = "get-program-name-indexed";
	public static final String METHOD_GET_VENDOR_STRING = "get-vendor-strin";
	public static final String METHOD_SET_BYPASS = "set-bypass";
	public static final String METHOD_STRING2PARAMETER = "string2parameter";
	public static final String METHOD_GET_NUM_PARAMS = "get-num-params";
	public static final String METHOD_GET_NUM_PROGRAMS = "get-num-programs";
	public static final String METHOD_GET_PARAMETER = "get-parameter";
	public static final String METHOD_GET_PARAMETER_NAME = "get-parameter-name";
	public static final String METHOD_GET_PROGRAM = "get-program";
	public static final String METHOD_GET_PROGRAM_NAME = "get-program-name";
	public static final String METHOD_SET_PARAMETER = "set-parameter";
	public static final String METHOD_SET_PROGRAM = "set-program";
	public static final String METHOD_SET_PROGRAM_NAME = "set-program-name";
	
}
