/**
 *
 */
package com.microtripit.mandrillapp.lutung.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author rschreijer
 * @since Mar 16, 2013
 */
public final class LutungGsonUtils {
	private static final String dateFormatStr = "yyyy-MM-dd HH:mm:ss";

	private static Gson gson = createGson();

	public static Gson getGson() {
		return gson;
	}

	public static Gson createGson() {
		return createGsonBuilder().create();
	}

	public static GsonBuilder createGsonBuilder() {
		return new GsonBuilder()
				.setDateFormat(dateFormatStr)
				.registerTypeAdapter(Date.class, new DateDeserializer())
				.registerTypeAdapter(Map.class, new MapSerializer())
				.registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeSerializer())
				.registerTypeAdapter(MandrillMessage.Recipient.Type.class,
						new RecipientTypeSerializer());
	}

	// https://howtodoinjava.com/gson/gson-typeadapter-for-inaccessibleobjectexception/
	public static final class ZonedDateTimeSerializer implements JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {

		private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d::MMM::uuuu HH::mm::ss z");

		@Override
		public JsonElement serialize(ZonedDateTime zonedDateTime, Type srcType,
									 JsonSerializationContext context) {

			return new JsonPrimitive(formatter.format(zonedDateTime));
		}

		@Override
		public ZonedDateTime deserialize(JsonElement json, Type typeOfT,
										 JsonDeserializationContext context) throws JsonParseException {

			return ZonedDateTime.parse(json.getAsString(), formatter);
		}
	}
	public static final class DateDeserializer
			implements JsonDeserializer<Date>, JsonSerializer<Date> {

		private SimpleDateFormat getNewDefaultDateTimeFormat() {
			final SimpleDateFormat formatter = new SimpleDateFormat(dateFormatStr);
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			return formatter;
		}

		public Date deserialize(final JsonElement json,
								final Type typeOfT,
								final JsonDeserializationContext context)
				throws JsonParseException {

			if(!json.isJsonPrimitive()) {
				throw new JsonParseException("Unexpected type for date: " + json);
			}
			try {
				return getNewDefaultDateTimeFormat().parse(json.getAsString());

			} catch(final ParseException e) {
				throw new JsonParseException("Failed to parse date '" + json.getAsString() + "'", e);
			}
		}

		public JsonElement serialize(
				final Date src,
				final Type typeOfSrc,
				final JsonSerializationContext context) {

			return new JsonPrimitive(getNewDefaultDateTimeFormat().format(src));
		}
	}

	public static class MapSerializer implements JsonSerializer<Map<? extends Object,? extends Object>> {

		public final JsonElement serialize(
				final Map<?, ?> src,
				final Type typeOfSrc,
				final JsonSerializationContext context) {

			Object value;
			final JsonObject json = new JsonObject();
			for(Object key : src.keySet()) {
				value = src.get(key);
				json.add( key.toString(), context.serialize(
						value, value.getClass()) );
			}
			return json;

		}

	}

	public static final class RecipientTypeSerializer
			implements JsonDeserializer<MandrillMessage.Recipient.Type>,
			JsonSerializer<MandrillMessage.Recipient.Type> {

		public MandrillMessage.Recipient.Type deserialize(
				final JsonElement json,
				final Type typeOfT,
				final JsonDeserializationContext context)
				throws JsonParseException {

			if(!json.isJsonPrimitive()) {
				throw new JsonParseException(
						"Unexpected type for recipient type: " + json);
			}

			return MandrillMessage.Recipient.Type.valueOf(
					json.getAsString().toUpperCase());

		}

		public JsonPrimitive serialize(
				final MandrillMessage.Recipient.Type src,
				final Type typeOfSrc,
				final JsonSerializationContext context) {

			return new JsonPrimitive(src.name().toLowerCase());
		}
	}
}
