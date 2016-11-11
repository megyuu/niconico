/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.niconico.mylasta.direction;

import javax.annotation.Resource;

import com.niconico.mylasta.direction.sponsor.NiconicoActionAdjustmentProvider;
import com.niconico.mylasta.direction.sponsor.NiconicoApiFailureHook;
import com.niconico.mylasta.direction.sponsor.NiconicoCookieResourceProvider;
import com.niconico.mylasta.direction.sponsor.NiconicoCurtainBeforeHook;
import com.niconico.mylasta.direction.sponsor.NiconicoJsonResourceProvider;
import com.niconico.mylasta.direction.sponsor.NiconicoListedClassificationProvider;
import com.niconico.mylasta.direction.sponsor.NiconicoMailDeliveryDepartmentCreator;
import com.niconico.mylasta.direction.sponsor.NiconicoSecurityResourceProvider;
import com.niconico.mylasta.direction.sponsor.NiconicoTimeResourceProvider;
import com.niconico.mylasta.direction.sponsor.NiconicoUserLocaleProcessProvider;
import com.niconico.mylasta.direction.sponsor.NiconicoUserTimeZoneProcessProvider;
import org.lastaflute.core.direction.CachedFwAssistantDirector;
import org.lastaflute.core.direction.FwAssistDirection;
import org.lastaflute.core.direction.FwCoreDirection;
import org.lastaflute.core.security.InvertibleCryptographer;
import org.lastaflute.core.security.OneWayCryptographer;
import org.lastaflute.db.dbflute.classification.ListedClassificationProvider;
import org.lastaflute.db.direction.FwDbDirection;
import org.lastaflute.thymeleaf.ThymeleafRenderingProvider;
import org.lastaflute.web.direction.FwWebDirection;
import org.lastaflute.web.ruts.renderer.HtmlRenderingProvider;

/**
 * @author jflute
 */
public class NiconicoFwAssistantDirector extends CachedFwAssistantDirector {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private NiconicoConfig config;

    // ===================================================================================
    //                                                                              Assist
    //                                                                              ======
    @Override
    protected void prepareAssistDirection(FwAssistDirection direction) {
        direction.directConfig(nameList -> nameList.add("niconico_config.properties"), "niconico_env.properties");
    }

    // ===================================================================================
    //                                                                               Core
    //                                                                              ======
    @Override
    protected void prepareCoreDirection(FwCoreDirection direction) {
        // this configuration is on niconico_env.properties because this is true only when development
        direction.directDevelopmentHere(config.isDevelopmentHere());

        // titles of the application for logging are from configurations
        direction.directLoggingTitle(config.getDomainTitle(), config.getEnvironmentTitle());

        // this configuration is on sea_env.properties because it has no influence to production
        // even if you set true manually and forget to set false back
        direction.directFrameworkDebug(config.isFrameworkDebug()); // basically false

        // you can add your own process when your application is booting
        direction.directCurtainBefore(createCurtainBeforeHook());

        direction.directSecurity(createSecurityResourceProvider());
        direction.directTime(createTimeResourceProvider());
        direction.directJson(createJsonResourceProvider());
        direction.directMail(createMailDeliveryDepartmentCreator().create());
    }

    protected NiconicoCurtainBeforeHook createCurtainBeforeHook() {
        return new NiconicoCurtainBeforeHook();
    }

    protected NiconicoSecurityResourceProvider createSecurityResourceProvider() { // #change_it_first
        final InvertibleCryptographer inver = InvertibleCryptographer.createAesCipher("niconico:dockside:");
        final OneWayCryptographer oneWay = OneWayCryptographer.createSha256Cryptographer();
        return new NiconicoSecurityResourceProvider(inver, oneWay);
    }

    protected NiconicoTimeResourceProvider createTimeResourceProvider() {
        return new NiconicoTimeResourceProvider(config);
    }

    protected NiconicoJsonResourceProvider createJsonResourceProvider() {
        return new NiconicoJsonResourceProvider();
    }

    protected NiconicoMailDeliveryDepartmentCreator createMailDeliveryDepartmentCreator() {
        return new NiconicoMailDeliveryDepartmentCreator(config);
    }

    // ===================================================================================
    //                                                                                 DB
    //                                                                                ====
    @Override
    protected void prepareDbDirection(FwDbDirection direction) {
        direction.directClassification(createListedClassificationProvider());
    }

    protected ListedClassificationProvider createListedClassificationProvider() {
        return new NiconicoListedClassificationProvider();
    }

    // ===================================================================================
    //                                                                                Web
    //                                                                               =====
    @Override
    protected void prepareWebDirection(FwWebDirection direction) {
        direction.directRequest(createUserLocaleProcessProvider(), createUserTimeZoneProcessProvider());
        direction.directCookie(createCookieResourceProvider());
        direction.directAdjustment(createActionAdjustmentProvider());
        direction.directMessage(nameList -> nameList.add("niconico_message"), "niconico_label");
        direction.directApiCall(createApiFailureHook());
        direction.directHtmlRendering(createHtmlRenderingProvider());
    }

    protected NiconicoUserLocaleProcessProvider createUserLocaleProcessProvider() {
        return new NiconicoUserLocaleProcessProvider();
    }

    protected NiconicoUserTimeZoneProcessProvider createUserTimeZoneProcessProvider() {
        return new NiconicoUserTimeZoneProcessProvider();
    }

    protected NiconicoCookieResourceProvider createCookieResourceProvider() { // #change_it_first
        final InvertibleCryptographer cr = InvertibleCryptographer.createAesCipher("dockside:niconico:");
        return new NiconicoCookieResourceProvider(config, cr);
    }

    protected NiconicoActionAdjustmentProvider createActionAdjustmentProvider() {
        return new NiconicoActionAdjustmentProvider();
    }

    protected NiconicoApiFailureHook createApiFailureHook() {
        return new NiconicoApiFailureHook();
    }

    protected HtmlRenderingProvider createHtmlRenderingProvider() {
        return new ThymeleafRenderingProvider().asDevelopment(config.isDevelopmentHere());
    }
}
