package com.saic.uicds.core.infrastructure.util;

import gov.ucore.ucore.x20.AgentEventRelationshipType;
import gov.ucore.ucore.x20.AgentRefType;
import gov.ucore.ucore.x20.CauseOfRelationshipType;
import gov.ucore.ucore.x20.CircleByCenterPointType;
import gov.ucore.ucore.x20.CollectionType;
import gov.ucore.ucore.x20.ContentMetadataType;
import gov.ucore.ucore.x20.DigestDocument;
import gov.ucore.ucore.x20.DigestType;
import gov.ucore.ucore.x20.EntityLocationExtendedRelationshipType;
import gov.ucore.ucore.x20.EntityLocationRelationshipType;
import gov.ucore.ucore.x20.EntityRefType;
import gov.ucore.ucore.x20.EntityType;
import gov.ucore.ucore.x20.EventLocationRelationshipType;
import gov.ucore.ucore.x20.EventRefType;
import gov.ucore.ucore.x20.EventType;
import gov.ucore.ucore.x20.GeoLocationType;
import gov.ucore.ucore.x20.IdentifierType;
import gov.ucore.ucore.x20.LineStringType;
import gov.ucore.ucore.x20.LocationRefType;
import gov.ucore.ucore.x20.LocationType;
import gov.ucore.ucore.x20.OrganizationType;
import gov.ucore.ucore.x20.PointType;
import gov.ucore.ucore.x20.PolygonType;
import gov.ucore.ucore.x20.RelationshipType;
import gov.ucore.ucore.x20.SimplePropertyType;
import gov.ucore.ucore.x20.ThingRefType;
import gov.ucore.ucore.x20.ThingType;
import gov.ucore.ucore.x20.TimeInstantType;
import gov.ucore.ucore.x20.WhatType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.LinearRingType;

import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.usersmarts.geo.gml.GMLDomModule;
import com.usersmarts.xmf2.Configuration;
import com.usersmarts.xmf2.MarshalContext;
import com.vividsolutions.jts.geom.Geometry;

public class DigestHelper implements InfrastructureNamespaces, DigestConstant {

	protected DigestDocument digest;

	private static Configuration gmlParseCfg = new Configuration(
			GMLDomModule.class);

	Logger log = LoggerFactory.getLogger(this.getClass());

	public DigestHelper() {

		super();
		digest = DigestDocument.Factory.newInstance();
		digest.addNewDigest();
	}

	public byte[] getBytes() {

		return digest.isNil() ? null : digest.toString().getBytes();
	}

	public DigestDocument getDigest() {

		return digest;
	}

	public boolean isNil() {

		return digest.isNil();
	}

	public void setEvent(EventType event) {

		// add an Event
		XmlUtil.substitute(digest.getDigest().addNewThingAbstract(), NS_UCORE,
				S_Event, EventType.type, event);
	}

	public void setEvent(String eventId, String descriptor, String identifier,
			String[] codespace, ContentMetadataType metadata,
			SimplePropertyType property) {

		EventType event = EventType.Factory.newInstance();
		event.setId(eventId);

		// set the Identifier for the Event
		if (identifier != null) {
			IdentifierType id = event.addNewIdentifier();
			id.setStringValue(identifier);
			if (codespace.length == 2) {
				id.setCodespace(codespace[0]);
				id.setCode(codespace[1]);
				id.addNewLabel().setStringValue("ID");
			} else if (codespace.length == 3) {
				id.setCodespace(codespace[0]);
				id.setCode(codespace[1]);
				id.addNewLabel().setStringValue(codespace[2]);
			} else {
				id.addNewLabel().setStringValue("label");
			}
		}

		if (descriptor != null) {
			event.addNewDescriptor().setStringValue(descriptor);
		}
		if (metadata != null) {
			event.setMetadata(metadata);
		}
		if (property != null) {
			event.addNewSimpleProperty().set(property);
		}

		this.setEvent(event);
	}

