package org.odata4j.core;

import org.odata4j.exceptions.ODataProducerException;

/**
 *
 */
public interface OBatchRequest {

  /**
   * Adds a create request.
   * @param createRequest the create request
   * @return the batch-request builder
   */
  OBatchRequest createRequest(OCreateRequest<OEntity> createRequest);

  /**
   * Adds a modify request.
   * @param modifyRequest the modify request
   * @return the batch-request builder
   */
  OBatchRequest modifyRequest(OModifyRequest<OEntity> modifyRequest);

  /**
   * Adds a delete request.
   * @param deleteRequest the delete request
   * @return the batch-request builder
   */
  OBatchRequest deleteRequest(OEntityDeleteRequest deleteRequest);

  /**
   * Adds an entity get request.
   * @param getRequest the get request
   * @return the batch-request builder
   */
  OBatchRequest entityGetRequest(OEntityGetRequest getRequest);

  /**
   * Adds an entity query request.
   * @param queryRequest the query request
   * @return the batch-request builder
   */
  OBatchRequest entityQueryRequest(OQueryRequest<?> queryRequest);

  /**
   * Sends the batch-request to the OData service.
   * @throws ODataProducerException
   */
  String execute() throws ODataProducerException;

}
