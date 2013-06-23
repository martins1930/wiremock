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
package com.github.tomakehurst.wiremock.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;

public class WireMockAltHostAndPortAcceptanceTest {

    
        private static final int PORT = 8099 ;
	private WireMockServer defaultServer;
	private WireMockServer altServer;
	
	@Before
	public void init() {
		defaultServer = new WireMockServer();
		defaultServer.start();
		altServer = new WireMockServer(PORT);
		altServer.start();
	}
	
	@After
	public void stopServer() {
		defaultServer.stop();
		altServer.stop();
	}

	@Test
	public void useStaticSyntaxOnAlternativeHostAndPort() throws Exception {
		WireMockTestClient defaultTestClient = new WireMockTestClient(8080);
		WireMockTestClient altTestClient = new WireMockTestClient(PORT);
		
		String thisHostName = InetAddress.getLocalHost().getHostName();
		WireMock.configureFor(thisHostName, PORT);
		
		givenThat(get(urlEqualTo("/resource/on/other/address"))
				.willReturn(aResponse()
					.withStatus(206)));
		
		assertThat(altTestClient.get("/resource/on/other/address").statusCode(), is(206));
		assertThat(defaultTestClient.get("/resource/on/other/address").statusCode(), is(404));
	}
}
