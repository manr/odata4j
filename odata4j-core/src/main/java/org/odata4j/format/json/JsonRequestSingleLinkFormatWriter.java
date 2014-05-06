package org.odata4j.format.json;


import org.odata4j.format.SingleLink;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.io.Writer;

public class JsonRequestSingleLinkFormatWriter extends JsonFormatWriter<SingleLink> {

  public JsonRequestSingleLinkFormatWriter(String jsonpCallback) {
    super(jsonpCallback);
  }

  @Override
  public String getContentType() {
    return MediaType.APPLICATION_JSON;
  }

  @Override
  public void write(UriInfo uriInfo, Writer w, SingleLink target) {
    JsonWriter jw = new JsonWriter(w);
    if (getJsonpCallback() != null) {
      jw.startCallback(getJsonpCallback());
    }

    writeContent(uriInfo, jw, target);
  }

  @Override
  protected void writeContent(UriInfo uriInfo, JsonWriter jw, SingleLink link) {
    jw.startObject();
    {
      jw.writeName("uri");
      jw.writeString(link.getUri());
    }
    jw.endObject();
  }
}
