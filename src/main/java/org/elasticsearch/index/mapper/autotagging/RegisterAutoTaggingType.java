package org.elasticsearch.index.mapper.autotagging;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.AbstractIndexComponent;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.settings.IndexSettings;
import org.elasticsearch.service.autotagging.DocumentTaggerService;

public class RegisterAutoTaggingType extends AbstractIndexComponent {

	@Inject
	public RegisterAutoTaggingType(Index index, @IndexSettings Settings indexSettings, MapperService mapperService, DocumentTaggerService service) {
		super(index, indexSettings);
		mapperService.documentMapperParser().putTypeParser("autotagging", new AutoTaggingMapper.TypeParser(service));
	}

}
