/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2005-2007 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jakarta.faces.view.facelets;

/**
 * <p class="changed_added_2_0">
 * A mutable set of rules to be used in auto-wiring state to a particular object instance. Rules assigned to this object
 * will be composed into a single Metadata instance which will encapsulate the ruleset.
 * </p>
 *
 * @since 2.0
 */
public abstract class MetaRuleset {

    /**
     * <p class="changed_added_2_0">
     * Customize this <code>MetaRuleset</code> instance to advise it to ignore the attribute named by the
     * <code>attribute</code> argument, returning <code>this</code>.
     * </p>
     *
     * @param attribute the name of the attribute to ignore.
     * @return the MetaRuleset with the given attribute ignored.
     * @since 2.0
     */
    public abstract MetaRuleset ignore(String attribute);

    /**
     * <p class="changed_added_2_0">
     * Customize this <code>MetaRuleset</code> instance to advise it to ignore all attributes, returning <code>this</code>.
     * </p>
     *
     * @return the ignoreAll <code>MetaRuleset</code>.
     * @since 2.0
     */
    public abstract MetaRuleset ignoreAll();

    /**
     * <p class="changed_added_2_0">
     * Customize this <code>MetaRuleset</code> by removing the attribute named by argument <code>attribute</code> and
     * re-adding it under the name given by the argument <code>property</code>, returning <code>this</code>.
     * </p>
     *
     * @param attribute the attribute to remove.
     * @param property the property to add.
     * @return the aliased MetaRuleSet.
     * @since 2.0
     */
    public abstract MetaRuleset alias(String attribute, String property);

    /**
     * <p class="changed_added_2_0">
     * Add another {@link Metadata} to this ruleset, returning <code>this</code>.
     * </p>
     *
     * @param metadata the {@link Metadata} to add.
     * @return the {@link MetaRuleset} with the {@link Metadata} added.
     * @since 2.0
     */
    public abstract MetaRuleset add(Metadata metadata);

    /**
     * <p class="changed_added_2_0">
     * Add another {@link MetaRule} to this ruleset, returning <code>this</code>.
     * </p>
     *
     * @param rule the rule to add.
     * @return the {@link MetaRuleset} with the {@link MetaRule} added.
     * @since 2.0
     */
    public abstract MetaRuleset addRule(MetaRule rule);

    /**
     * <p class="changed_added_2_0">
     * Take actions to apply the rule.
     * </p>
     *
     * @return the Metadata with the MetaRuleSet applied.
     */
    public abstract Metadata finish();
}
