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

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.Module;
import org.gradle.api.artifacts.PublishArtifact;

import java.io.File;

public class IvyNormalizedPublication {

    private final Module module;
    private final File descriptorFile;
    private final Iterable<PublishArtifact> artifacts;
    private final Iterable<Dependency> runtimeDependencies;

    public IvyNormalizedPublication(Module module, Iterable<PublishArtifact> artifacts, Iterable<Dependency> runtimeDependencies, File descriptorFile) {
        this.module = module;
        this.artifacts = artifacts;
        this.runtimeDependencies = runtimeDependencies;
        this.descriptorFile = descriptorFile;
    }

    public Module getModule() {
        return module;
    }

    public File getDescriptorFile() {
        return descriptorFile;
    }

    public Iterable<PublishArtifact> getArtifacts() {
        return artifacts;
    }

    public Iterable<Dependency> getRuntimeDependencies() {
        return runtimeDependencies;
    }
}
