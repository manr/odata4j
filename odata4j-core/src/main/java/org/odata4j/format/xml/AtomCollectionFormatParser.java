package org.odata4j.format.xml;


import org.core4j.Enumerable;
import org.odata4j.core.OCollection;
import org.odata4j.core.OCollections;
import org.odata4j.core.OComplexObjects;
import org.odata4j.core.OObject;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.core.OSimpleObjects;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmPropertyBase;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmStructuralType;
import org.odata4j.edm.EdmType;
import org.odata4j.format.Entry;
import org.odata4j.format.Feed;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.FormatType;
import org.odata4j.format.Settings;
import org.odata4j.stax2.Attribute2;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.StartElement2;
import org.odata4j.stax2.XMLEvent2;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.util.StaxUtil;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AtomCollectionFormatParser extends XmlFormatParser implements FormatParser<OCollection<? extends OObject>> {
  private final EdmDataServices   metadata;
  private final EdmCollectionType returnType;
  private final Settings settings;

  public AtomCollectionFormatParser(Settings settings) {
    this.settings = settings;
    this.metadata = settings.metadata;
    this.returnType = (EdmCollectionType) (settings == null ? null : settings.parseType);

  }

  @Override
  public OCollection<OObject> parse(Reader reader) {
    OCollection.Builder<OObject> collectionBuilder = OCollections.newBuilder(returnType);

    if (returnType.getItemType().getClass().isAssignableFrom(EdmEntityType.class)) {
      FormatParser<Feed> feedParser = FormatParserFactory.getParser(Feed.class, FormatType.ATOM, settings);

      Feed feed = feedParser.parse(reader);
      Iterator<Entry> feedEntries = feed.getEntries().iterator();

      while (feedEntries.hasNext()) {
        collectionBuilder.add(feedEntries.next().getEntity());
      }
    } else if (returnType.getItemType().getClass().isAssignableFrom(EdmComplexType.class)) {
      XMLEventReader2 eventReader = StaxUtil.newXMLEventReader(reader);

      while (eventReader.hasNext())
      {
        XMLEvent2 event = eventReader.nextEvent();

        if (isStartElement(event, new QName2(NS_DATASERVICES, settings.entitySetName))) {
          Iterable<OProperty<?>> props = parseComplex(eventReader, new QName2(NS_DATASERVICES, settings.entitySetName), this.metadata, null);

          for (OProperty<?> prop : props)
            collectionBuilder.add(OComplexObjects.create((EdmComplexType) returnType.getItemType(), (List<OProperty<?>>)prop.getValue()));
        }
      }
    } else if (returnType.getItemType().getClass().isAssignableFrom(EdmSimpleType.class)) {
      collectionBuilder = parseSimple(StaxUtil.newXMLEventReader(reader));
    }

    return collectionBuilder.build();
  }

  private OCollection.Builder<OObject> parseSimple(XMLEventReader2 reader) {
    List<OObject> elements = new ArrayList<OObject>();

    if (reader.hasNext())
      reader.nextEvent();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, new QName2(NS_DATASERVICES, settings.entitySetName))) {
        elements = parseEntries(reader, event.asStartElement());
      } else {
        break;
      }
    }

    OCollection.Builder<OObject> builder = OCollections.newBuilder(returnType.getItemType());

    for (OObject elem : elements)
      builder.add(elem);

    return builder;
  }

  private List<OObject> parseEntries(XMLEventReader2 reader, StartElement2 startElement2) {
    List<OObject> elements = new ArrayList<OObject>();
    OObject simpleObject = null;

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, new QName2(NS_DATASERVICES, "element"))) {
        String value = reader.getElementText();
        simpleObject = OSimpleObjects.parse((EdmSimpleType<?>) returnType.getItemType(), value);
        elements.add(simpleObject);
      }
      else if (isEndElement(event, new QName2(NS_DATASERVICES, settings.entitySetName))) {
        break;
      }
    }

    return elements;
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

    throw new RuntimeException();
  }
}