	public EventType getEvent(String eventId) {

		ThingType[] things = digest.getDigest().getThingAbstractArray();
		for (ThingType thing : things) {
			if (thing.getId().equalsIgnoreCase(eventId.trim())
					&& (thing instanceof EventType)) {
				return (EventType) thing;
			}
		}
		return null;
	}

	public static Set<ThingType> getThingsByWhatType(DigestType digest,
			String codespace, String code) {

		ThingType[] things = digest.getThingAbstractArray();
		Set<ThingType> results = new HashSet<ThingType>();
		for (ThingType thing : things) {
			if (objectHasWhatType(codespace, code, null, null, thing)) {
				results.add(thing);
			}
		}
		return results;
	}

	public static EventType getFirstEventWithActivityNameIdentifier(
			DigestType digest) {

		ThingType[] things = digest.getThingAbstractArray();
		for (ThingType thing : things) {
			if (thing instanceof EventType) {
				XmlObject[] ids = thing.selectChildren(IdentifierType.type
						.getName().getNamespaceURI(), "Identifier");
				if (ids.length != 0) {
					for (XmlObject object : ids) {
						if (((IdentifierType) object).getCode().equals(
								"ActivityName")) {
							return (EventType) thing;
						}
					}
				}
			}
		}
		return null;
	}

	public void addSimplePropertyToThing(ThingType thing, String codespace,
			String code, String label, String value) {

		SimplePropertyType property = SimplePropertyType.Factory.newInstance();
		if (codespace != null)
			property.setCodespace(codespace);
		if (code != null)
			property.setCode(code);
		if (label != null)
			property.addNewLabel().setStringValue(label);
		if (value != null)
			property.setStringValue(value);
		thing.addNewSimpleProperty().set(property);
	}

	public static SimplePropertyType getSimplePropertyFromThing(
			ThingType thing, String codespace, String code, String label,
			String value) {

		if (thing == null) {
			return null;
		}

		SimplePropertyType result = null;
		XmlObject[] props = thing.selectChildren(SimplePropertyType.type
				.getName().getNamespaceURI(), "SimpleProperty");
		for (XmlObject prop : props) {
			SimplePropertyType property = (SimplePropertyType) prop;
			if (simplePropertyMatches(property, codespace, code, label, value)) {
				result = property;
				break;
			}
		}
		return result;
	}

	protected static boolean simplePropertyMatches(SimplePropertyType property,
			String codespace, String code, String label, String value) {

		// must have at least a label
		if (label == null) {
			return false;
		}

		if (property.getLabel().getStringValue().equals(label)) {
			boolean codespaceOK = false;
			boolean codeOK = false;
			boolean valueOK = false;
			if (codespace != null) {
				if (property.getCodespace().equals(codespace)) {
					codespaceOK = true;
				}
			} else {
				codespaceOK = true;
			}
			if (code != null) {
				if (property.getCode().equals(code)) {
					codeOK = true;
				}
			} else {
				codeOK = true;
			}
			if (value != null) {
				if (property.getStringValue().equals(value)) {
					valueOK = true;
				}
			} else {
				valueOK = true;
			}
			return codespaceOK && codeOK && valueOK;
		}
		return false;
	}

	public void setEntity(EntityType entity) {

		// add an Entity
		XmlUtil.substitute(digest.getDigest().addNewThingAbstract(), NS_UCORE,
				S_Entity, EntityType.type, entity);
	}

	public void setOrganization(OrganizationType org) {

		XmlUtil.substitute(digest.getDigest().addNewThingAbstract(), NS_UCORE,
				S_Organization, OrganizationType.type, org);
	}

	public void setWhatForEvent(WhatType theWhat, String eventId) {

		ThingType[] things = digest.getDigest().getThingAbstractArray();
		for (ThingType thing : things) {
			if (thing.getId().equals(eventId)) {

				WhatType[] whats = null;
				if (thing instanceof EventType) {
					whats = ((EventType) thing).getWhatArray();
				} else if (thing instanceof EntityType) {
					whats = ((EntityType) thing).getWhatArray();
				} else if (thing instanceof CollectionType) {
					whats = ((CollectionType) thing).getWhatArray();
				}
				boolean found = false;
				for (WhatType what : whats) {
					if (what.equals(theWhat)) {
						found = true;
						break;
					}
				}
				if (!found) {
					if (thing instanceof EventType) {
						((EventType) thing).addNewWhat().set(theWhat);
					} else if (thing instanceof EntityType) {
						((EntityType) thing).addNewWhat().set(theWhat);
					} else if (thing instanceof CollectionType) {
						((CollectionType) thing).addNewWhat().set(theWhat);
					}
				}
			}
		}
	}

