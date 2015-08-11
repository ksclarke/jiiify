/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package info.freelibrary.jiiify.templates;

import com.github.jknack.handlebars.Handlebars;

import info.freelibrary.jiiify.templates.impl.HandlebarsTemplateEngineImpl;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.templ.TemplateEngine;

/**
 * A template engine that uses the Handlebars library.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
@VertxGen
public interface HandlebarsTemplateEngine extends TemplateEngine {

    /**
     * Default max number of templates to cache
     */
    int DEFAULT_MAX_CACHE_SIZE = 10000;

    /**
     * Default template extension
     */
    String DEFAULT_TEMPLATE_EXTENSION = "hbs";

    /**
     * Create a template engine using defaults
     *
     * @return the engine
     */
    static HandlebarsTemplateEngine create() {
        return new HandlebarsTemplateEngineImpl();
    }

    /**
     * Set the extension for the engine
     *
     * @param aExtension The extension
     * @return a reference to this for fluency
     */
    HandlebarsTemplateEngine setExtension(String aExtension);

    /**
     * Set the max cache size for the engine
     *
     * @param maxCacheSize the maxCacheSize
     * @return a reference to this for fluency
     */
    HandlebarsTemplateEngine setMaxCacheSize(int aMaxCacheSize);

    /**
     * Get a reference to the internal Handlebars object so it can be configured.
     *
     * @return a reference to the internal Handlebars instance.
     */
    @GenIgnore
    Handlebars getHandlebars();

}
