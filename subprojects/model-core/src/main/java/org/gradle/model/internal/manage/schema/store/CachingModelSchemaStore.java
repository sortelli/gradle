/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.model.internal.manage.schema.store;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.util.concurrent.UncheckedExecutionException;
import net.jcip.annotations.NotThreadSafe;
import org.gradle.internal.Cast;
import org.gradle.model.internal.core.ModelType;
import org.gradle.model.internal.manage.schema.ModelProperty;
import org.gradle.model.internal.manage.schema.ModelSchema;

import java.util.concurrent.ExecutionException;

import static org.gradle.internal.UncheckedException.throwAsUncheckedException;

@NotThreadSafe
public class CachingModelSchemaStore implements ModelSchemaStore {

    private final ModelSchemaExtractor extractor;

    public CachingModelSchemaStore(ModelSchemaExtractor extractor) {
        this.extractor = extractor;
    }

    private final LoadingCache<ModelType<?>, ExtractedModelSchema<?>> schemas = CacheBuilder.newBuilder().build(new CacheLoader<ModelType<?>, ExtractedModelSchema<?>>() {
        @Override
        public ExtractedModelSchema<?> load(ModelType<?> key) throws Exception {
            return extractor.extract(key);
        }
    });

    public <T> ModelSchema<T> getSchema(ModelType<T> type) {
        try {
            ExtractedModelSchema<?> schema = schemas.getIfPresent(type);
            if (schema == null) {
                schema = schemas.get(type);
                determineProperties(schema);
            }
            return Cast.uncheckedCast(schema);
        } catch (ExecutionException e) {
            throw throwAsUncheckedException(e.getCause());
        } catch (UncheckedExecutionException e) {
            throw throwAsUncheckedException(e.getCause());
        }
    }

    public boolean isManaged(ModelType<?> type) {
        return extractor.isManaged(type.getRawClass());
    }

    private void determineProperties(ExtractedModelSchema<?> schema) {
        ImmutableSortedMap.Builder<String, ModelProperty<?>> builder = ImmutableSortedMap.naturalOrder();
        for (ModelPropertyFactory<?> propertyFactory : schema.getPropertyFactories()) {
            ModelProperty<?> property = propertyFactory.create(this);
            builder.put(property.getName(), property);
        }
        schema.setProperties(builder.build());
    }
}