	// The LocatedAt Relationship is used to associate an Entity with a time and
	// a place.
	public void setLocatedAt(String entityId, String locationId, Calendar cal) {

		EntityLocationExtendedRelationshipType locatedAt = EntityLocationExtendedRelationshipType.Factory
				.newInstance();
		locatedAt.setId(UUIDUtil.getID(S_LocatedAt));

		if (cal != null) {
			// TRAC #272
			// UCore's TimeInstant is a union of several date, time, and
			// date-time types
			// The value is converted into a Java Calendar object which, when
			// passed to xmlbeans
			// uses the first match in the union (generally date or date+offset)
			// To overcome this,
			// the following code constructs a valid iso 8601 (xs:datTime)
			// string and then
			// invokes TimeInstantType's parse method to construct the Value
			// element.
			//
			// The following code normalizes the date and time such that a
			// complete
			// datetime object is constructed including a locale offset
			// (timezone).
			// This ensures meaningful sorting and searching within UICDS.

			// normalized iso 8601 datetime string (with offset)
			SimpleDateFormat iso8601Format = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssZ");

			// use the current time and offset to fill in any missing blanks.
			Calendar now = Calendar.getInstance();

			TimeInstantType time = TimeInstantType.Factory.newInstance();
			try {
				// check yyyy
				if (!cal.isSet(Calendar.YEAR)) {
					cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
				}

				// check MM
				if (!cal.isSet(Calendar.MONTH)) {
					cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
				}

				// check dd
				if (!cal.isSet(Calendar.DAY_OF_MONTH)) {
					cal.set(Calendar.DAY_OF_MONTH,
							now.get(Calendar.DAY_OF_MONTH));
				}

				// check HH
				if (!cal.isSet(Calendar.HOUR_OF_DAY)) {
					cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
				}

				// check mm
				if (!cal.isSet(Calendar.MINUTE)) {
					cal.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
				}

				// check ss
				if (!cal.isSet(Calendar.SECOND)) {
					cal.set(Calendar.SECOND, now.get(Calendar.SECOND));
				}

				// check timezone
				if (cal.getTimeZone() == null) {
					cal.setTimeZone(now.getTimeZone());
				}

				StringBuffer buf = new StringBuffer(iso8601Format.format(cal
						.getTime()));
				buf.insert(buf.length() - 2, ':');
				time = TimeInstantType.Factory.parse(
						"<Value xmlns=\"http://ucore.gov/ucore/2.0\">"
								+ buf.toString() + "</Value>", null);
			} catch (Exception e) {
				time.setValue(now);
			}

			XmlUtil.substitute(locatedAt.addNewTime().addNewTimeAbstract(),
					NS_UCORE, S_TimeInstant, TimeInstantType.type, time);
		}

		EntityRefType entityRef = EntityRefType.Factory.newInstance();
		ArrayList<String> theList = new ArrayList<String>();
		theList.add(entityId);
		entityRef.setRef(theList);
		locatedAt.setEntityRef(entityRef);

		LocationRefType locationRef = LocationRefType.Factory.newInstance();
		theList.clear();
		theList.add(locationId);
		locationRef.setRef(theList);
		locatedAt.setLocationRef(locationRef);

		XmlUtil.substitute(digest.getDigest().addNewRelationshipAbstract(),
				NS_UCORE, S_LocatedAt,
				EntityLocationExtendedRelationshipType.type, locatedAt);
	}

