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

package net.shibboleth.idp.spring;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * An {@link ApplicationContextInitializer} which appends properties to the application context's environment.
 * 
 * Properties are loaded from {@link #IDP_PROPERTIES} as well as additional property files specified by
 * {@link #IDP_ADDITIONAL_PROPERTY} if set, or if absent, by locating all files under idp.home/conf/ that
 * end in ".properties".
 * 
 * The {@link #IDP_PROPERTIES} file is searched for in the well location returned by {@link #getSearchLocation()}.
 * 
 * If not already set, the {@link #IDP_HOME_PROPERTY} will be set to the first search location in which the
 * {@link #IDP_PROPERTIES} file is found.
 * 
 * A {@link ConstraintViolationException} will be thrown if the property files can not be found or loaded and
 * {@link #isFailFast(ConfigurableApplicationContext)} returns true.
 */
public class IdPPropertiesApplicationContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /** IdP home property. */
    @Nonnull @NotEmpty public static final String IDP_HOME_PROPERTY = "idp.home";

    /** Property that points to more property sources. */
    @Nonnull @NotEmpty public static final String IDP_ADDITIONAL_PROPERTY = "idp.additionalProperties";

    /** Property that controls auto-search for property sources. */
    @Nonnull @NotEmpty public static final String IDP_AUTOSEARCH_PROPERTY = "idp.searchForProperties";

    /** Target resource to be searched for. */
    @Nonnull public static final String IDP_PROPERTIES = "/conf/idp.properties";

    /** Well known search location. */
    @Nonnull public static final String SEARCH_LOCATION = "/opt/shibboleth-idp";

    /** Property controlling whether to fail fast. */
    @Nonnull public static final String FAILFAST_PROPERTY = "idp.initializer.failFast";

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(IdPPropertiesApplicationContextInitializer.class);

    /** {@inheritDoc} */
    @Override public void initialize(@Nonnull final ConfigurableApplicationContext applicationContext) {
        log.debug("Initializing application context '{}'", applicationContext);

        // TODO: Override default property replacement syntax.
        // We can't do this now because it would break web.xml's use of ${idp.home}
        // If we end up breaking web.xml later, I think we could force that in line.
        // See IDP-1642
        // applicationContext.getEnvironment().setPlaceholderPrefix("%{");
        // applicationContext.getEnvironment().setPlaceholderSuffix("}");
        
        final String searchLocation = selectSearchLocation(applicationContext);
        log.debug("Attempting to find '{}' at search location '{}'", getSearchTarget(), searchLocation);

        final String searchPath = searchLocation + getSearchTarget();

        log.debug("Attempting to find resource '{}'", searchPath);
        final Resource resource = applicationContext.getResource(searchPath);

        if (resource.exists()) {
            log.debug("Found resource '{}' at search path '{}'", resource, searchPath);

            final Properties properties = loadProperties(null, resource);
            if (properties == null) {
                if (isFailFast(applicationContext)) {
                    log.error("Unable to load properties from resource '{}'", resource);
                    throw new ConstraintViolationException("Unable to load properties from resource");
                }
                log.warn("Unable to load properties from resource '{}'", resource);
                return;
            }

            if ("classpath:".equals(searchLocation) || (resource instanceof ClassPathResource)) {
                setIdPHomeProperty(searchLocation, properties);
            } else {
                String searchLocationAbsolutePath = Paths.get(searchLocation).toAbsolutePath().toString();
                // Minimal normalization required on Windows to allow SWF's flow machinery to work.
                // Just replace backslashes with forward slashes.
                if (File.separatorChar == '\\') {
                    searchLocationAbsolutePath = searchLocationAbsolutePath.replace('\\', '/');
                }
                setIdPHomeProperty(searchLocationAbsolutePath, properties);
            }

            loadAdditionalPropertySources(applicationContext, searchLocation, properties);

            logProperties(properties);

            appendPropertySource(applicationContext, resource.toString(), properties);

        } else if (isFailFast(applicationContext)) {
            log.error("Unable to find '{}' at '{}'", getSearchTarget(), searchLocation);
            throw new ConstraintViolationException(
                    "Unable to find '" + getSearchTarget() + "' at '" + searchLocation + "'");
        } else {
            log.warn("Unable to find '{}' at '{}'", getSearchTarget(), searchLocation);
        }
    }

    /**
     * Get the target resource to be searched for. Defaults to {@link #IDP_PROPERTIES}.
     * 
     * @return the target resource to be searched for
     */
    @Nonnull public String getSearchTarget() {
        return IDP_PROPERTIES;
    }

    /**
     * Get the well known search location. Defaults to {@link #SEARCH_LOCATION}.
     * 
     * @return the well known search locations
     */
    @Nonnull public String getSearchLocation() {
        return SEARCH_LOCATION;
    }

    /**
     * Select the location used to search for the target. Prefers the user-defined search location defined by
     * {@link #IDP_HOME_PROPERTY} in the application context. Defaults to the well-known search location returned from
     * {@link #getSearchLocation()}.
     * 
     * @param applicationContext the application context
     * @return the search location used to search for the target
     * @throws ConstraintViolationException if the user-defined search location is empty or ends with '/' and
     *             {@link #isFailFast(ConfigurableApplicationContext)} is true
     */
    @Nonnull public String selectSearchLocation(@Nonnull final ConfigurableApplicationContext applicationContext) {

        Constraint.isNotNull(applicationContext, "Application context cannot be null");
        final String homeProperty = applicationContext.getEnvironment().getProperty(IDP_HOME_PROPERTY);
        if (homeProperty != null && isFailFast(applicationContext)) {
            Constraint.isNotEmpty(homeProperty, "idp.home cannot be empty");
            Constraint.isFalse(homeProperty.endsWith("/"), "idp.home cannot end with '/'");
        }
        return (homeProperty != null) ? homeProperty : getSearchLocation();
    }

    /**
     * Load properties from the resource.
     * 
     * @param sink if non-null use this instance as the target
     * @param resource the resource
     * @return properties loaded from the resource or {@code  null} if loading failed
     */
    @Nullable public Properties loadProperties(@Nullable final Properties sink, @Nonnull final Resource resource) {
        Constraint.isNotNull(resource, "Resource cannot be null");
        try {
            final Properties properties;
            if (sink != null) {
                properties = sink;
            } else {
                properties = new Properties();
            }
            PropertiesLoaderUtils.fillProperties(properties, resource);
            return properties;
        } catch (final IOException e) {
            log.warn("Unable to load properties from resource '{}'", resource, e);
            return null;
        }
    }

// Checkstyle: CyclomaticComplexity OFF
    /**
     * Load additional property sources.
     * 
     * File names of additional property sources are defined by {@link #IDP_ADDITIONAL_PROPERTY}, and are resolved
     * relative to the given search location.
     * 
     * @param applicationContext the application context
     * @param searchLocation the location from which additional property sources are resolved
     * @param properties the properties to be filled with additional property sources
     * @throws ConstraintViolationException if an error occurs loading the additional property sources and
     *             {@link #isFailFast(ConfigurableApplicationContext)} is true
     */
    public void loadAdditionalPropertySources(@Nonnull final ConfigurableApplicationContext applicationContext,
            @Nonnull final String searchLocation, @Nonnull final Properties properties) {
        
        final Collection<String> sources = new ArrayList<>();
        
        final Boolean autosearch = Boolean.valueOf(properties.getProperty(IDP_AUTOSEARCH_PROPERTY, "false"));
        if (autosearch) {
            final Path searchRoot = Path.of(searchLocation).resolve("conf");
            if (searchRoot.toFile().isDirectory()) {
                try (final Stream<Path> paths = Files.find(searchRoot, Integer.MAX_VALUE,
                        new BiPredicate<Path,BasicFileAttributes>() {
                                public boolean test(final Path t, final BasicFileAttributes u) {
                                    if (u.isRegularFile() && t.getFileName().toString().endsWith(".properties")
                                            && !t.endsWith(Path.of(IDP_PROPERTIES))) {
                                        log.debug("Including auto-located properties in {}", t);
                                        return true;
                                    }
                                    return false;
                                }
                        }, FileVisitOption.FOLLOW_LINKS)) {
                    
                    sources.addAll(paths.map(Path::toString).collect(Collectors.toUnmodifiableList()));
                } catch (final IOException e) {
                    log.error("Error searching for additional properties", e);
                }
            }
        }
        
        final String additionalSources = properties.getProperty(IDP_ADDITIONAL_PROPERTY);
        if (additionalSources != null) {
            final String[] split = additionalSources.split(",");
            for (final String s : split) {
                final String trimmedSource = StringSupport.trimOrNull(s);
                if (trimmedSource != null) {
                    sources.add(searchLocation + trimmedSource);
                }
            }
        }
        
        for (final String source : sources) {
            log.debug("Attempting to load properties from resource '{}'", source);
            final Resource additionalResource = applicationContext.getResource(source);
            if (additionalResource.exists()) {
                log.debug("Found property resource '{}'", additionalResource);
                if (loadProperties(properties, additionalResource) == null) {
                    if (isFailFast(applicationContext)) {
                        log.error("Unable to load properties from resource '{}'", additionalResource);
                        throw new ConstraintViolationException("Unable to load properties from resource");
                    }
                    log.warn("Unable to load properties from resource '{}'", additionalResource);
                    continue;
                }
            } else {
                log.warn("Unable to find property resource '{}'", additionalResource);
            }
        }
    }
// Checkstyle: CyclomaticComplexity ON
    
    /**
     * Log property names and values at debug level, suppressing properties whose name matches 'password',
     * 'credential', 'secret', or 'salt'.
     * 
     * @param properties the properties to log
     */
    public void logProperties(@Nonnull final Properties properties) {
        if (log.isDebugEnabled()) {
            final Pattern pattern = Pattern.compile("password|credential|secret|salt|key", Pattern.CASE_INSENSITIVE);
            for (final String name : new TreeSet<>(properties.stringPropertyNames())) {
                final Object value = pattern.matcher(name).find() ? "<suppressed>" : properties.get(name);
                log.debug("Loaded property '{}'='{}'", name, value);
            }
        }
    }

    /**
     * Add property source to the application context environment with lowest precedence.
     * 
     * @param applicationContext the application context
     * @param name the name of the property source to be added to the application context
     * @param properties the properties added to the application context
     */
    public void appendPropertySource(@Nonnull final ConfigurableApplicationContext applicationContext,
            @Nonnull final String name, @Nonnull final Properties properties) {
        applicationContext.getEnvironment().getPropertySources()
                .addLast(new PropertiesPropertySource(name, properties));
    }

    /**
     * Set the {@link #IDP_HOME_PROPERTY} property to the given path if not already set.
     * 
     * @param path the property value
     * @param properties the properties
     */
    public void setIdPHomeProperty(@Nonnull final String path, @Nonnull final Properties properties) {
        Constraint.isNotNull(path, "Path cannot be null");
        Constraint.isNotNull(properties, "Properties cannot be null");

        if (properties.getProperty(IDP_HOME_PROPERTY) != null) {
            log.debug("Will not set '{}' property because it is already set.", IDP_HOME_PROPERTY);
            return;
        }

        log.debug("Setting '{}' property to '{}'", IDP_HOME_PROPERTY, path);

        properties.setProperty(IDP_HOME_PROPERTY, path);
    }

    /**
     * Whether we fail immediately if the config is bogus. Defaults to true. Controlled by the value of the
     * {@link #FAILFAST_PROPERTY}.
     * <b>This functionality is reserved for use in tests </b> where is is usually used to allow
     * tests to be run in the presence of partial configuration.
     * 
     * @param applicationContext the application context
     * @return whether we fail immediately if the config is faulty or incomplete.
     */
    public boolean isFailFast(@Nonnull final ConfigurableApplicationContext applicationContext) {
        Constraint.isNotNull(applicationContext, "Application context cannot be null");
        final String failFast = applicationContext.getEnvironment().getProperty(FAILFAST_PROPERTY);
        return (failFast == null) ? true : Boolean.parseBoolean(failFast);
    }

}
