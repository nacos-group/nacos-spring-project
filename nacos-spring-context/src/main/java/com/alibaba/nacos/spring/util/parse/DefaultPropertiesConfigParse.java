/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.spring.util.parse;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.spring.util.AbstractConfigParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.3.0
 */
public class DefaultPropertiesConfigParse extends AbstractConfigParse {

	private static final Logger logger = LoggerFactory
			.getLogger(DefaultPropertiesConfigParse.class);

	@Override
	public Map<String, Object> parse(String configText) {
		OriginTrackedPropertiesLoader loader = new OriginTrackedPropertiesLoader(
				new ByteArrayResource(configText.getBytes(Charset.defaultCharset())));
		try {
			if (StringUtils.hasText(configText)) {
				return loader.load();
			}
			return new LinkedHashMap<String, Object>();
		}
		catch (IOException e) {
			throw new ConfigParseException(e);
		}
	}

	@Override
	public String processType() {
		return ConfigType.PROPERTIES.getType();
	}

	class OriginTrackedPropertiesLoader {

		private final Resource resource;

		/**
		 * Create a new {@link OriginTrackedPropertiesLoader} instance.
		 *
		 * @param resource the resource of the {@code .properties} data
		 */
		OriginTrackedPropertiesLoader(Resource resource) {
			Assert.notNull(resource, "Resource must not be null");
			this.resource = resource;
		}

		/**
		 * Load {@code .properties} data and return a map of {@code String} ->
		 * {@link OriginTrackedValue}.
		 *
		 * @return the loaded properties
		 * @throws IOException on read error
		 */
		public Map<String, Object> load() throws IOException {
			return load(true);
		}

		/**
		 * Load {@code .properties} data and return a map of {@code String} ->
		 * {@link OriginTrackedValue}.
		 *
		 * @param expandLists if list {@code name[]=a,b,c} shortcuts should be expanded
		 * @return the loaded properties
		 * @throws IOException on read error
		 */
		public Map<String, Object> load(boolean expandLists) throws IOException {
			OriginTrackedPropertiesLoader.CharacterReader reader = new OriginTrackedPropertiesLoader.CharacterReader(
					this.resource);
			try {
				Map<String, Object> result = new LinkedHashMap<String, Object>();
				StringBuilder buffer = new StringBuilder();
				while (reader.read()) {
					String key = loadKey(buffer, reader).trim();
					if (expandLists && key.endsWith("[]")) {
						key = key.substring(0, key.length() - 2);
						int index = 0;
						do {
							OriginTrackedValue value = loadValue(buffer, reader, true);
							put(result, key + "[" + (index++) + "]", value);
							if (!reader.isEndOfLine()) {
								reader.read();
							}
						}
						while (!reader.isEndOfLine());
					}
					else {
						OriginTrackedValue value = loadValue(buffer, reader, false);
						put(result, key, value);
					}
				}
				return result;
			}
			finally {
				reader.close();
			}
		}

		private void put(Map<String, Object> result, String key,
				OriginTrackedValue value) {
			if (!key.isEmpty()) {
				result.put(key, value.value);
			}
		}

		private String loadKey(StringBuilder buffer,
				OriginTrackedPropertiesLoader.CharacterReader reader) throws IOException {
			buffer.setLength(0);
			boolean previousWhitespace = false;
			while (!reader.isEndOfLine()) {
				if (reader.isPropertyDelimiter()) {
					reader.read();
					return buffer.toString();
				}
				if (!reader.isWhiteSpace() && previousWhitespace) {
					return buffer.toString();
				}
				previousWhitespace = reader.isWhiteSpace();
				buffer.append(reader.getCharacter());
				reader.read();
			}
			return buffer.toString();
		}

		private OriginTrackedValue loadValue(StringBuilder buffer,
				OriginTrackedPropertiesLoader.CharacterReader reader, boolean splitLists)
				throws IOException {
			buffer.setLength(0);
			while (reader.isWhiteSpace() && !reader.isEndOfLine()) {
				reader.read();
			}
			Location location = reader.getLocation();
			while (!reader.isEndOfLine() && !(splitLists && reader.isListDelimiter())) {
				buffer.append(reader.getCharacter());
				reader.read();
			}
			TextResourceOrigin origin = new TextResourceOrigin(this.resource, location);
			return OriginTrackedValue.of(buffer.toString(), origin);
		}

