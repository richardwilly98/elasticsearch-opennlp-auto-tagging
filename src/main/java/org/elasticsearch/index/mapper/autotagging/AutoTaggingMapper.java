package org.elasticsearch.index.mapper.autotagging;

import static org.elasticsearch.index.mapper.MapperBuilders.stringField;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.base.Strings;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.FieldMapperListener;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MergeContext;
import org.elasticsearch.index.mapper.MergeMappingException;
import org.elasticsearch.index.mapper.ObjectMapperListener;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.core.StringFieldMapper;
import org.elasticsearch.index.mapper.multifield.MultiFieldMapper;
import org.elasticsearch.service.autotagging.DocumentTaggerService;

public class AutoTaggingMapper implements Mapper {

    private static ESLogger logger = ESLoggerFactory.getLogger(AutoTaggingMapper.class.getName());

    public static final String CONTENT_TYPE = "autotagging";
    public static final String TAGS_FIELD = "tags";
    private final String name;
    private final StringFieldMapper contentMapper;
    private final StringFieldMapper tagsMapper;
    private final DocumentTaggerService service;

    public AutoTaggingMapper(String name, DocumentTaggerService service, StringFieldMapper contentMapper, StringFieldMapper tagsMapper) {
        this.name = name;
        this.contentMapper = contentMapper;
        this.tagsMapper = tagsMapper;
        this.service = service;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name);
        builder.field("type", CONTENT_TYPE);
        builder.startObject("fields");
        contentMapper.toXContent(builder, params);
        tagsMapper.toXContent(builder, params);
        builder.endObject();
        builder.endObject();
        return builder;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void parse(ParseContext context) throws IOException {
        logger.debug("parse - {}", context);
        // logger.debug("allEntries.buildText: {}",
        // context.allEntries().buildText());
        logger.debug("allEntries: {}", context.allEntries().toString());
        String content = null;

        XContentParser parser = context.parser();
        XContentParser.Token token = parser.currentToken();

        String text = context.allEntries().buildText();

        if (token == XContentParser.Token.VALUE_STRING) {
            content = parser.text();
            if (Strings.isNullOrEmpty(text)) {
                text = content;
            }
            logger.debug("1. content - {}", content);
            // try {
            // byte[] b = parser.binaryValue();
            // if (b != null && b.length > 0) {
            // content = new String(b, Charset.forName("UTF-8"));
            // }
            // } catch (Exception e) {
            // }
        }

        logger.debug("text - {}", text);

//        DocumentTagger tagger = new 
        Set<String> tags = service.extractKeywords(text);
        logger.debug("tags - {}", tags);

        // context.externalValue(content);
        // contentMapper.parse(context);

        for (String tag : tags) {
            context.externalValue(tag);
            tagsMapper.parse(context);
        }
    }

    @Override
    public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
    }

    @Override
    public void traverse(FieldMapperListener fieldMapperListener) {
        contentMapper.traverse(fieldMapperListener);
        tagsMapper.traverse(fieldMapperListener);

    }

    @Override
    public void traverse(ObjectMapperListener objectMapperListener) {
    }

    @Override
    public void close() {
        contentMapper.close();
        tagsMapper.close();
    }

    public static class Defaults {
        public static final ContentPath.Type PATH_TYPE = ContentPath.Type.FULL;
    }

    public static class Builder extends Mapper.Builder<Builder, AutoTaggingMapper> {

        protected Builder builder;
        private DocumentTaggerService service;
        private StringFieldMapper.Builder contentBuilder;
        private StringFieldMapper.Builder tagsBuilder = stringField(TAGS_FIELD);

        protected Builder(String name, DocumentTaggerService service) {
            super(name);
            this.service = service;
            this.contentBuilder = stringField(name);
            this.builder = this;
        }

        public Builder content(StringFieldMapper.Builder content) {
            this.contentBuilder = content;
            return this;
        }

        public Builder tags(StringFieldMapper.Builder tags) {
            this.tagsBuilder = tags;
            return this;
        }

        public String name() {
            return this.name;
        }

        @Override
        public AutoTaggingMapper build(BuilderContext context) {
            context.path().add(name);
            StringFieldMapper contentMapper = contentBuilder.build(context);
            StringFieldMapper testMapper = tagsBuilder.build(context);
            context.path().remove();
            return new AutoTaggingMapper(name, service, contentMapper, testMapper);
        }
    }

    public static class TypeParser implements Mapper.TypeParser {

        private DocumentTaggerService service;

        public TypeParser(DocumentTaggerService service) {
            this.service = service;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext) throws MapperParsingException {
            logger.debug("TypeParser.parse - {}", name);
            AutoTaggingMapper.Builder builder = new Builder(name, service);

            for (Map.Entry<String, Object> entry : node.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                logger.debug("Field {} - {}", fieldName, fieldNode);
                if (fieldName.equals("fields")) {
                    Map<String, Object> fieldsNode = (Map<String, Object>) fieldNode;
                    for (Map.Entry<String, Object> fieldsEntry : fieldsNode.entrySet()) {
                        String propName = fieldsEntry.getKey();
                        Object propNode = fieldsEntry.getValue();
                        logger.debug("\tProperty {} - {}", propName, propNode);
                        // Check if we have a multifield here
                        boolean isMultifield = false;
                        // boolean isString = false;
                        if (propNode != null && propNode instanceof Map) {
                            Object oType = ((Map<String, Object>) propNode).get("type");
                            if (oType != null && oType.equals(MultiFieldMapper.CONTENT_TYPE)) {
                                isMultifield = true;
                            }
                            // if (oType != null &&
                            // oType.equals(StringFieldMapper.CONTENT_TYPE)) {
                            // isString = true;
                            // }
                        }

                        if (name.equals(propName)) {
                            builder.content((StringFieldMapper.Builder) parserContext.typeParser(
                                    isMultifield ? MultiFieldMapper.CONTENT_TYPE : StringFieldMapper.CONTENT_TYPE).parse(name,
                                    (Map<String, Object>) propNode, parserContext));
                        } else if (TAGS_FIELD.equals(propName)) {
                            builder.tags((StringFieldMapper.Builder) parserContext.typeParser(
                                    isMultifield ? MultiFieldMapper.CONTENT_TYPE : StringFieldMapper.CONTENT_TYPE).parse(TAGS_FIELD,
                                    (Map<String, Object>) propNode, parserContext));
                        }
                    }
                }
                // if (fieldName.equals("path")) {
                // builder.pathType(parsePathType(name, fieldNode.toString()));
                // }
            }
            return builder;
        }

    }
}
