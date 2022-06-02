package com.windhoverlabs.yamcs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.io.InputStream;
import java.util.Map;
import java.io.FileInputStream;
import java.io.File;
import java.util.LinkedHashMap;
import java.nio.file.Paths;

import org.yamcs.AbstractYamcsService;
import org.yamcs.parameter.BooleanValue;
import org.yamcs.parameter.DoubleValue;
import org.yamcs.parameter.ParameterValue;
import org.yamcs.parameter.SInt32Value;
import org.yamcs.parameter.SInt64Value;
import org.yamcs.parameter.StringValue;
import org.yamcs.parameter.SystemParametersProducer;
import org.yamcs.Spec;
import org.yamcs.Spec.OptionType;
import org.yamcs.InitException;
import org.yamcs.ConfigurationException;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.events.EventProducer;
import org.yamcs.events.EventProducerFactory;
import org.yamcs.parameter.SystemParametersService;
import org.yamcs.protobuf.Yamcs.Value.Type;
import org.yamcs.time.TimeService;

import static org.yamcs.xtce.XtceDb.YAMCS_SPACESYSTEM_NAME;
import org.yaml.snakeyaml.Yaml;
import org.yamcs.xtce.Parameter;
import org.yamcs.xtce.XtceDb;
import org.yamcs.mdb.XtceDbFactory;


public class RegistryService extends AbstractYamcsService implements SystemParametersProducer {
    TimeService    timeService;
    private String yamcsInstance;
    private String name;
    private String inputFile;
    protected EventProducer eventProducer;
    private SystemParametersService sysParamService;
    private XtceDb xtceDb = null;
    private List<ParameterValue> pvlist = new ArrayList<>();
    private boolean paramsCollected = false;
    private long gentime = 0;

    @Override
    public Spec getSpec() {
        Spec spec = new Spec();
        spec.addOption("file", OptionType.STRING).withRequired(true);
        return spec;
    }

    @Override
    public void init(String yamcsInstance, String serviceName, YConfiguration config) throws InitException {
        super.init(yamcsInstance, serviceName, config);

        this.yamcsInstance = yamcsInstance;
        this.name = serviceName;
        this.timeService = YamcsServer.getTimeService(yamcsInstance);
        this.gentime = timeService.getMissionTime();
        this.xtceDb = XtceDbFactory.getInstance(yamcsInstance);
      
        this.eventProducer = EventProducerFactory.getEventProducer(this.yamcsInstance);
        this.eventProducer.setSource("Registry");
        
        this.inputFile = config.getString("file");

        setupSysVariables();
    }

    @Override
    protected void doStart() {
        notifyStarted();
    }

    
    @Override
    protected void doStop() {
        notifyStopped();
    }
    
    
    @SuppressWarnings("unchecked")
	protected void processMapFlat(String namespace, Map<String, Object> data) {  
        for (String key : data.keySet()) {
        	String newPath = namespace + key;
	        
    		if((key != null) && (data.get(key) != null)) {
    			if(data.get(key).getClass() == LinkedHashMap.class) {
                    processMapFlat(newPath + "/", (Map<String, Object>) data.get(key));
    			} else if(data.get(key).getClass() == java.lang.Integer.class) {
    				Parameter param = xtceDb.createSystemParameter(newPath, sysParamService.getBasicType(Type.DOUBLE),  "");
    		        ParameterValue pv = new ParameterValue(param);
    		        pv.setGenerationTime(gentime);
    		        pv.setDoubleValue(((java.lang.Integer)data.get(key)).intValue());
    		        this.pvlist.add(pv);
    			} else if(data.get(key).getClass() == java.lang.String.class) {
    				Parameter param = xtceDb.createSystemParameter(newPath, sysParamService.getBasicType(Type.STRING),  "");
    		        ParameterValue pv = new ParameterValue(param);
    		        pv.setGenerationTime(gentime);
    		        pv.setStringValue((String)data.get(key));
    		        this.pvlist.add(pv);
    			} else if(data.get(key).getClass() == java.lang.Boolean.class) {
    				Parameter param = xtceDb.createSystemParameter(newPath, sysParamService.getBasicType(Type.BOOLEAN),  "");
    		        ParameterValue pv = new ParameterValue(param);
    		        pv.setGenerationTime(gentime);
    		        pv.setBooleanValue(((java.lang.Boolean)data.get(key)).booleanValue());
    		        this.pvlist.add(pv);
    			} else if(data.get(key).getClass() == java.lang.Long.class) {
    				Parameter param = xtceDb.createSystemParameter(newPath, sysParamService.getBasicType(Type.DOUBLE),  "");
    		        ParameterValue pv = new ParameterValue(param);
    		        pv.setGenerationTime(gentime);
    		        pv.setDoubleValue(((java.lang.Long)data.get(key)).longValue());
    		        this.pvlist.add(pv);
    			} else if(data.get(key).getClass() == java.lang.Double.class) {
    				Parameter param = xtceDb.createSystemParameter(newPath, sysParamService.getBasicType(Type.DOUBLE),  "");
    		        ParameterValue pv = new ParameterValue(param);
    		        pv.setGenerationTime(gentime);
    		        pv.setDoubleValue(((java.lang.Double)data.get(key)).doubleValue());
    		        this.pvlist.add(pv);
    			} else if(data.get(key).getClass() == java.util.ArrayList.class) {
    				eventProducer.sendWarning("Arrays not yet supported.  Ignoring '" + newPath + "'.");
    			} else {
    				eventProducer.sendWarning("Unknown data type (" + data.get(key).getClass() + ") encountered.  Ignoring '" + newPath + "'.");
    			}
    		}
        }
    }
    
    
    @SuppressWarnings("unchecked")
	protected void setupSysVariables() {
        this.sysParamService = SystemParametersService.getInstance(yamcsInstance);
        if(this.sysParamService != null) {
        	sysParamService.registerProducer(this);
            
            try {
                InputStream inputStream = new FileInputStream(new File(Paths.get("").toAbsolutePath().toString() + "/" + this.inputFile));
                String namespace = YAMCS_SPACESYSTEM_NAME + "/registry/" + name + "/";
                
                Yaml yaml = new Yaml();
                Map<String, Object> data = yaml.load(inputStream);
                
                for (String key : data.keySet()) {
                	if(data.get(key).getClass() == LinkedHashMap.class) {
        				String newNamespace = namespace + key;
                    	
                        processMapFlat(newNamespace + "/", (Map<String, Object>)data.get(key));
                	}
                }        
            }  catch (java.io.FileNotFoundException e) {
                throw new ConfigurationException("Error encountered", e.toString(), e);
            } 
        } else {
            log.info("System variables collector not defined for instance {} ", yamcsInstance);
        }
    }

    
    public Collection<org.yamcs.parameter.ParameterValue> getSystemParameters() {        
        if(paramsCollected == false) {
        	paramsCollected = true;
        	return pvlist;
        } else {
        	return Arrays.asList();
        }
    }
}