		/**
		 * Reads characters from the source resource, taking care of skipping comments,
		 * handling multi-line values and tracking {@code '\'} escapes.
		 */
		private class CharacterReader implements Closeable {

			private final String[] ESCAPES = { "trnf", "\t\r\n\f" };

			private final LineNumberReader reader;

			private int columnNumber = -1;

			private boolean escaped;

			private int character;

			CharacterReader(Resource resource) throws IOException {
				this.reader = new LineNumberReader(new InputStreamReader(
						resource.getInputStream(), StandardCharsets.ISO_8859_1));
			}

			@Override
			public void close() throws IOException {
				this.reader.close();
			}

			public boolean read() throws IOException {
				return read(false);
			}

			public boolean read(boolean wrappedLine) throws IOException {
				this.escaped = false;
				this.character = this.reader.read();
				this.columnNumber++;
				if (this.columnNumber == 0) {
					skipLeadingWhitespace();
					if (!wrappedLine) {
						skipComment();
					}
				}
				if (this.character == '\\') {
					this.escaped = true;
					readEscaped();
				}
				else if (this.character == '\n') {
					this.columnNumber = -1;
				}
				return !isEndOfFile();
			}

			private void skipLeadingWhitespace() throws IOException {
				while (isWhiteSpace()) {
					this.character = this.reader.read();
					this.columnNumber++;
				}
			}

			private void skipComment() throws IOException {
				if (this.character == '#' || this.character == '!') {
					while (this.character != '\n' && this.character != -1) {
						this.character = this.reader.read();
					}
					this.columnNumber = -1;
					read();
				}
			}

			private void readEscaped() throws IOException {
				this.character = this.reader.read();
				int escapeIndex = ESCAPES[0].indexOf(this.character);
				if (escapeIndex != -1) {
					this.character = ESCAPES[1].charAt(escapeIndex);
				}
				else if (this.character == '\n') {
					this.columnNumber = -1;
					read(true);
				}
				else if (this.character == 'u') {
					readUnicode();
				}
			}

			private void readUnicode() throws IOException {
				this.character = 0;
				for (int i = 0; i < 4; i++) {
					int digit = this.reader.read();
					if (digit >= '0' && digit <= '9') {
						this.character = (this.character << 4) + digit - '0';
					}
					else if (digit >= 'a' && digit <= 'f') {
						this.character = (this.character << 4) + digit - 'a' + 10;
					}
					else if (digit >= 'A' && digit <= 'F') {
						this.character = (this.character << 4) + digit - 'A' + 10;
					}
					else {
						throw new IllegalStateException("Malformed \\uxxxx encoding.");
					}
				}
			}

			public boolean isWhiteSpace() {
				return !this.escaped && (this.character == ' ' || this.character == '\t'
						|| this.character == '\f');
			}

			public boolean isEndOfFile() {
				return this.character == -1;
			}

			public boolean isEndOfLine() {
				return this.character == -1 || (!this.escaped && this.character == '\n');
			}

			public boolean isListDelimiter() {
				return !this.escaped && this.character == ',';
			}

			public boolean isPropertyDelimiter() {
				return !this.escaped && (this.character == '=' || this.character == ':');
			}

			public char getCharacter() {
				return (char) this.character;
			}

			public Location getLocation() {
				return new Location(this.reader.getLineNumber(), this.columnNumber);
			}

		}

	}

	public interface OriginProvider {

		/**
		 * Return the source origin or {@code null} if the origin is not known.
		 *
		 * @return the origin or {@code null}
		 */
		Origin getOrigin();

	}

	public abstract static class Origin {

		/**
		 * Find the {@link Origin} that an object originated from. Checks if the source
		 * object is an {@link OriginProvider} and also searches exception stacks.
		 *
		 * @param source the source object or {@code null}
		 * @return an optional {@link Origin}
		 */
		static Origin from(Object source) {
			if (source instanceof Origin) {
				return (Origin) source;
			}
			Origin origin = null;
			if (source != null && source instanceof OriginProvider) {
				origin = ((OriginProvider) source).getOrigin();
			}
			if (origin == null && source != null && source instanceof Throwable) {
				return from(((Throwable) source).getCause());
			}
			return origin;
		}

	}

	public static class OriginTrackedValue implements OriginProvider {

		private final Object value;

		private final Origin origin;

		private OriginTrackedValue(Object value, Origin origin) {
			this.value = value;
			this.origin = origin;
		}

