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
    private Integer max;

    public AutoTaggingRequest() {
    }

    public AutoTaggingRequest(String index) {
        this.index = index;
    }

    public AutoTaggingRequest index(String index) {
        this.index = index;
        return this;
    }

    public AutoTaggingRequest type(String type) {
        this.type = type;
        return this;
    }

    public AutoTaggingRequest id(String id) {
        this.id = id;
        return this;
    }

    public AutoTaggingRequest field(String field) {
        this.field = field;
        return this;
    }

    public AutoTaggingRequest content(String content) {
        this.content = content;
        return this;
    }

    public AutoTaggingRequest max(Integer max) {
        this.max = max;
        return this;
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

    public Integer getMax() {
        return max;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        index = in.readOptionalString();
        type = in.readOptionalString();
        id = in.readOptionalString();
        field = in.readOptionalString();
        content = in.readOptionalString();
        max = in.readInt();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeOptionalString(getIndex());
        out.writeOptionalString(getType());
        out.writeOptionalString(getId());
        out.writeOptionalString(getField());
        out.writeOptionalString(getContent());
        out.writeInt(max);
    }

}
