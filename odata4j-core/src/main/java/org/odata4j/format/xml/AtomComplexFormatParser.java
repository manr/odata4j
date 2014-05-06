package org.odata4j.format.xml;


import org.core4j.Enumerable;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.OComplexObjects;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmPropertyBase;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmStructuralType;
import org.odata4j.edm.EdmType;
import org.odata4j.format.FormatParser;
import org.odata4j.format.Settings;
import org.odata4j.stax2.Attribute2;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.XMLEvent2;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.util.StaxUtil;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;


public class AtomComplexFormatParser extends XmlFormatParser implements FormatParser<OComplexObject> {
  private final EdmDataServices metadata;
  private final Settings settings;

  public AtomComplexFormatParser(Settings settings) {
    this.settings = settings;
    this.metadata = settings.metadata;
  }

  @Override
  public OComplexObject parse(Reader reader) {
    XMLEventReader2 eventReader = StaxUtil.newXMLEventReader(reader);

    Iterable<OProperty<?>> props = parseComplex(eventReader, new QName2(NS_DATASERVICES, settings.entitySetName), this.metadata, null);

    return OComplexObjects.create((EdmComplexType) settings.parseType, (List<OProperty<?>>) props.iterator().next().getValue());
  }

  private Iterable<OProperty<?>> parseComplex(XMLEventReader2 reader, QName2 endElementName, EdmDataServices metadata, EdmStructuralType structuralType) {
    List<OProperty<?>> rt = new ArrayList<OProperty<?>>();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(endElementName.getLocalPart())) {
        return rt;
      }

      if (event.isStartElement() && event.asStartElement().getName().getNamespaceUri().equals(NS_DATASERVICES)) {

        String name = event.asStartElement().getName().getLocalPart();
        Attribute2 typeAttribute = event.asStartElement().getAttributeByName(M_TYPE);
        Attribute2 nullAttribute = event.asStartElement().getAttributeByName(M_NULL);
        boolean isNull = nullAttribute != null && "true".equals(nullAttribute.getValue());

        OProperty<?> op = null;

        EdmType et = null;
        if (typeAttribute != null) {
          String type = typeAttribute.getValue();
          et = metadata.resolveType(type);
          if (et == null) {
            // property arrived with an unknown type
            throw new RuntimeException("unknown property type: " + type);
          }
        } else {
          EdmProperty property = (EdmProperty) structuralType.findProperty(name);
          if (property != null)
            et = property.getType();
          //else
          //  et = EdmSimpleType.STRING;  // we must support open types
        }

        if (et != null && !et.isSimple()) {
          EdmStructuralType est = (EdmStructuralType) et;
          op = OProperties.complex(name, (EdmComplexType) et, isNull ? null : Enumerable.create(parseComplex(reader, event.asStartElement().getName(), metadata, est)).toList());
        } else if (et != null && (et.isSimple())) {
          op = OProperties.parseSimple(name, (EdmSimpleType<?>) et, isNull ? null : reader.getElementText());
        } else {
          //property arrived with no type, might be a complex type
          EdmPropertyBase propBase = metadata.findEdmProperty(name);

          if (propBase != null && propBase instanceof EdmProperty) {
            EdmProperty prop = (EdmProperty) propBase;
            EdmType  edmType = prop.getType();

            if (!edmType.isSimple()) {
              EdmStructuralType est = (EdmStructuralType) et;
              op = OProperties.complex(name, (EdmComplexType) edmType, isNull ? null : Enumerable.create(parseComplex(reader, event.asStartElement().getName(), metadata, est)).toList());
            } else {
              op = OProperties.parseSimple(name, (EdmSimpleType<?>) et, isNull ? null : reader.getElementText());
            }
          } else {
            op = OProperties.parseSimple(name, (EdmSimpleType<?>) et, isNull ? null : reader.getElementText());
            //throw new RuntimeException("could not determine type of property: " + name);
          }
        }

        rt.add(op);
      }
    }

    return rt;
  }
}