	// The OccursAt Relationship is used to associate an Event (like the forest
	// fire) with a time
	// and place.
	public void setOccursAt(String eventId, String locationId, Calendar cal) {

		EventLocationRelationshipType occursAt = EventLocationRelationshipType.Factory
				.newInstance();
		occursAt.setId(UUIDUtil.getID(S_OccursAt));

		if (cal != null) {
			// TRAC #272
			// UCore's TimeInstant is a union of several date, time, and
			// date-time types
			// The value is converted into a Java Calendar object which, when
			// passed to xmlbeans
			// uses the first match in the union (generally date or date+offset)
			// To overcome this,
			// the following code constructs a valid iso 8601 (xs:datTime)
			// string and then
			// invokes TimeInstantType's parse method to construct the Value
			// element.
			//
			// The following code normalizes the date and time such that a
			// complete
			// datetime object is constructed including a locale offset
			// (timezone).
			// This ensures meaningful sorting and searching within UICDS.

			// normalized iso 8601 datetime string (with offset)
			SimpleDateFormat iso8601Format = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssZ");

			// use the current time and offset to fill in any missing blanks.
			Calendar now = Calendar.getInstance();

			TimeInstantType time = TimeInstantType.Factory.newInstance();
			try {
				// check yyyy
				if (!cal.isSet(Calendar.YEAR)) {
					cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
				}

				// check MM
				if (!cal.isSet(Calendar.MONTH)) {
					cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
				}

				// check dd
				if (!cal.isSet(Calendar.DAY_OF_MONTH)) {
					cal.set(Calendar.DAY_OF_MONTH,
							now.get(Calendar.DAY_OF_MONTH));
				}

				// check HH
				if (!cal.isSet(Calendar.HOUR_OF_DAY)) {
					cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
				}

				// check mm
				if (!cal.isSet(Calendar.MINUTE)) {
					cal.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
				}

				// check ss
				if (!cal.isSet(Calendar.SECOND)) {
					cal.set(Calendar.SECOND, now.get(Calendar.SECOND));
				}

				// check timezone
				if (cal.getTimeZone() == null) {
					cal.setTimeZone(now.getTimeZone());
				}

				StringBuffer buf = new StringBuffer(iso8601Format.format(cal
						.getTime()));
				buf.insert(buf.length() - 2, ':');
				time = TimeInstantType.Factory.parse(
						"<Value xmlns=\"http://ucore.gov/ucore/2.0\">"
								+ buf.toString() + "</Value>", null);
			} catch (Exception e) {
				time.setValue(now);
			}

			XmlUtil.substitute(occursAt.addNewTime().addNewTimeAbstract(),
					NS_UCORE, S_TimeInstant, TimeInstantType.type, time);
		}

		// set the event reference
		EventRefType eventRef = EventRefType.Factory.newInstance();
		ArrayList<String> theList = new ArrayList<String>();
		theList.add(eventId);
		eventRef.setRef(theList);
		occursAt.setEventRef(eventRef);

		// set the location reference
		LocationRefType locationRef = LocationRefType.Factory.newInstance();
		theList.clear();
		theList.add(locationId);
		locationRef.setRef(theList);
		occursAt.setLocationRef(locationRef);

		XmlUtil.substitute(digest.getDigest().addNewRelationshipAbstract(),
				NS_UCORE, S_OccursAt, EventLocationRelationshipType.type,
				occursAt);
	}

