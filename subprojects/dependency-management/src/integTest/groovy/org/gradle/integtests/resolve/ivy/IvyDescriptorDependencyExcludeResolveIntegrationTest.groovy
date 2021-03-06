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

package org.gradle.integtests.resolve.ivy

import spock.lang.Ignore
import spock.lang.Unroll

class IvyDescriptorDependencyExcludeResolveIntegrationTest extends AbstractIvyDescriptorExcludeResolveIntegrationTest {
    /**
     * Dependency exclude for a single artifact by using a combination of name exclude rules.
     *
     * Dependency graph:
     * a -> b, c
     *
     * Exclude rules are applied to dependency "b".
     */
    @Ignore("Exclude does not work for artifacts of the same module")
    @Unroll
    def "dependency exclude having single artifact with matching #name"() {
        given:
        ivyRepo.module('b').publish()
        ivyRepo.module('c').publish()
        ivyRepo.module('a')
                .dependsOn('b')
                .dependsOn('c')
                .withXml {
            asNode().dependencies[0].dependency[0].appendNode(EXCLUDE_ATTRIBUTE, excludeAttributes)
        }
        .publish()

        when:
        succeeds 'check'

        then:
        file('libs').assertHasDescendants(['a-1.0.jar', 'c-1.0.jar'] as String[])

        where:
        name                              | excludeAttributes
        'all names'                       | [name: '*']
        'wildcard name'                   | [name: 'b*']
        'name'                            | [name: 'b']
        'name and type'                   | [name: 'b', type: 'jar']
        'name and ext'                    | [name: 'b', ext: 'jar']
        'name, type and ext'              | [name: 'b', type: 'jar', ext: 'jar']
        'org and name'                    | [org: 'org.gradle.test', name: 'b']
        'org, name and type'              | [org: 'org.gradle.test', name: 'b', type: 'jar']
        'org, name, type and ext'         | [org: 'org.gradle.test', name: 'b', type: 'jar', ext: 'jar']
        'org, module and name'            | [org: 'org.gradle.test', module: 'b', name: 'b']
        'org, module, name and type'      | [org: 'org.gradle.test', module: 'b', name: 'b', type: 'jar']
        'org, module, name, type and ext' | [org: 'org.gradle.test', module: 'b', name: 'b', type: 'jar', ext: 'jar']
    }

    /**
     * Exclude of transitive dependency with a single artifact by using a combination of name exclude rules.
     *
     * Dependency graph:
     * a -> b, c
     * b -> d
     * c -> e
     *
     * Exclude rules are applied to dependency "b".
     */
    @Ignore("Exclude of all artifacts (*) does not work for artifacts of the same module")
    @Unroll
    def "transitive dependency exclude having single artifact with matching #name"() {
        given:
        ivyRepo.module('d').publish()
        ivyRepo.module('b').dependsOn('d').publish()
        ivyRepo.module('e').publish()
        ivyRepo.module('c').dependsOn('e').publish()
        ivyRepo.module('a')
                .dependsOn('b')
                .dependsOn('c')
                .withXml {
            asNode().dependencies[0].dependency[0].appendNode(EXCLUDE_ATTRIBUTE, excludeAttributes)
        }
        .publish()

        when:
        succeeds 'check'

        then:
        file('libs').assertHasDescendants(resolvedJars as String[])

        where:
        name                              | excludeAttributes                                                         | resolvedJars
        'all names'                       | [name: '*']                                                               | ['a-1.0.jar', 'c-1.0.jar', 'e-1.0.jar']
        'wildcard name'                   | [name: 'd*']                                                              | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar']
        'name'                            | [name: 'd']                                                               | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar']
        'name and type'                   | [name: 'd', type: 'jar']                                                  | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar']
        'name and ext'                    | [name: 'd', ext: 'jar']                                                   | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar']
        'name, type and ext'              | [name: 'd', type: 'jar', ext: 'jar']                                      | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar']
        'org and name'                    | [org: 'org.gradle.test', name: 'd']                                       | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar']
        'org, name and type'              | [org: 'org.gradle.test', name: 'd', type: 'jar']                          | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar']
        'org, name, type and ext'         | [org: 'org.gradle.test', name: 'd', type: 'jar', ext: 'jar']              | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar']
        'org, module and name'            | [org: 'org.gradle.test', module: 'd', name: 'd']                          | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar']
        'org, module, name and type'      | [org: 'org.gradle.test', module: 'd', name: 'd', type: 'jar']             | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar']
        'org, module, name, type and ext' | [org: 'org.gradle.test', module: 'd', name: 'd', type: 'jar', ext: 'jar'] | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar']
    }

