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

package info.freelibrary.jiiify.templates.impl;

import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.HBS_PATH_SKIP_KEY;
import static info.freelibrary.jiiify.Constants.MESSAGES;

import java.nio.file.Paths;
import java.util.Map;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.templates.HandlebarsTemplateEngine;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.impl.CachingTemplateEngine;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class HandlebarsTemplateEngineImpl extends CachingTemplateEngine<Template> implements HandlebarsTemplateEngine {

    private final Logger LOGGER = LoggerFactory.getLogger(HandlebarsTemplateEngineImpl.class, MESSAGES);

    private final Handlebars myHandlebars;

    /**
     * Creates a Handlebars template engine implementation.
     */
    public HandlebarsTemplateEngineImpl() {
        super(HandlebarsTemplateEngine.DEFAULT_TEMPLATE_EXTENSION, HandlebarsTemplateEngine.DEFAULT_MAX_CACHE_SIZE);
        myHandlebars = new Handlebars(new ClassPathTemplateLoader("/webroot"));
        LOGGER.debug(MessageCodes.DBG_087);
    }

    @Override
    public HandlebarsTemplateEngine setExtension(final String aExtension) {
        LOGGER.debug(MessageCodes.DBG_088, aExtension);
        doSetExtension(aExtension);
        return this;
    }

    @Override
    public HandlebarsTemplateEngine setMaxCacheSize(final int aMaxCacheSize) {
        LOGGER.debug(MessageCodes.DBG_089, aMaxCacheSize);
        this.cache.setMaxSize(aMaxCacheSize);
        return null;
    }

    @Override
    public void render(final RoutingContext aContext, final String aTemplateDirName, final String aTemplateFileName,
            final Handler<AsyncResult<Buffer>> aHandler) {
        render(aContext, Paths.get(aTemplateDirName, aTemplateFileName).toString(), aHandler);
    }

    @Override
    public void render(final RoutingContext aContext, final String aTemplateFileName,
            final Handler<AsyncResult<Buffer>> aHandler) {
        final Object skip = aContext.data().get(HBS_PATH_SKIP_KEY);
        final String templateFileName;

        if (skip != null) {
            final String[] pathParts = aTemplateFileName.split("/");
            final StringBuilder pathBuilder = new StringBuilder();

            for (int index = 0; index < pathParts.length - (int) skip; index++) {
                pathBuilder.append(pathParts[index]).append('/');
            }

            templateFileName = pathBuilder.deleteCharAt(pathBuilder.length() - 1).toString();
            LOGGER.debug(MessageCodes.DBG_090, skip, aTemplateFileName);
        } else {
            LOGGER.debug(MessageCodes.DBG_091, aTemplateFileName);
            // FIXME: Seems some paths are coming in with /admin/ and some aren't... related to POST(?!)
            templateFileName = aTemplateFileName.replaceAll("\\/admin\\/", "/");
        }

        try {
            Template template = cache.get(templateFileName);

            if (template == null) {
                LOGGER.debug(MessageCodes.DBG_092, templateFileName);

                synchronized (this) {
                    template = myHandlebars.compile(templateFileName);
                    cache.put(templateFileName, template);
                }
            }

            final Map<String, Object> dataMap = aContext.data();
            final String templateOutput = template.apply(dataMap.get(HBS_DATA_KEY));

            LOGGER.debug(MessageCodes.DBG_093, templateOutput);
            aHandler.handle(Future.succeededFuture(Buffer.buffer(templateOutput)));
        } catch (final Exception details) {
            LOGGER.error(details, details.getMessage());
            aHandler.handle(Future.failedFuture(details));
        }
    }

    @Override
    public Handlebars getHandlebars() {
        return myHandlebars;
    }

}
