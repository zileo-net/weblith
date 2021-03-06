package io.weblith.core.results;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import io.weblith.core.results.Result.RenderResponse;

public class TextResult extends AbstractResult<TextResult> implements RenderResponse {

    protected final String content;

    public TextResult(String content) {
        super(TextResult.class, MediaType.TEXT_PLAIN, Status.OK);
        this.content = content;
    }

    public TextResult(Exception exception) {
        this(exception.getMessage() != null ? exception.getMessage() : exception.toString());
    }

    public TextResult(Object content) {
        this(String.valueOf(content));
    }

    @Override
    public void write(OutputStream entityStream) throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
            inputStream.transferTo(entityStream);
        }
    }

}