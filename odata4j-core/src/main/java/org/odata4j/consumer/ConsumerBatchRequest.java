package org.odata4j.consumer;

import org.core4j.Enumerable;
import org.core4j.Predicate1;
import org.odata4j.core.OBatchRequest;
import org.odata4j.core.OCreateRequest;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityDeleteRequest;
import org.odata4j.core.OEntityGetRequest;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OModifyRequest;
import org.odata4j.core.OProperty;
import org.odata4j.core.OQueryRequest;
import org.odata4j.core.Throwables;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.format.Entry;
import org.odata4j.internal.InternalUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class ConsumerBatchRequest implements OBatchRequest {
  private final ODataClient client;
  private final String serviceRootUri;
  private final EdmDataServices metadata;
  private final List<ODataClientRequest> clientRequests;

  public ConsumerBatchRequest(ODataClient client, String serviceRootUri, EdmDataServices metadata)
  {
    this.client = client;
    this.serviceRootUri = serviceRootUri;
    this.metadata = metadata;
    this.clientRequests = new ArrayList<ODataClientRequest>();
  }

  @Override
  public OBatchRequest createRequest(OCreateRequest<OEntity> createRequest) {
    ConsumerCreateEntityRequest<OEntity> request = (ConsumerCreateEntityRequest<OEntity>) createRequest;

    EdmEntitySet ees = metadata.getEdmEntitySet(request.entitySetName);
    Entry entry = client.createRequestEntry(ees, null, request.props, request.links);

    StringBuilder url = new StringBuilder(request.serviceRootUri);
    if (request.parent != null) {
      url.append(InternalUtil.getEntityRelId(request.parent))
          .append("/")
          .append(request.navProperty);
    } else {
      url.append(request.entitySetName);
    }

    clientRequests.add(ODataClientRequest.post(url.toString(), entry));
    return this;
  }

  @Override
  public OBatchRequest modifyRequest(OModifyRequest<OEntity> modifyRequest) {
    ConsumerEntityModificationRequest<OEntity> request = (ConsumerEntityModificationRequest<OEntity>) modifyRequest;

    List<OProperty<?>> requestProps = request.props;
    if (request.updateRoot != null) {
      OEntity updateRootEntity = (OEntity) request.updateRoot;
      requestProps = Enumerable.create(updateRootEntity.getProperties()).toList();
      for (final OProperty<?> prop : request.props) {
        OProperty<?> requestProp = Enumerable.create(requestProps).firstOrNull(new Predicate1<OProperty<?>>() {
          public boolean apply(OProperty<?> input) {
            return input.getName().equals(prop.getName());
          }
        });
        requestProps.remove(requestProp);
        requestProps.add(prop);
      }
    }

    OEntityKey entityKey = Enumerable.create(request.segments).last().key;
    Entry entry = client.createRequestEntry(request.entitySet, entityKey, requestProps, request.links);

    String path = Enumerable.create(request.segments).join("/");

    ODataClientRequest clientRequest = request.updateRoot != null ?
        ODataClientRequest.put(serviceRootUri + path, entry) :
        ODataClientRequest.merge(serviceRootUri + path, entry);

    if (request.ifMatch != null)
      clientRequest.header(ODataConstants.Headers.IF_MATCH, request.ifMatch);

    clientRequests.add(clientRequest);
    return this;
  }

  @Override
  public OBatchRequest deleteRequest(OEntityDeleteRequest deleteRequest) {
    ConsumerDeleteEntityRequest request = (ConsumerDeleteEntityRequest) deleteRequest;
    String path = Enumerable.create(request.getSegments()).join("/");

    ODataClientRequest clientRequest = ODataClientRequest.delete(serviceRootUri + path);

    if (request.ifMatch != null)
      clientRequest.header(ODataConstants.Headers.IF_MATCH, request.ifMatch);

    clientRequests.add(clientRequest);
    return this;
  }

  @Override
  public OBatchRequest entityGetRequest(OEntityGetRequest getRequest) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OBatchRequest entityQueryRequest(OQueryRequest<?> queryRequest) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String execute() throws ODataProducerException {
    ODataClientBatchRequest request = ODataClientBatchRequest.create(serviceRootUri + "$batch", clientRequests);
    ODataClientResponse response = client.executeBatch(request);
    Reader reader = client.getFeedReader(response);

    StringBuilder out = new StringBuilder();
    if (reader != null) {
      BufferedReader bufferedReader = new BufferedReader(reader);
      char[] buf = new char[1024];
      try {
        int c;
        while ((c = bufferedReader.read(buf, 0, 1024)) != -1) {
          out.append(buf, 0, c);
        }
      } catch (IOException e) {
        Throwables.propagate(e);
      }
    }

    return out.toString();
  }

}
