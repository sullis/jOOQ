/**
 * Copyright (c) 2009-2012, Lukas Eder, lukas.eder@gmail.com
 * All rights reserved.
 *
 * This software is licensed to you under the Apache License, Version 2.0
 * (the "License"); You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * . Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * . Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * . Neither the name "jOOQ" nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.jooq.impl;

import static org.jooq.impl.SQLDataType.BLOB;
import static org.jooq.impl.SQLDataType.CLOB;
import static org.jooq.impl.SQLDataType.NCLOB;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jooq.ArrayRecord;
import org.jooq.Configuration;
import org.jooq.Converter;
import org.jooq.DataType;
import org.jooq.EnumType;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.UDTRecord;
import org.jooq.exception.SQLDialectNotSupportedException;
import org.jooq.tools.Convert;
import org.jooq.types.Interval;

/**
 * A common base class for data types.
 * <p>
 * This also acts as a static data type registry for jOOQ internally.
 * <p>
 * This type is for JOOQ INTERNAL USE only. Do not reference directly
 *
 * @author Lukas Eder
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class DefaultDataType<T> implements DataType<T> {

    /**
     * Generated UID
     */
    private static final long                            serialVersionUID = 4155588654449505119L;

    /**
     * A pattern for data type name normalisation
     */
    private static final Pattern                         NORMALISE_PATTERN = Pattern.compile("\"|\\.|\\s|\\(\\w+(,\\w+)*\\)|(NOT\\s*NULL)?");

    // -------------------------------------------------------------------------
    // Data type caches
    // -------------------------------------------------------------------------

    /**
     * A cache for dialect-specific data types by normalised
     */
    private static final Map<String, DataType<?>>[]      TYPES_BY_NAME;

    /**
     * A cache for dialect-specific data types by Java type
     */
    private static final Map<Class<?>, DataType<?>>[]    TYPES_BY_TYPE;

    /**
     * A cache for dialect-specific data types by SQL DataTypes
     */
    private static final Map<DataType<?>, DataType<?>>[] TYPES_BY_SQL_DATATYPE;

    /**
     * A cache for SQL DataTypes by Java type
     */
    private static final Map<Class<?>, DataType<?>>      SQL_DATATYPES_BY_TYPE;

    // -------------------------------------------------------------------------
    // Precisions
    // -------------------------------------------------------------------------

    /**
     * The minimum decimal precision needed to represent a Java {@link Long} type
     */
    private static final int                             LONG_PRECISION    = String.valueOf(Long.MAX_VALUE).length();

    /**
     * The minimum decimal precision needed to represent a Java {@link Integer} type
     */
    private static final int                             INTEGER_PRECISION = String.valueOf(Integer.MAX_VALUE).length();

    /**
     * The minimum decimal precision needed to represent a Java {@link Short} type
     */
    private static final int                             SHORT_PRECISION   = String.valueOf(Short.MAX_VALUE).length();

    /**
     * The minimum decimal precision needed to represent a Java {@link Byte} type
     */
    private static final int                             BYTE_PRECISION    = String.valueOf(Byte.MAX_VALUE).length();

    // -------------------------------------------------------------------------
    // Data type attributes
    // -------------------------------------------------------------------------

    /**
     * The SQL dialect associated with this data type
     */
    private final SQLDialect                             dialect;

    /**
     * The SQL DataType corresponding to this data type
     */
    private final SQLDataType<T>                         sqlDataType;

    /**
     * The Java class corresponding to this data type
     */
    private final Class<T>                               type;

    /**
     * The Java class corresponding to arrays of this data type
     */
    private final Class<T[]>                             arrayType;

    /**
     * The type name used for casting to this type
     */
    private final String                                 castTypeName;

    /**
     * The type name
     */
    private final String                                 typeName;

    static {
        TYPES_BY_SQL_DATATYPE = new Map[SQLDialect.values().length];
        TYPES_BY_NAME = new Map[SQLDialect.values().length];
        TYPES_BY_TYPE = new Map[SQLDialect.values().length];

        for (SQLDialect dialect : SQLDialect.values()) {
            TYPES_BY_SQL_DATATYPE[dialect.ordinal()] = new LinkedHashMap<DataType<?>, DataType<?>>();
            TYPES_BY_NAME[dialect.ordinal()] = new LinkedHashMap<String, DataType<?>>();
            TYPES_BY_TYPE[dialect.ordinal()] = new LinkedHashMap<Class<?>, DataType<?>>();
        }

        SQL_DATATYPES_BY_TYPE = new LinkedHashMap<Class<?>, DataType<?>>();
    }

    public DefaultDataType(SQLDialect dialect, SQLDataType<T> sqlDataType, String typeName) {
        this(dialect, sqlDataType, sqlDataType.getType(), typeName, typeName);
    }

    public DefaultDataType(SQLDialect dialect, SQLDataType<T> sqlDataType, String typeName, String castTypeName) {
        this(dialect, sqlDataType, sqlDataType.getType(), typeName, castTypeName);
    }

    public DefaultDataType(SQLDialect dialect, SQLDataType<T> sqlDataType, Class<T> type, String typeName) {
        this(dialect, sqlDataType, type, typeName, typeName);
    }

    public DefaultDataType(SQLDialect dialect, SQLDataType<T> sqlDataType, Class<T> type, String typeName, String castTypeName) {
        this.dialect = dialect;

        // [#858] SQLDataTypes should reference themselves for more convenience
        this.sqlDataType = (SQLDataType<T>) ((this instanceof SQLDataType) ? this : sqlDataType);
        this.type = type;
        this.typeName = typeName;
        this.castTypeName = castTypeName;
        this.arrayType = (Class<T[]>) Array.newInstance(type, 0).getClass();

        init();
    }

    private final void init() {

        // Dialect-specific data types
        int ordinal = dialect == null ? SQLDialect.SQL99.ordinal() : dialect.ordinal();
        String normalised = DefaultDataType.normalise(typeName);

        if (TYPES_BY_NAME[ordinal].get(normalised) == null) {
            TYPES_BY_NAME[ordinal].put(normalised, this);
        }

        if (TYPES_BY_TYPE[ordinal].get(type) == null) {
            TYPES_BY_TYPE[ordinal].put(type, this);
        }

        if (TYPES_BY_SQL_DATATYPE[ordinal].get(sqlDataType) == null) {
            TYPES_BY_SQL_DATATYPE[ordinal].put(sqlDataType, this);
        }

        // Global data types
        if (dialect == null) {
            if (SQL_DATATYPES_BY_TYPE.get(type) == null) {
                SQL_DATATYPES_BY_TYPE.put(type, this);
            }
        }
    }

    @Override
    public final SQLDataType<T> getSQLDataType() {
        return sqlDataType;
    }

    @Override
    public final DataType<T> getDataType(Configuration configuration) {

        // If this is a SQLDataType find the most suited dialect-specific
        // data type
        if (getDialect() == null) {
            DataType<?> dataType = TYPES_BY_SQL_DATATYPE[configuration.getDialect().ordinal()].get(this);

            if (dataType != null) {
                return (DataType<T>) dataType;
            }
        }

        // If this is already the dialect's specific data type, return this
        else if (getDialect() == configuration.getDialect()) {
            return this;
        }

        // If the SQL data type is not available stick with this data type
        else if (getSQLDataType() == null) {
            return this;
        }

        // If this is another dialect's specific data type, recurse
        else {
            getSQLDataType().getDataType(configuration);
        }

        return this;
    }

    @Override
    public /* final */ int getSQLType() {
        // TODO [#1227] There is some confusion with these types, especially
        // when it comes to byte[] which can be mapped to BLOB, BINARY, VARBINARY

        if (type == Blob.class) {
            return Types.BLOB;
        }
        else if (type == Boolean.class) {
            return Types.BOOLEAN;
        }
        else if (type == BigInteger.class) {
            return Types.BIGINT;
        }
        else if (type == BigDecimal.class) {
            return Types.DECIMAL;
        }
        else if (type == Byte.class) {
            return Types.TINYINT;
        }
        else if (type == byte[].class) {
            return Types.BLOB;
        }
        else if (type == Clob.class) {
            return Types.CLOB;
        }
        else if (type == Date.class) {
            return Types.DATE;
        }
        else if (type == Double.class) {
            return Types.DOUBLE;
        }
        else if (type == Float.class) {
            return Types.FLOAT;
        }
        else if (type == Integer.class) {
            return Types.INTEGER;
        }
        else if (type == Long.class) {
            return Types.BIGINT;
        }
        else if (type == Short.class) {
            return Types.SMALLINT;
        }
        else if (type == String.class) {
            return Types.VARCHAR;
        }
        else if (type == Time.class) {
            return Types.TIME;
        }
        else if (type == Timestamp.class) {
            return Types.TIMESTAMP;
        }

        // The type byte[] is handled earlier.
        else if (type.isArray()) {
            return Types.ARRAY;
        }
        else if (ArrayRecord.class.isAssignableFrom(type)) {
            return Types.ARRAY;
        }
        else if (EnumType.class.isAssignableFrom(type)) {
            return Types.VARCHAR;
        }
        else if (UDTRecord.class.isAssignableFrom(type)) {
            return Types.STRUCT;
        }
        else if (Result.class.isAssignableFrom(type)) {
            switch (dialect) {
                case ORACLE:
                case H2:
                    return -10; // OracleTypes.CURSOR;

                case POSTGRES:
                default:
                    return Types.OTHER;
            }
        }
        else {
            return Types.OTHER;
        }
    }

    @Override
    public final Class<T> getType() {
        return type;
    }

    @Override
    public final Class<T[]> getArrayType() {
        return arrayType;
    }

    @Override
    public final String getTypeName() {
        return typeName;
    }

    @Override
    public String getTypeName(Configuration configuration) {
        return getDataType(configuration).getTypeName();
    }

    @Override
    public final String getCastTypeName() {
        return castTypeName;
    }

    @Override
    public /* final */ String getCastTypeName(Configuration configuration, int length) {
        String result = getCastTypeName(configuration);

        if (length != 0) {

            // Remove existing length information, first
            result = result.replaceAll("\\([^\\)]*\\)", "");
            result += "(" + length + ")";
        }

        return result;
    }

    @Override
    public /* final */ String getCastTypeName(Configuration configuration, int precision, int scale) {
        String result = getCastTypeName(configuration);

        if (precision != 0) {

            // Remove existing precision / scale information, first
            result = result.replaceAll("\\([^\\)]*\\)", "");

            if (scale != 0) {
                result += "(" + precision + ", " + scale + ")";
            }
            else {
                result += "(" + precision + ")";
            }
        }

        return result;
    }

    @Override
    public String getCastTypeName(Configuration configuration) {
        return getDataType(configuration).getCastTypeName();
    }

    @Override
    public final DataType<T[]> getArrayDataType() {
        return new ArrayDataType<T>(this);
    }

    @Override
    public final <A extends ArrayRecord<T>> DataType<A> asArrayDataType(Class<A> arrayDataType) {
        return new DefaultDataType<A>(dialect, null, arrayDataType, typeName, castTypeName);
    }

    @Override
    public final <E extends EnumType> DataType<E> asEnumDataType(Class<E> enumDataType) {
        return new DefaultDataType<E>(dialect, null, enumDataType, typeName, castTypeName);
    }

    @Override
    public final <U> DataType<U> asConvertedDataType(Converter<? super T, U> converter) {
        return new ConvertedDataType<T, U>(this, converter);
    }

    @Override
    public final SQLDialect getDialect() {
        return dialect;
    }

    @Override
    public /* final */ T convert(Object object) {

        // [#1441] Avoid unneeded type conversions to improve performance
        if (object == null) {
            return null;
        }
        else if (object.getClass() == type) {
            return (T) object;
        }
        else {
            return Convert.convert(object, type);
        }
    }

    @Override
    public final T[] convert(Object... objects) {
        return (T[]) Convert.convertArray(objects, type);
    }

    @Override
    public final List<T> convert(Collection<?> objects) {
        return Convert.convert(objects, type);
    }

    public static DataType<Object> getDefaultDataType(String typeName) {
        return new DefaultDataType<Object>(SQLDialect.SQL99, null, Object.class, typeName, typeName);
    }

    public static DataType<Object> getDefaultDataType(SQLDialect dialect, String typeName) {
        return new DefaultDataType<Object>(dialect, null, Object.class, typeName, typeName);
    }

    public static DataType<?> getDataType(SQLDialect dialect, String typeName) {
        String normalised = DefaultDataType.normalise(typeName);
        DataType<?> result = TYPES_BY_NAME[dialect.ordinal()].get(normalised);

        // UDT data types and others are registered using SQL99
        if (result == null) {
            result = TYPES_BY_NAME[SQLDialect.SQL99.ordinal()].get(normalised);
        }

        if (result == null) {
            // [#366] Don't log a warning here. The warning is logged when
            // catching the exception in jOOQ-codegen
            throw new SQLDialectNotSupportedException("Type " + typeName + " is not supported in dialect " + dialect, false);
        }

        return result;
    }

    public static <T> DataType<T> getDataType(SQLDialect dialect, Class<T> type) {

        // Recurse for arrays
        if (byte[].class != type && type.isArray()) {
            return (DataType<T>) getDataType(dialect, type.getComponentType()).getArrayDataType();
        }

        // Base types are registered statically
        else {
            DataType<?> result = null;

            if (dialect != null) {
                result = TYPES_BY_TYPE[dialect.ordinal()].get(type);
            }

            if (result == null) {

                // jOOQ data types are handled here
                if (EnumType.class.isAssignableFrom(type) ||
                    UDTRecord.class.isAssignableFrom(type) ||
                    ArrayRecord.class.isAssignableFrom(type)) {

                    for (SQLDialect d : SQLDialect.values()) {
                        result = TYPES_BY_TYPE[d.ordinal()].get(type);

                        if (result != null) {
                            break;
                        }
                    }
                }
            }

            if (result == null) {
                if (SQL_DATATYPES_BY_TYPE.get(type) != null) {
                    return (DataType<T>) SQL_DATATYPES_BY_TYPE.get(type);
                }

                // All other data types are illegal
                else {
                    throw new SQLDialectNotSupportedException("Type " + type + " is not supported in dialect " + dialect);
                }
            }

            return (DataType<T>) result;
        }
    }

    @Override
    public final boolean isNumeric() {
        return Number.class.isAssignableFrom(type) && !isInterval();
    }

    @Override
    public final boolean isString() {
        return type == String.class;
    }

    @Override
    public final boolean isDateTime() {
        return java.util.Date.class.isAssignableFrom(type);
    }

    @Override
    public final boolean isTemporal() {
        return isDateTime() || isInterval();
    }

    @Override
    public final boolean isInterval() {
        return Interval.class.isAssignableFrom(type);
    }

    @Override
    public final boolean isLob() {
        SQLDataType<T> t = getSQLDataType();
        return (t == BLOB || t == CLOB || t == NCLOB);
    }

    @Override
    public final boolean isBinary() {
        return type == byte[].class;
    }

    @Override
    public final boolean isArray() {
        return ArrayRecord.class.isAssignableFrom(type)
            || (!isBinary() && type.isArray());
    }

    // ------------------------------------------------------------------------
    // The Object API
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + type + ", " + typeName + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dialect == null) ? 0 : dialect.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((typeName == null) ? 0 : typeName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultDataType<?> other = (DefaultDataType<?>) obj;
        if (dialect != other.dialect)
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        }
        else if (!type.equals(other.type))
            return false;
        if (typeName == null) {
            if (other.typeName != null)
                return false;
        }
        else if (!typeName.equals(other.typeName))
            return false;
        return true;
    }

    /**
     * @return The type name without all special characters and white spaces
     */
    public static String normalise(String typeName) {
        return NORMALISE_PATTERN.matcher(typeName.toUpperCase()).replaceAll("");
    }

    /**
     * Convert a type name (using precision and scale) into a Java class
     */
    public static DataType<?> getDataType(SQLDialect dialect, String t, int p, int s) throws SQLDialectNotSupportedException {
        DataType<?> result = DefaultDataType.getDataType(dialect, DefaultDataType.normalise(t));

        if (result.getType() == BigDecimal.class) {
            result = DefaultDataType.getDataType(dialect, getNumericClass(p, s));
        }

        return result;
    }

    /**
     * Convert a type name (using precision and scale) into a Java class
     */
    public static Class<?> getType(SQLDialect dialect, String t, int p, int s) throws SQLDialectNotSupportedException {
        return getDataType(dialect, t, p, s).getType();
    }

    /**
     * Get the most suitable Java class for a given precision and scale
     */
    private static Class<?> getNumericClass(int precision, int scale) {

        // Integer numbers
        if (scale == 0 && precision != 0) {
            if (precision < BYTE_PRECISION) {
                return Byte.class;
            }
            if (precision < SHORT_PRECISION) {
                return Short.class;
            }
            if (precision < INTEGER_PRECISION) {
                return Integer.class;
            }
            if (precision < LONG_PRECISION) {
                return Long.class;
            }

            // Default integer number
            return BigInteger.class;
        }

        // Real numbers should not be represented as float or double
        else {
            return BigDecimal.class;
        }
    }
}
