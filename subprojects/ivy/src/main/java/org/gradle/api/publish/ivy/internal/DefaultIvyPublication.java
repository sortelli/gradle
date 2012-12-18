/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.api.publish.ivy.internal;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Module;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.configurations.DependencyMetaDataProvider;
import org.gradle.api.internal.component.ComponentInternal;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.file.collections.DefaultConfigurableFileCollection;
import org.gradle.api.internal.tasks.TaskResolver;
import org.gradle.api.publish.ivy.IvyModuleDescriptor;
import org.gradle.internal.reflect.Instantiator;

import java.io.File;
import java.util.concurrent.Callable;

public class DefaultIvyPublication implements IvyPublicationInternal {

    private final String name;
    private final IvyModuleDescriptorInternal descriptor;
    private final DependencyMetaDataProvider dependencyMetaDataProvider;
    private final ComponentInternal component;
    private final FileResolver fileResolver;
    private final TaskResolver taskResolver;
    private PublishArtifact descriptorArtifact;

    public DefaultIvyPublication(
            String name, Instantiator instantiator, ComponentInternal component,
            DependencyMetaDataProvider dependencyMetaDataProvider, FileResolver fileResolver, TaskResolver taskResolver
    ) {
        this.name = name;
        this.component = component;
        this.dependencyMetaDataProvider = dependencyMetaDataProvider;
        this.fileResolver = fileResolver;
        this.taskResolver = taskResolver;
        this.descriptor = instantiator.newInstance(DefaultIvyModuleDescriptor.class, this);
    }

    public String getName() {
        return name;
    }

    public IvyModuleDescriptorInternal getDescriptor() {
        return descriptor;
    }

    public void descriptor(Action<? super IvyModuleDescriptor> configure) {
        configure.execute(descriptor);
    }

    public FileCollection getPublishableFiles() {
        ConfigurableFileCollection files = new DefaultConfigurableFileCollection("publication artifacts", fileResolver, taskResolver);
        files.from(component.getArtifacts().getFiles());
        if (descriptorArtifact != null) {
            files.from(new Callable<File>() {
                public File call() throws Exception {
                    return getDescriptorFile();
                }
            });
            files.builtBy(descriptorArtifact);
        }
        return files;
    }

    public IvyNormalizedPublication asNormalisedPublication() {
        return new IvyNormalizedPublication(getModule(), component.getArtifacts(), component.getRuntimeDependencies(), getDescriptorFile());
    }

    public Module getModule() {
        return dependencyMetaDataProvider.getModule();
    }

    public Class<IvyNormalizedPublication> getNormalisedPublicationType() {
        return IvyNormalizedPublication.class;
    }

    public void setDescriptorArtifact(PublishArtifact descriptorArtifact) {
        this.descriptorArtifact = descriptorArtifact;
    }

    private File getDescriptorFile() {
        return descriptorArtifact.getFile();
    }

}