	// The HasDestinationOf Relationship is used to associate an Event with a
	// destination
	// (i.e. RequestResource event with where the resource is requested to go
	// to)
	public void setHasDestinationOf(String eventId, String locationId,
			Calendar cal) {

		EntityLocationRelationshipType hasDestinationOf = EntityLocationRelationshipType.Factory
				.newInstance();
		hasDestinationOf.setId(UUIDUtil.getID(S_HasDestionationOf));

		// add a time instant
		TimeInstantType time = TimeInstantType.Factory.newInstance();
		time.setValue(cal);

		XmlUtil.substitute(hasDestinationOf.addNewTime().addNewTimeAbstract(),
				NS_UCORE, S_TimeInstant, TimeInstantType.type, time);

		// set the event reference
		EntityRefType eventRef = EntityRefType.Factory.newInstance();
		ArrayList<String> theList = new ArrayList<String>();
		theList.add(eventId);
		eventRef.setRef(theList);
		hasDestinationOf.setEntityRef(eventRef);

		// set the location reference
		LocationRefType locationRef = LocationRefType.Factory.newInstance();
		theList.clear();
		theList.add(locationId);
		locationRef.setRef(theList);
		hasDestinationOf.setLocationRef(locationRef);

		// System.out.println(hasDestinationOf);
		XmlUtil.substitute(digest.getDigest().addNewRelationshipAbstract(),
				NS_UCORE, S_HasDestionationOf,
				EntityLocationRelationshipType.type, hasDestinationOf);
	}

	// The InvolvedIn relationship is used to associate an Agent with an Event
	public void setInvolvedIn(String agentId, String eventId, Calendar cal) {

		AgentEventRelationshipType involvedIn = AgentEventRelationshipType.Factory
				.newInstance();
		involvedIn.setId(UUIDUtil.getID(S_InvolvedIn));

		// add a time instant
		TimeInstantType time = TimeInstantType.Factory.newInstance();
		time.setValue(cal);
		XmlUtil.substitute(involvedIn.addNewTime().addNewTimeAbstract(),
				NS_UCORE, S_TimeInstant, TimeInstantType.type, time);

		// set the event reference
		EventRefType eventRef = EventRefType.Factory.newInstance();
		ArrayList<String> theList = new ArrayList<String>();
		theList.add(eventId);
		eventRef.setRef(theList);
		involvedIn.setEventRef(eventRef);

		// set the location reference
		AgentRefType locationRef = AgentRefType.Factory.newInstance();
		theList.clear();
		theList.add(agentId);
		locationRef.setRef(theList);
		involvedIn.setAgentRef(locationRef);

		// System.out.println(hasDestinationOf);
		XmlUtil.substitute(digest.getDigest().addNewRelationshipAbstract(),
				NS_UCORE, S_InvolvedIn, AgentEventRelationshipType.type,
				involvedIn);
	}

	public void setCauseOf(String causeId, String effectId) {

		CauseOfRelationshipType causeOf = CauseOfRelationshipType.Factory
				.newInstance();
		causeOf.setId(UUIDUtil.getID("CauseOf"));

		// Set the cause
		ThingRefType thingRef = ThingRefType.Factory.newInstance();
		ArrayList<String> theList = new ArrayList<String>();
		theList.add(causeId);
		thingRef.setRef(theList);
		causeOf.setCause(thingRef);

		// Set the effect
		EventRefType effectRef = EventRefType.Factory.newInstance();
		theList.clear();
		theList.add(effectId);
		effectRef.setRef(theList);
		causeOf.setEffect(effectRef);

		XmlUtil.substitute(digest.getDigest().addNewRelationshipAbstract(),
				NS_UCORE, S_CauseOf, CauseOfRelationshipType.type, causeOf);
	}

	public void setPoint(LocationType location,
			net.opengis.gml.x32.PointType point) {

		addPointToLocation(location, point);
		setLocation(location);
	}

	public void addPointToLocation(LocationType location,
			net.opengis.gml.x32.PointType point) {

		PointType uPoint = PointType.Factory.newInstance();
		uPoint.addNewPoint().set(point);
		XmlUtil.substitute(location.addNewGeoLocation()
				.addNewGeoLocationAbstract(), NS_UCORE, S_Point,
				PointType.type, uPoint);
	}

	public void setCircle(LocationType location,
			net.opengis.gml.x32.CircleByCenterPointType circle) {

		addCircleToLocation(location, circle);
		setLocation(location);
	}

	protected void addCircleToLocation(LocationType location,
			net.opengis.gml.x32.CircleByCenterPointType circle) {

		circle.getPos().setSrsName(GeoUtil.EPSG4326);
		CircleByCenterPointType uCircle = CircleByCenterPointType.Factory
				.newInstance();
		uCircle.addNewCircleByCenterPoint().set(circle);
		XmlUtil.substitute(location.addNewGeoLocation()
				.addNewGeoLocationAbstract(), NS_UCORE, S_CircleByCenterPoint,
				CircleByCenterPointType.type, uCircle);
	}

