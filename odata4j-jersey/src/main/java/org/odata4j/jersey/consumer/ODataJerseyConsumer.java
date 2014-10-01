package org.odata4j.jersey.consumer;

import javax.ws.rs.ext.RuntimeDelegate;

import org.odata4j.consumer.AbstractODataConsumer;
import org.odata4j.consumer.ODataClient;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.behaviors.OClientBehavior;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.Throwables;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.FormatType;
import org.odata4j.format.xml.EdmxFormatParser;
import org.odata4j.internal.BOMWorkaroundReader;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.util.StaxUtil;

import java.io.*;

/**
 * OData consumer based on Jersey.
 */
public class ODataJerseyConsumer extends AbstractODataConsumer {

  private ODataJerseyClient client;

  private ODataJerseyConsumer(FormatType type, String serviceRootUri, JerseyClientFactory clientFactory, OClientBehavior... behaviors) {
    super(serviceRootUri);

    // ensure that a correct JAX-RS implementation (Jersey, server or default) is loaded
    if (!(RuntimeDelegate.getInstance() instanceof com.sun.jersey.core.spi.factory.AbstractRuntimeDelegate))
      RuntimeDelegate.setInstance(new com.sun.ws.rs.ext.RuntimeDelegateImpl());

    this.client = new ODataJerseyClient(type, clientFactory, behaviors);
  }

  private ODataJerseyConsumer(FormatType type, String serviceRootUri, JerseyClientFactory clientFactory, EdmDataServices metadata, OClientBehavior... behaviors) {
    super(serviceRootUri, metadata);

    // ensure that a correct JAX-RS implementation (Jersey, server or default) is loaded
    if (!(RuntimeDelegate.getInstance() instanceof com.sun.jersey.core.spi.factory.AbstractRuntimeDelegate))
      RuntimeDelegate.setInstance(new com.sun.ws.rs.ext.RuntimeDelegateImpl());

    this.client = new ODataJerseyClient(type, clientFactory, behaviors);
  }

  @Override
  protected ODataClient getClient() {
    return client;
  }

  /**
   * Builder for {@link ODataJerseyConsumer} objects.
   */
  public static class Builder implements ODataConsumer.Builder {

    private FormatType formatType;
    private String serviceRootUri;
    private JerseyClientFactory clientFactory;
    private OClientBehavior[] clientBehaviors;
    private EdmDataServices   dataServices;

    private Builder(String serviceRootUri) {
      this.serviceRootUri = serviceRootUri;
      this.formatType = FormatType.ATOM;
      this.clientFactory = DefaultJerseyClientFactory.INSTANCE;
    }

    /**
     * Sets a preferred {@link FormatType}. Defaults to {@code FormatType.ATOM}.
     *
     * @param formatType  the format type
     * @return this builder
     */
    public Builder setFormatType(FormatType formatType) {
      this.formatType = formatType;
      return this;
    }

    /**
     * Sets a specific {@link JerseyClientFactory}.
     *
     * @param clientFactory  the jersey client factory
     * @return this builder
     */
    public Builder setClientFactory(JerseyClientFactory clientFactory) {
      this.clientFactory = clientFactory;
      return this;
    }

    /**
     * Sets one or more client behaviors.
     *
     * <p>Client behaviors transform http requests to interact with services that require custom extensions.
     *
     * @param clientBehaviors  the client behaviors
     * @return this builder
     */
    public Builder setClientBehaviors(OClientBehavior... clientBehaviors) {
      this.clientBehaviors = clientBehaviors;
      return this;
    }

    public Builder setMetadata(EdmDataServices dataServices) {
      this.dataServices = dataServices;
      return this;
    }

    public Builder setMetadata(File edmxFile) {
      EdmDataServices edmDataServices = new EdmxFormatParser().parseMetadata(toXml(edmxFile));
      this.dataServices = edmDataServices;
      return this;
    }

    /**
     * Builds the {@link ODataJerseyConsumer} object.
     *
     * @return a new OData consumer
     */
    public ODataJerseyConsumer build() {
      if (this.dataServices == null) {
        if (this.clientBehaviors != null)
          return new ODataJerseyConsumer(this.formatType, this.serviceRootUri, this.clientFactory, this.clientBehaviors);
        else
          return new ODataJerseyConsumer(this.formatType, this.serviceRootUri, this.clientFactory);
      } else {
        if (this.clientBehaviors != null)
          return new ODataJerseyConsumer(this.formatType, this.serviceRootUri, this.clientFactory, this.dataServices, this.clientBehaviors);
        else
          return new ODataJerseyConsumer(this.formatType, this.serviceRootUri, this.clientFactory, this.dataServices);
      }
    }

    private XMLEventReader2 toXml(File metadata) {
      try {
        return StaxUtil.newXMLEventReader(new BOMWorkaroundReader(new InputStreamReader(new FileInputStream(metadata), ODataConstants.Charsets.Upper.UTF_8)));
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }
  }

  /**
   * Constructs a new builder for an {@link ODataJerseyConsumer} object.
   *
   * @param serviceRootUri  the OData service root uri
   */
  public static Builder newBuilder(String serviceRootUri) {
    return new Builder(serviceRootUri);
  }

  /**
   * Creates a new consumer for the given OData service uri.
   *
   * <p>Wrapper for {@code ODataJerseyConsumer.newBuilder(serviceRootUri).build()}.
   *
   * @param serviceRootUri  the service uri <p>e.g. <code>http://services.odata.org/Northwind/Northwind.svc/</code></p>
   * @return a new OData consumer
   */
  public static ODataJerseyConsumer create(String serviceRootUri) {
    return ODataJerseyConsumer.newBuilder(serviceRootUri).build();
  }
}
