/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.global.RequestDelayControl;
import com.github.tomakehurst.wiremock.global.ThreadSafeRequestDelayControl;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.jetty.DelayableSocketConnector;
import com.github.tomakehurst.wiremock.jetty.DelayableSslSocketConnector;
import com.github.tomakehurst.wiremock.servlet.ContentTypeSettingFilter;
import com.github.tomakehurst.wiremock.servlet.HandlerDispatchingServlet;
import com.github.tomakehurst.wiremock.servlet.TrailingSlashFilter;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsLoader;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.stubbing.StubMappingJsonRecorder;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockApp.ADMIN_CONTEXT_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.servlet.HandlerDispatchingServlet.SHOULD_FORWARD_TO_FILES_CONTEXT;
import static com.google.common.collect.Maps.newHashMap;

public class WireMockServer {

	public static final String FILES_ROOT = "__files";
	private static final String FILES_URL_MATCH = String.format("/%s/*", FILES_ROOT);
	
	private final WireMockApp wireMockApp;
    private final AdminRequestHandler adminRequestHandler;
    private final StubRequestHandler stubRequestHandler;

	
	private Server jettyServer;
    private final RequestDelayControl requestDelayControl;
	private final FileSource fileSource;
	private final Notifier notifier;
	private final int port;

    private final Options options;

    public WireMockServer(Options options) {
        this.options = options;
        this.fileSource = options.filesRoot();
        this.port = options.portNumber();
        this.notifier = options.notifier();

        requestDelayControl = new ThreadSafeRequestDelayControl();

        MappingsLoader defaultMappingsLoader = makeDefaultMappingsLoader();
        wireMockApp = new WireMockApp(requestDelayControl, options.browserProxyingEnabled(), defaultMappingsLoader, options.journalCapacity());

        adminRequestHandler = new AdminRequestHandler(wireMockApp, new BasicResponseRenderer());
        stubRequestHandler = new StubRequestHandler(wireMockApp,
                new StubResponseRenderer(fileSource.child(FILES_ROOT),
                        wireMockApp.getGlobalSettingsHolder(),
                        new ProxyResponseRenderer(options.proxyVia())));
    }

    private MappingsLoader makeDefaultMappingsLoader() {
        FileSource mappingsFileSource = fileSource.child("mappings");
        if (mappingsFileSource.exists()) {
            return new JsonFileMappingsLoader(mappingsFileSource);
        } else {
            return new NoOpMappingsLoader();
        }
    }

    public WireMockServer(int port, Integer httpsPort, FileSource fileSource, boolean enableBrowserProxying, ProxySettings proxySettings, Notifier notifier, Integer journalCapacity) {
        this(wireMockConfig()
                .port(port)
                .httpsPort(httpsPort)
                .fileSource(fileSource)
                .enableBrowserProxying(enableBrowserProxying)
                .proxyVia(proxySettings)
                .notifier(notifier)
                .journalCapacity(journalCapacity));
    }

	public WireMockServer(int port, FileSource fileSource, boolean enableBrowserProxying, ProxySettings proxySettings) {
        this(wireMockConfig()
                .port(port)
                .fileSource(fileSource)
                .enableBrowserProxying(enableBrowserProxying)
                .proxyVia(proxySettings));
	}

    public WireMockServer(int port, FileSource fileSource, boolean enableBrowserProxying) {
        this(wireMockConfig()
                .port(port)
                .fileSource(fileSource)
                .enableBrowserProxying(enableBrowserProxying));
    }
	
	public WireMockServer(int port) {
		this(wireMockConfig().port(port));
	}

    public WireMockServer(int port, Integer httpsPort) {
        this(wireMockConfig().port(port).httpsPort(httpsPort));
    }
	
	public WireMockServer() {
		this(wireMockConfig());
	}
	
	public void loadMappingsUsing(final MappingsLoader mappingsLoader) {
		wireMockApp.loadMappingsUsing(mappingsLoader);
	}
	
	public void addMockServiceRequestListener(RequestListener listener) {
		stubRequestHandler.addRequestListener(listener);
	}
	
