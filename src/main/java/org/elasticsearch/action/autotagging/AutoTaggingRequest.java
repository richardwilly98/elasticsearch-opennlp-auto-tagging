package org.elasticsearch.action.autotagging;

import java.io.IOException;

import org.elasticsearch.action.support.single.custom.SingleCustomOperationRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

public class AutoTaggingRequest extends SingleCustomOperationRequest<AutoTaggingRequest> {

    private String index;
    private String type;
    private String id;
    private String field;
    private String content;

    public static class Builder {

        private Builder builder;
        private String index;
        private String type;
        private String id;
        private String field;
        private String content;

        public Builder(String index) {
            this.index = index;
            this.builder = this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder field(String field) {
            this.field = field;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public AutoTaggingRequest build() {
            return new AutoTaggingRequest(builder);
        }
    }

    AutoTaggingRequest() {}
    
    public AutoTaggingRequest(final Builder builder) {
        this.index = builder.index;
        this.type = builder.type;
        this.id = builder.id;
        this.field = builder.field;
        this.content = builder.content;
    }

    public String getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getField() {
        return field;
    }

    public String getContent() {
        return content;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        index = in.readOptionalString();
        type = in.readOptionalString();
        id = in.readOptionalString();
        field = in.readOptionalString();
        content = in.readOptionalString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeOptionalString(getIndex());
        out.writeOptionalString(getType());
        out.writeOptionalString(getId());
        out.writeOptionalString(getField());
        out.writeOptionalString(getContent());
    }

}
