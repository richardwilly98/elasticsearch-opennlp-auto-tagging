package org.elasticsearch.index.mapper.autotagging;

import org.elasticsearch.common.inject.AbstractModule;

public class AutoTaggingIndexModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(RegisterAutoTaggingType.class).asEagerSingleton();
	}
}