	public void setLocation(LocationType location) {

		XmlUtil.substitute(digest.getDigest().addNewThingAbstract(), NS_UCORE,
				S_Location, LocationType.type, location);
	}

	public void setPolygon(LocationType location,
			net.opengis.gml.x32.PolygonType polygon) {

		addPolygonToLocation(location, polygon);
		setLocation(location);
	}

	public void addPolygonToLocation(LocationType location,
			net.opengis.gml.x32.PolygonType polygon) {

		polygon.setSrsName(GeoUtil.EPSG4326);
		if (polygon.getExterior().getAbstractRing() instanceof LinearRingType) {
			LinearRingType ring = (LinearRingType) polygon.getExterior()
					.getAbstractRing();
			for (DirectPositionType pos : ring.getPosArray()) {
				pos.setSrsName(GeoUtil.EPSG4326);
			}
		}
		PolygonType uPolygon = PolygonType.Factory.newInstance();
		uPolygon.addNewPolygon().set(polygon);
		XmlUtil.substitute(location.addNewGeoLocation()
				.addNewGeoLocationAbstract(), NS_UCORE, S_Polygon,
				PolygonType.type, uPolygon);
	}

	public void setLineString(LocationType location,
			net.opengis.gml.x32.LineStringType line) {

		addLineStringToLocation(location, line);
		setLocation(location);
	}

	public void addLineStringToLocation(LocationType location,
			net.opengis.gml.x32.LineStringType line) {
		line.setSrsName(GeoUtil.EPSG4326);
		LineStringType uLine = LineStringType.Factory.newInstance();
		uLine.addNewLineString().set(line);
		XmlUtil.substitute(location.addNewGeoLocation()
				.addNewGeoLocationAbstract(), NS_UCORE, "LineString",
				LineStringType.type, uLine);
	}

	public static Geometry getFirstGeometry(DigestType digest) {

		for (ThingType thing : digest.getThingAbstractArray()) {
			if (thing instanceof LocationType) {
				LocationType location = (LocationType) thing;
				for (GeoLocationType geo : location.getGeoLocationArray()) {
					Geometry geometry = getGeometry(geo);
					if (geometry != null) {
						return geometry;
					}
				}
			}
		}
		return null;
	}

	public static Geometry getGeometry(GeoLocationType geo) {

		Geometry result = null;
		XmlObject abstr = geo.getGeoLocationAbstract();
		if (abstr instanceof PolygonType) {
			Node node = ((PolygonType) abstr).getPolygon().getDomNode();
			result = getGeometry(node);
		} else if (abstr instanceof PointType) {
			Node node = ((PointType) abstr).getPoint().getDomNode();
			result = getGeometry(node);
		} else if (abstr instanceof CircleByCenterPointType) {
			PointType point = PointType.Factory.newInstance();
			point.addNewPoint().setPos(
					((CircleByCenterPointType) abstr).getCircleByCenterPoint()
							.getPos());
			Node node = point.getPoint().getDomNode();
			result = getGeometry(node);
		}
		return result;
	}

	public static Geometry getGeometry(Node node) {

		MarshalContext ctx = new MarshalContext(gmlParseCfg);
		Geometry result = (Geometry) ctx.marshal(node);
		return result;
	}

