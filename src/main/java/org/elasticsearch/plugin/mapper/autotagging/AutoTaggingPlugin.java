package org.elasticsearch.plugin.mapper.autotagging;

import static org.elasticsearch.common.collect.Lists.newArrayList;

import java.util.Collection;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.action.autotagging.AutoTaggingAction;
import org.elasticsearch.action.autotagging.TransportAutoTaggingAction;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.mapper.autotagging.AutoTaggingIndexModule;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.rest.autotagging.RestAutoTaggingAction;
import org.elasticsearch.service.autotagging.DocumentTaggerService;

public class AutoTaggingPlugin extends AbstractPlugin {

    public String description() {
        return "Adds the auto tagging type allowing to parse and extract tags";
    }

    public String name() {
        return "opennlp-auto-tagging";
    }

    public void onModule(RestModule module) {
        module.addRestAction(RestAutoTaggingAction.class);
    }

    public void onModule(ActionModule module) {
        module.registerAction(AutoTaggingAction.INSTANCE, TransportAutoTaggingAction.class);        
    }
    
    @SuppressWarnings({ "rawtypes" })
    @Override
    public Collection<Class<? extends LifecycleComponent>> services() {
        Collection<Class<? extends LifecycleComponent>> services = newArrayList();
        services.add(DocumentTaggerService.class);
        return services;
    }

    @Override
    public Collection<Class<? extends Module>> indexModules() {
        Collection<Class<? extends Module>> modules = newArrayList();
        modules.add(AutoTaggingIndexModule.class);
        return modules;
    }
}