		/**
		 * Return the tracked value.
		 *
		 * @return the tracked value
		 */
		public Object getValue() {
			return this.value;
		}

		@Override
		public Origin getOrigin() {
			return this.origin;
		}

		@Override
		public String toString() {
			return (this.value != null ? this.value.toString() : null);
		}

		@Override
		public int hashCode() {
			return ObjectUtils.nullSafeHashCode(this.value);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || obj.getClass() != getClass()) {
				return false;
			}
			return ObjectUtils.nullSafeEquals(this.value,
					((OriginTrackedValue) obj).value);
		}

		public static OriginTrackedValue of(Object value) {
			return of(value, null);
		}

		/**
		 * Create an {@link OriginTrackedValue} containing the specified {@code
		 * value} and {@code origin}. If the source value implements {@link CharSequence}
		 * then so will the resulting {@link OriginTrackedValue}.
		 *
		 * @param value the source value
		 * @param origin the origin
		 * @return an {@link OriginTrackedValue} or {@code null} if the source value was
		 * {@code null}.
		 */
		public static OriginTrackedValue of(Object value, Origin origin) {
			if (value == null) {
				return null;
			}
			if (value instanceof CharSequence) {
				return new OriginTrackedCharSequence((CharSequence) value, origin);
			}
			return new OriginTrackedValue(value, origin);
		}

		/**
		 * {@link OriginTrackedValue} for a {@link CharSequence}.
		 */
		private static class OriginTrackedCharSequence extends OriginTrackedValue
				implements CharSequence {

			OriginTrackedCharSequence(CharSequence value, Origin origin) {
				super(value, origin);
			}

			@Override
			public int length() {
				return getValue().length();
			}

			@Override
			public char charAt(int index) {
				return getValue().charAt(index);
			}

			@Override
			public CharSequence subSequence(int start, int end) {
				return getValue().subSequence(start, end);
			}

			@Override
			public CharSequence getValue() {
				return (CharSequence) super.getValue();
			}

		}

	}

	public class TextResourceOrigin extends Origin {

		private final Resource resource;

		private final Location location;

		public TextResourceOrigin(Resource resource, Location location) {
			this.resource = resource;
			this.location = location;
		}

		/**
		 * Return the resource where the property originated.
		 *
		 * @return the text resource or {@code null}
		 */
		public Resource getResource() {
			return this.resource;
		}

		/**
		 * Return the location of the property within the source (if known).
		 *
		 * @return the location or {@code null}
		 */
		public Location getLocation() {
			return this.location;
		}

		@Override
		public int hashCode() {
			int result = 1;
			result = 31 * result + ObjectUtils.nullSafeHashCode(this.resource);
			result = 31 * result + ObjectUtils.nullSafeHashCode(this.location);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (obj instanceof TextResourceOrigin) {
				TextResourceOrigin other = (TextResourceOrigin) obj;
				boolean result = true;
				result = result
						&& ObjectUtils.nullSafeEquals(this.resource, other.resource);
				result = result
						&& ObjectUtils.nullSafeEquals(this.location, other.location);
				return result;
			}
			return super.equals(obj);
		}

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append(this.resource != null ? this.resource.getDescription()
					: "unknown resource [?]");
			if (this.location != null) {
				result.append(":").append(this.location);
			}
			return result.toString();
		}
	}

	/**
	 * A location (line and column number) within the resource.
	 */
	static class Location {

		private final int line;

		private final int column;

		/**
		 * Create a new {@link Location} instance.
		 *
		 * @param line the line number (zero indexed)
		 * @param column the column number (zero indexed)
		 */
		public Location(int line, int column) {
			this.line = line;
			this.column = column;
		}

		/**
		 * Return the line of the text resource where the property originated.
		 *
		 * @return the line number (zero indexed)
		 */
		public int getLine() {
			return this.line;
		}

		/**
		 * Return the column of the text resource where the property originated.
		 *
		 * @return the column number (zero indexed)
		 */
		public int getColumn() {
			return this.column;
		}

		@Override
		public int hashCode() {
			return (31 * this.line) + this.column;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			Location other = (Location) obj;
			boolean result = true;
			result = result && this.line == other.line;
			result = result && this.column == other.column;
			return result;
		}

		@Override
		public String toString() {
			return (this.line + 1) + ":" + (this.column + 1);
		}

	}

}
