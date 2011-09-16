/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.idp.attribute.filtering.impl.policy;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.idp.attribute.filtering.AttributeFilterContext;

import org.opensaml.util.StringSupport;
import org.opensaml.util.component.ComponentInitializationException;
import org.opensaml.util.component.InitializableComponent;
import org.opensaml.util.component.UninitializedComponentException;
import org.opensaml.util.component.UnmodifiableComponent;
import org.opensaml.util.component.UnmodifiableComponentException;
import org.opensaml.util.criteria.AbstractBiasedEvaluableCriterion;
import org.opensaml.util.criteria.EvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement the Scripted Criterion.
 * 
 * This script is given a single {@link AttributeFilterContext} and is expected to return a boolean.<br />
 * 
 * In contrast with the V2 version which would be called iteratively this is called once. It is is given the entire
 * context allowing arbitrary complexity.
 */
@ThreadSafe
public class ScriptedCriterion extends AbstractBiasedEvaluableCriterion<AttributeFilterContext> implements
        InitializableComponent, UnmodifiableComponent {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ScriptedCriterion.class);

    /** The scripting language. */
    private String scriptLanguage;

    /** The script to execute. */
    private String script;

    /** The script engine to execute the script. */
    private ScriptEngine scriptEngine;

    /** The compiled form of the script, if the script engine supports compiling. */
    private CompiledScript compiledScript;

    /** Initialization state. */
    private boolean initialized;

    /**
     * Has initialize been called on this object. {@inheritDoc}.
     * */
    public final boolean isInitialized() {
        return initialized;
    }

    /**
     * Initialize. Check parameters and try to compile the script {@inheritDoc}
     */
    public final synchronized void initialize() throws ComponentInitializationException {
        if (isInitialized()) {
            return;
        }

        if (null == scriptLanguage) {
            throw new ComponentInitializationException("ScriptedCriterion: No language set.");
        }

        if (null == script) {
            throw new ComponentInitializationException("ScriptedCriterion: No script set.");
        }

        final ScriptEngineManager sem = new ScriptEngineManager();
        scriptEngine = sem.getEngineByName(scriptLanguage);
        if (null == scriptEngine) {
            throw new ComponentInitializationException("ScriptedCriterion: No valid language set.");
        }
        compiledScript = compileScript();
        initialized = true;
    }

    /**
     * Set the language we are using.
     * 
     * @param theLanguage what we use.
     */
    public synchronized void setLanguage(final String theLanguage) {
        if (isInitialized()) {
            throw new UnmodifiableComponentException("ScriptletCriterion has already been initialized");
        }
        scriptLanguage = StringSupport.trimOrNull(theLanguage);
    }

    /**
     * Gets the scripting language used.
     * 
     * @return scripting language used. This is always a valid language.
     */
    public String getScriptLanguage() {
        return scriptLanguage;
    }

    /**
     * Set the actual Script we will use.
     * 
     * @param theScript what to use.
     */
    public synchronized void setScript(final String theScript) {
        if (isInitialized()) {
            throw new UnmodifiableComponentException("ScriptletCriterion has already been initialized");
        }
        script = StringSupport.trimOrNull(theScript);
    }

    /**
     * Gets the script that will be executed.
     * 
     * @return script that will be executed. This is never null or empty.
     */
    public String getScript() {
        return script;
    }

    /**
     * Get this resolver's script engine.
     * 
     * @return the engine. Never null.
     */
    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    /**
     * Get the compiled script for this engine. Note that this will be null if the language does not support
     * compilation.
     * 
     * @return the compiled script.
     */
    public CompiledScript getCompiledScript() {
        return compiledScript;
    }

    /**
     * Compiles the script if the scripting engine supports it.
     * 
     * @return the compiled script.
     */
    protected CompiledScript compileScript() {
        try {
            if (scriptEngine != null && scriptEngine instanceof Compilable) {
                return ((Compilable) scriptEngine).compile(script);
            }
        } catch (ScriptException e) {
            log.warn("ScriptedCriterion cannot compile {} even though the scripting engine"
                    + " supports this functionality: {}", script, e.toString());
            log.debug("Fails", e);
        }
        return null;
    }

    /** {@inheritDoc} */
    public Boolean doEvaluate(final AttributeFilterContext filterContext) throws EvaluationException {
        final SimpleScriptContext scriptContext = new SimpleScriptContext();
        scriptContext.setAttribute("filterContext", filterContext, ScriptContext.ENGINE_SCOPE);
        final Boolean result;

        if (!isInitialized()) {
            throw new UninitializedComponentException("ScriptedMatcher has not been initialized");
        }

        try {
            if (compiledScript != null) {
                result = (Boolean) compiledScript.eval(scriptContext);
            } else {
                result = (Boolean) scriptEngine.eval(script, scriptContext);
            }

            if (result != null) {
                return result.booleanValue();
            } else {
                return Boolean.FALSE;
            }
        } catch (ScriptException e) {
            throw new EvaluationException("Unable to execute policy script", e);
        }
    }
}