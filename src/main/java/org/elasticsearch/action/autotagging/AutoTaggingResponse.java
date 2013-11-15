package org.elasticsearch.action.autotagging;

import java.io.IOException;
import java.util.Set;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;

public class AutoTaggingResponse extends ActionResponse implements ToXContent {

    private Set<String> tags;

    public AutoTaggingResponse() {}
    
    public AutoTaggingResponse(Set<String> tags) {
        this.tags = tags;
    }
    
    public Set<String> getTags() {
        return tags;
    }    

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startArray("tags");
        for (String tag : tags) {
              builder.value(tag);
        }
        builder.endArray();
        return builder;
    }

}
