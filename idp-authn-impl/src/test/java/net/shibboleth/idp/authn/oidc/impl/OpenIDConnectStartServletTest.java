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

package net.shibboleth.idp.authn.oidc.impl;

import java.io.IOException;
import java.net.URI;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mockito.Mockito;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import net.shibboleth.idp.authn.AuthenticationFlowDescriptor;
import net.shibboleth.idp.authn.ExternalAuthentication;
import net.shibboleth.idp.authn.ExternalAuthenticationException;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.impl.ExternalAuthenticationImpl;
import net.shibboleth.idp.authn.oidc.context.OpenIDConnectContext;

/**
 * Unit tests for {@link OpenIDConnectStartServlet}.
 */
public class OpenIDConnectStartServletTest {

    /** The servlet to be tested. */
    OpenIDConnectStartServlet servlet;

    /** The conversation key. */
    String conversationKey;

    /**
     * Init tests.
     * 
     * @throws Exception
     */
    @BeforeTest
    public void initTests() throws Exception {
        servlet = new OpenIDConnectStartServlet();
        MockServletConfig mockConfig = new MockServletConfig();
        servlet.init(mockConfig);
        conversationKey = "mockKey";
    }

    /**
     * Run servlet without HttpSession set.
     * 
     * @throws Exception
     */
    @Test
    public void testNoHttpSession() throws Exception {
        HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(httpRequest.getSession()).thenReturn(null);
        Assert.assertTrue(runService(servlet, httpRequest, new MockHttpServletResponse()));
    }

    /**
     * Run servlet without conversation key set.
     * 
     * @throws Exception
     */
    @Test
    public void testNoConversationKey() throws Exception {
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        Assert.assertTrue(runService(servlet, httpRequest, new MockHttpServletResponse()));
    }

    /**
     * Run servlet without conversation existing in the session.
     * 
     * @throws Exception
     */
    @Test
    public void testNoConversationInSession() throws Exception {
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setParameter(ExternalAuthentication.CONVERSATION_KEY, conversationKey);
        Assert.assertTrue(runService(servlet, httpRequest, new MockHttpServletResponse()));
    }

    /**
     * Run servlet without {@link ProfileRequestContext}.
     * 
     * @throws Exception
     */
    @Test
    public void testNoProfileRequestContext() throws Exception {
        final MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setParameter(ExternalAuthentication.CONVERSATION_KEY, conversationKey);
        httpRequest.getSession().setAttribute(ExternalAuthentication.CONVERSATION_KEY + conversationKey,
                new MockExternalAuthentication());
        Assert.assertTrue(runService(servlet, httpRequest, new MockHttpServletResponse()));
    }

    /**
     * Run servlet without {@link AuthenticationContext}.
     * 
     * @throws Exception
     */
    @Test
    public void testNoAuthenticationContext() throws Exception {
        final MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setParameter(ExternalAuthentication.CONVERSATION_KEY, conversationKey);
        final ProfileRequestContext<?, ?> ctx = new ProfileRequestContext<>();
        httpRequest.setAttribute(ProfileRequestContext.BINDING_KEY, ctx);
        httpRequest.getSession().setAttribute(ExternalAuthentication.CONVERSATION_KEY + conversationKey,
                new MockExternalAuthentication());
        Assert.assertTrue(runService(servlet, httpRequest, new MockHttpServletResponse()));
    }

    /**
     * Run servlet without {@link OpenIDConnectContext}.
     * 
     * @throws Exception
     */
    @Test
    public void testNoUserContext() throws Exception {
        final MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setParameter(ExternalAuthentication.CONVERSATION_KEY, conversationKey);
        final ProfileRequestContext<?, ?> ctx = new ProfileRequestContext<>();
        httpRequest.getSession().setAttribute(ExternalAuthentication.CONVERSATION_KEY + conversationKey,
                new ExternalAuthenticationImpl(ctx));
        final AuthenticationContext authnCtx = ctx.getSubcontext(AuthenticationContext.class, true);
        final AuthenticationFlowDescriptor flow = new AuthenticationFlowDescriptor();
        flow.setId("mock");
        authnCtx.setAttemptedFlow(flow);
        Assert.assertTrue(runService(servlet, httpRequest, new MockHttpServletResponse()));
    }

    /**
     * Run servlet with prerequisities met.
     * 
     * @throws Exception
     */
    @Test
    public void testSuccess() throws Exception {
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setParameter(ExternalAuthentication.CONVERSATION_KEY, conversationKey);
        final ProfileRequestContext<?, ?> ctx = new ProfileRequestContext<>();
        final AuthenticationContext authnCtx = ctx.getSubcontext(AuthenticationContext.class, true);
        final AuthenticationFlowDescriptor flow = new AuthenticationFlowDescriptor();
        flow.setId("mock");
        authnCtx.setAttemptedFlow(flow);
        final OpenIDConnectContext suOidcCtx =
                authnCtx.getSubcontext(OpenIDConnectContext.class, true);
        final String redirectUri = "https://mock.example.org";
        suOidcCtx.setAuthenticationRequestURI(new URI(redirectUri));
        httpRequest.getSession().setAttribute(ExternalAuthentication.CONVERSATION_KEY + conversationKey,
                new ExternalAuthenticationImpl(ctx));
        final MockHttpServletResponse httpResponse = new MockHttpServletResponse();
        Assert.assertFalse(runService(servlet, httpRequest, httpResponse));
        Assert.assertEquals(httpResponse.getRedirectedUrl(), redirectUri);
        Assert.assertNotNull(
                httpRequest.getSession().getAttribute(OpenIDConnectStartServlet.SESSION_ATTR_SUCTX));
        Assert.assertTrue(httpRequest.getSession().getAttribute(
                OpenIDConnectStartServlet.SESSION_ATTR_SUCTX) instanceof OpenIDConnectContext);
    }

    /**
     * Runs the given servlet with given request and response objects.
     * 
     * @param httpServlet
     * @param httpRequest
     * @param httpResponse
     * @return True if {@link ServletException} is thrown, false otherwise.
     * @throws IOException
     */
    protected static boolean runService(HttpServlet httpServlet, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws IOException {
        boolean catched = false;
        try {
            httpServlet.service(httpRequest, httpResponse);
        } catch (ServletException e) {
            catched = true;
        }
        return catched;
    }

    /**
     * Mock class extending {@link ExternalAuthentication}.
     */
    class MockExternalAuthentication extends ExternalAuthentication {

        /** {@inheritDoc} */
        @Override
        protected void doStart(@Nonnull final HttpServletRequest request) throws ExternalAuthenticationException {
            // no op
        }
    }
}
