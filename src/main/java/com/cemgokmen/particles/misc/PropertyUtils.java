/*
 * Particles, a self-organizing particle system simulator.
 * Copyright (C) 2018  Cem Gokmen.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.cemgokmen.particles.misc;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.util.StringConverter;
import javafx.util.converter.BooleanStringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.NumberStringConverter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyUtils {

    public static List<PropertyWrapper> getPropertyWrappersFromObject(Object o, Class<?> baseClass) throws Exception {
        if (!baseClass.isInstance(o)) {
            throw new RuntimeException("Not instance of baseClass");
        }
        Map<String, Object> props = org.apache.commons.beanutils.PropertyUtils.describe(o);

        List<PropertyWrapper> propertyWrappers = new ArrayList<>();
        outer:
        for (Map.Entry<String, Object> prop : props.entrySet()) {
            String name = prop.getKey();
            Class<?> currentClass = o.getClass();
            while (!currentClass.equals(baseClass)) {
                Property property = getPropertyWithName(o, currentClass, name);
                if (property != null) {
                    propertyWrappers.add(new PropertyWrapper(name, property));
                    continue outer;
                }

                currentClass = currentClass.getSuperclass();
            }

            Property property = getPropertyWithName(o, currentClass, name);
            if (property != null) {
                propertyWrappers.add(new PropertyWrapper(name, property));
            }
        }

        return propertyWrappers;
    }

    public static Property getPropertyWithName(Object o, Class klass, String name) {
        try {
            Method m = klass.getMethod(name + "Property");
            return (Property) m.invoke(o);
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, String> getPropertyValues(Object o, Class<?> baseClass) throws Exception {
        List<PropertyWrapper> wrappers = getPropertyWrappersFromObject(o, baseClass);
        Map<String, String> values = new HashMap<>();

        wrappers.forEach(wrapper -> values.put(wrapper.getName(), wrapper.data.getValue()));
        return values;
    }

    public static class PropertyWrapper {
        private final StringProperty name;
        private final StringProperty data = new SimpleStringProperty();

        public PropertyWrapper(String name, Property property) {
            this.name = new ReadOnlyStringWrapper(name);

            if (property instanceof StringProperty) {
                Bindings.bindBidirectional(this.data, property);
            } else if (property instanceof DoubleProperty ||property instanceof IntegerProperty ) {
                StringConverter<Number> converter = new NumberStringConverter();
                Bindings.bindBidirectional(this.data, property, converter);
            } else if (property instanceof BooleanProperty) {
                StringConverter<Boolean> converter = new BooleanStringConverter();
                Bindings.bindBidirectional(this.data, property, converter);
            } else {
                throw new RuntimeException("Unsupported property type.");
            }
        }

        public Property dataProperty() {
            return this.data;
        }

        public String getName() {
            return this.name.get();
        }

        public StringProperty nameProperty() {
            return this.name;
        }
    }
}
