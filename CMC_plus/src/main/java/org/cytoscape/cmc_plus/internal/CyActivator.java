package org.cytoscape.cmc_plus.internal;

import java.util.Properties;
import org.cytoscape.cmc_plus.internal.task.CreateUiTaskFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator{
    public CyActivator() {
        super();
    }
    
    public void start(BundleContext bc) {
        final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
        final CreateUiTaskFactory createUiTaskFactory = new CreateUiTaskFactory(serviceRegistrar);
        
        Properties cmcPlusProps = new Properties();
        cmcPlusProps.setProperty(PREFERRED_MENU, "Apps");
        cmcPlusProps.setProperty(TITLE, "CMC+");
        registerService(bc, createUiTaskFactory, TaskFactory.class, cmcPlusProps);
    }


}