	public void enableRecordMappings(FileSource mappingsFileSource, FileSource filesFileSource) {
	    addMockServiceRequestListener(
                new StubMappingJsonRecorder(mappingsFileSource, filesFileSource, wireMockApp));
	    notifier.info("Recording mappings to " + mappingsFileSource.getPath());
	}
	
	public void stop() {
		try {
			jettyServer.stop();
            jettyServer.join();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void start() {
		try {
            jettyServer = new Server();
            jettyServer.addConnector(createHttpConnector());

            if (options.httpsSettings().enabled()) {
                jettyServer.addConnector(createHttpsConnector());
            }

            addAdminContext();
            addMockServiceContext();
			jettyServer.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    private DelayableSocketConnector createHttpConnector() {
        DelayableSocketConnector connector = new DelayableSocketConnector(requestDelayControl);
        connector.setPort(port);
        connector.setHeaderBufferSize(8192);
        return connector;
    }

    private DelayableSslSocketConnector createHttpsConnector() {
        DelayableSslSocketConnector connector = new DelayableSslSocketConnector(requestDelayControl);
        connector.setPort(options.httpsSettings().port());
        connector.setHeaderBufferSize(8192);
        connector.setKeystore(options.httpsSettings().keyStorePath());
        connector.setKeyPassword("password");
        return connector;
    }

    public boolean isRunning() {
        return jettyServer != null && jettyServer.isRunning();
    }

    @SuppressWarnings({"rawtypes", "unchecked" })
    private void addMockServiceContext() {
        Context mockServiceContext = new Context(jettyServer, "/");
        
        Map initParams = newHashMap();
        initParams.put("org.mortbay.jetty.servlet.Default.maxCacheSize", "0");
        initParams.put("org.mortbay.jetty.servlet.Default.resourceBase", fileSource.getPath());
        initParams.put("org.mortbay.jetty.servlet.Default.dirAllowed", "false");
        mockServiceContext.setInitParams(initParams);
        
        mockServiceContext.addServlet(DefaultServlet.class, FILES_URL_MATCH);
        
		mockServiceContext.setAttribute(StubRequestHandler.class.getName(), stubRequestHandler);
		mockServiceContext.setAttribute(Notifier.KEY, notifier);
		ServletHolder servletHolder = mockServiceContext.addServlet(HandlerDispatchingServlet.class, "/");
		servletHolder.setInitParameter(RequestHandler.HANDLER_CLASS_KEY, StubRequestHandler.class.getName());
		servletHolder.setInitParameter(SHOULD_FORWARD_TO_FILES_CONTEXT, "true");
		
		MimeTypes mimeTypes = new MimeTypes();
		mimeTypes.addMimeMapping("json", "application/json");
		mimeTypes.addMimeMapping("html", "text/html");
		mimeTypes.addMimeMapping("xml", "application/xml");
		mimeTypes.addMimeMapping("txt", "text/plain");
		mockServiceContext.setMimeTypes(mimeTypes);
		
		mockServiceContext.setWelcomeFiles(new String[] { "index.json", "index.html", "index.xml", "index.txt" });
		
		mockServiceContext.addFilter(ContentTypeSettingFilter.class, FILES_URL_MATCH, Handler.FORWARD);
		mockServiceContext.addFilter(TrailingSlashFilter.class, FILES_URL_MATCH, Handler.ALL);
		
		jettyServer.addHandler(mockServiceContext);
    }

    private void addAdminContext() {
        Context adminContext = new Context(jettyServer, ADMIN_CONTEXT_ROOT);
		ServletHolder servletHolder = adminContext.addServlet(HandlerDispatchingServlet.class, "/");
		servletHolder.setInitParameter(RequestHandler.HANDLER_CLASS_KEY, AdminRequestHandler.class.getName());
		adminContext.setAttribute(AdminRequestHandler.class.getName(), adminRequestHandler);
		adminContext.setAttribute(Notifier.KEY, notifier);
		jettyServer.addHandler(adminContext);
    }


    private static class NoOpMappingsLoader implements MappingsLoader {
        @Override
        public void loadMappingsInto(StubMappings stubMappings) {
            // do nothing
        }
    }
}
