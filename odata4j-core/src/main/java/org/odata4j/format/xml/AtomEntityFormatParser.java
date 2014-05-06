package org.odata4j.format.xml;

import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.FormatParser;

import java.io.Reader;


public class AtomEntityFormatParser implements FormatParser<OEntity> {

  protected EdmDataServices metadata;
  protected String entitySetName;
  protected OEntityKey entityKey;

  public AtomEntityFormatParser(EdmDataServices metadata, String entitySetName, OEntityKey entityKey) {
    this.metadata = metadata;
    this.entitySetName = entitySetName;
    this.entityKey = entityKey;
  }

  @Override
  public OEntity parse(Reader reader) {
    return new AtomFeedFormatParser(metadata, entitySetName, entityKey)
        .parse(reader).entries.iterator().next().getEntity();
  }
}