	public static Geometry getGeometryFromLocationByID(DigestType digest,
			String id) {

		XmlObject[] elements = digest.selectChildren(NS_UCORE, "Location");
		if (elements != null && elements.length > 0) {
			for (XmlObject element : elements) {
				if (element instanceof LocationType) {
					LocationType location = (LocationType) element;
					if (location.getId().equals(id)) {
						for (GeoLocationType geo : location
								.getGeoLocationArray()) {
							Geometry geometry = getGeometry(geo);
							if (geometry != null) {
								return geometry;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public String toString() {

		return digest.isNil() ? null : digest.toString();
	}

	public static boolean objectHasWhatType(String codespace, String code,
			String label, String value, XmlObject event) {

		boolean codespaceOk = false;
		boolean codeOk = false;
		boolean labelOk = false;
		boolean valueOk = false;
		boolean ok = false;
		XmlObject[] props = event.selectChildren(WhatType.type.getName()
				.getNamespaceURI(), "What");
		for (XmlObject prop : props) {
			// System.out.println(prop);
			// Find the SimpleProperty with the correct codespace (there may be
			// more than one)
			XmlObject codespaceAttr = prop.selectAttribute(WhatType.type
					.getName().getNamespaceURI(), "codespace");
			if (codespaceAttr != null) {
				// System.out.println(getTextFromAny(codespaceAttr) + " " +
				// codespace);
				if (XmlUtil.getTextFromAny(codespaceAttr).equals(codespace)) {
					codespaceOk = true;
					// Check if it has the correct code value
					if (code != null) {
						XmlObject codeAttr = prop.selectAttribute(WhatType.type
								.getName().getNamespaceURI(), "code");
						if (codeAttr != null
								&& XmlUtil.getTextFromAny(codeAttr)
										.equals(code)) {
							codeOk = true;
						}
					} else {
						codeOk = true;
					}
					// Check if it has the correct label
					if (label != null) {
						XmlObject labelAttr = prop.selectAttribute(
								WhatType.type.getName().getNamespaceURI(),
								"label");
						if (labelAttr != null
								&& XmlUtil.getTextFromAny(labelAttr).equals(
										label)) {
							labelOk = true;
						}
					} else {
						labelOk = true;
					}

					// Check if it has the correct value
					if (value != null) {
						if (XmlUtil.getTextFromAny(prop).equals(value)) {
							valueOk = true;
						}
					} else {
						valueOk = true;
					}
				}
			}
			ok = codespaceOk && codeOk && labelOk && valueOk;
			if (ok) {
				break;
			} else {
				// System.out.println(codespaceOk);
				// System.out.println(codeOk);
				// System.out.println(labelOk);
				// System.out.println(valueOk);
				ok = codespaceOk = codeOk = labelOk = valueOk = false;
			}
		}
		return ok;
	}

	public static List<LocationType> getLocationElements(DigestType digest) {

		ArrayList<LocationType> list = new ArrayList<LocationType>();
		XmlObject[] locations = digest.selectChildren(
				gov.ucore.ucore.x20.LocationType.type.getName()
						.getNamespaceURI(), "Location");
		if (locations.length > 0) {
			for (XmlObject object : locations) {
				LocationType location = (LocationType) object;
				list.add(location);
			}
		}
		return list;
	}

	public static String getUCoreWhatType(DigestType digest) {

		return "Event";
	}

	public static CauseOfRelationshipType getCauseByEffectID(DigestType digest,
			String effectID) {

		RelationshipType[] relationships = digest
				.getRelationshipAbstractArray();
		for (RelationshipType relationship : relationships) {
			if (relationship instanceof CauseOfRelationshipType) {
				CauseOfRelationshipType causeOf = (CauseOfRelationshipType) relationship;
				if (causeOf.getEffect().getRef().size() > 0) {
					String effectValue = (String) causeOf.getEffect().getRef()
							.get(0);
					if (effectValue.equals(effectID)) {
						return causeOf;
					}
				}
			}
		}
		return null;
	}

	public static EventLocationRelationshipType getLocationRelationshipByTypeAndEventID(
			DigestType digest, String type, ThingRefType eventID) {

		XmlObject[] relationships = digest.selectChildren(
				EventLocationRelationshipType.type.getName().getNamespaceURI(),
				type);
		for (XmlObject relationship : relationships) {
			EventLocationRelationshipType elRelationship = (EventLocationRelationshipType) relationship;
			if (elRelationship.getEventRef().getRef().get(0)
					.equals(eventID.getRef().get(0))) {
				return elRelationship;
			}
		}
		return null;
	}
}