    /**
     * Exclude of transitive dependency with a single artifact does not exclude its transitive module by using a combination of name exclude rules.
     *
     * Dependency graph:
     * a -> b, c
     * b -> d -> f
     * c -> e
     *
     * Exclude rules are applied to dependency "b".
     */
    @Ignore("Exclude rule with a name attribute excludes matching module and not artifacts")
    @Unroll
    def "transitive dependency exclude having single artifact with matching #name does not exclude its transitive module"() {
        given:
        ivyRepo.module('f').publish()
        ivyRepo.module('d').dependsOn('f').publish()
        ivyRepo.module('b').dependsOn('d').publish()
        ivyRepo.module('e').publish()
        ivyRepo.module('c').dependsOn('e').publish()
        ivyRepo.module('a')
                .dependsOn('b')
                .dependsOn('c')
                .withXml {
            asNode().dependencies[0].dependency[0].appendNode(EXCLUDE_ATTRIBUTE, excludeAttributes)
        }
        .publish()

        when:
        succeeds 'check'

        then:
        file('libs').assertHasDescendants(resolvedJars as String[])

        where:
        name                              | excludeAttributes                                                         | resolvedJars
        'all names'                       | [name: '*']                                                               | ['a-1.0.jar', 'c-1.0.jar', 'e-1.0.jar']
        'wildcard name'                   | [name: 'd*']                                                              | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar', 'f-1.0.jar']
        'name'                            | [name: 'd']                                                               | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar', 'f-1.0.jar']
        'name and type'                   | [name: 'd', type: 'jar']                                                  | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar', 'f-1.0.jar']
        'name and ext'                    | [name: 'd', ext: 'jar']                                                   | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar', 'f-1.0.jar']
        'name, type and ext'              | [name: 'd', type: 'jar', ext: 'jar']                                      | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar', 'f-1.0.jar']
        'org and name'                    | [org: 'org.gradle.test', name: 'd']                                       | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar', 'f-1.0.jar']
        'org, name and type'              | [org: 'org.gradle.test', name: 'd', type: 'jar']                          | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar', 'f-1.0.jar']
        'org, name, type and ext'         | [org: 'org.gradle.test', name: 'd', type: 'jar', ext: 'jar']              | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar', 'f-1.0.jar']
        'org, module and name'            | [org: 'org.gradle.test', module: 'd', name: 'd']                          | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar', 'f-1.0.jar']
        'org, module, name and type'      | [org: 'org.gradle.test', module: 'd', name: 'd', type: 'jar']             | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar', 'f-1.0.jar']
        'org, module, name, type and ext' | [org: 'org.gradle.test', module: 'd', name: 'd', type: 'jar', ext: 'jar'] | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'e-1.0.jar', 'f-1.0.jar']
    }

    /**
     * Exclude of transitive dependency with multiple artifacts by using a combination of name exclude rules.
     *
     * Dependency graph:
     * a -> b, c
     * b -> d
     * c -> e
     *
     * Exclude rules are applied to dependency "b".
     */
    @Ignore("Exclude rule with a name attribute excludes matching module and not artifacts")
    @Unroll
    def "transitive dependency exclude having multiple artifacts with matching #name"() {
        given:
        ivyRepo.module('d')
                .artifact([:])
                .artifact([type: 'sources', classifier: 'sources', ext: 'jar'])
                .artifact([type: 'javadoc', classifier: 'javadoc', ext: 'jar'])
                .publish()
        ivyRepo.module('b').dependsOn('d').publish()
        ivyRepo.module('e').publish()
        ivyRepo.module('c').dependsOn('e').publish()
        ivyRepo.module('a')
                .dependsOn('b')
                .dependsOn('c')
                .withXml {
            asNode().dependencies[0].dependency[0].appendNode(EXCLUDE_ATTRIBUTE, excludeAttributes)
        }
        .publish()

        when:
        succeeds 'check'

        then:
        file('libs').assertHasDescendants(resolvedJars as String[])

        where:
        name                              | excludeAttributes                                                         | resolvedJars
        'all names'                       | [name: '*']                                                               | ['a-1.0.jar', 'c-1.0.jar', 'e-1.0.jar']
        'name'                            | [name: 'd']                                                               | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'd-1.0-javadoc.jar', 'd-1.0-sources.jar', 'e-1.0.jar']
        'name and type'                   | [name: 'd', type: 'jar']                                                  | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'd-1.0-javadoc.jar', 'd-1.0-sources.jar', 'e-1.0.jar']
        'name and ext'                    | [name: 'd', ext: 'jar']                                                   | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'd-1.0-javadoc.jar', 'd-1.0-sources.jar', 'e-1.0.jar']
        'name, type and ext'              | [name: 'd', type: 'jar', ext: 'jar']                                      | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'd-1.0-javadoc.jar', 'd-1.0-sources.jar', 'e-1.0.jar']
        'org and name'                    | [org: 'org.gradle.test', name: 'd']                                       | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'd-1.0-javadoc.jar', 'd-1.0-sources.jar', 'e-1.0.jar']
        'org, name and type'              | [org: 'org.gradle.test', name: 'd', type: 'jar']                          | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'd-1.0-javadoc.jar', 'd-1.0-sources.jar', 'e-1.0.jar']
        'org, name, type and ext'         | [org: 'org.gradle.test', name: 'd', type: 'jar', ext: 'jar']              | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'd-1.0-javadoc.jar', 'd-1.0-sources.jar', 'e-1.0.jar']
        'org, module and name'            | [org: 'org.gradle.test', module: 'd', name: 'd']                          | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'd-1.0-javadoc.jar', 'd-1.0-sources.jar', 'e-1.0.jar']
        'org, module, name and type'      | [org: 'org.gradle.test', module: 'd', name: 'd', type: 'jar']             | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'd-1.0-javadoc.jar', 'd-1.0-sources.jar', 'e-1.0.jar']
        'org, module, name, type and ext' | [org: 'org.gradle.test', module: 'd', name: 'd', type: 'jar', ext: 'jar'] | ['a-1.0.jar', 'b-1.0.jar', 'c-1.0.jar', 'd-1.0-javadoc.jar', 'd-1.0-sources.jar', 'e-1.0.jar']
    }
}
