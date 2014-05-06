package org.odata4j.consumer;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ODataClientBatchRequest extends ODataClientRequest {
  private final List<ODataClientRequest> requests;

  public ODataClientBatchRequest(String url, Map<String, String> headers, Map<String, String> queryParams, List<ODataClientRequest> requests) {
    super("POST", url, headers, queryParams, null);
    this.requests = requests == null ? new ArrayList<ODataClientRequest>() : requests;
  }

  /**
   * Gets the batch requests.
   *
   * @return the batch requests
   */
  public List<ODataClientRequest> getRequests() {
    return requests;
  }

  /**
   * Creates a batch request.
   *
   * @param url the endpoint url
   * @param requests the requests to batch
   * @return the request builder
   */
  public static ODataClientBatchRequest create(String url, List<ODataClientRequest> requests)
  {
    return new ODataClientBatchRequest(url, null, null, requests);
  }
}
