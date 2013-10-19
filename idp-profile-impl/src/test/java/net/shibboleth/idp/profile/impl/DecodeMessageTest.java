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

package net.shibboleth.idp.profile.impl;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.AbstractMessageDecoder;
import org.opensaml.messaging.decoder.MessageDecoder;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Unit test for {@link DecodeMessage}. */
public class DecodeMessageTest {
    
    private MockMessage message; 
    
    private MockMessageDecoder decoder;
    
    private ProfileRequestContext profileCtx;
    
    @BeforeMethod
    public void setUp() throws ComponentInitializationException {
        message = new MockMessage();
        
        decoder = new MockMessageDecoder(message);
        decoder.initialize();
        
        profileCtx = new ProfileRequestContext();
    }

    /** Test that the action proceeds properly if the message can be decoded. */
    @Test public void testDecodeMessage() throws Exception {
        DecodeMessage action = new DecodeMessage(decoder);
        action.setId("test");
        action.initialize();

        Event result = action.doExecute(new MockRequestContext(), profileCtx);

        ActionTestingSupport.assertProceedEvent(result);

        Assert.assertNotNull(profileCtx.getInboundMessageContext());
        Assert.assertEquals(profileCtx.getInboundMessageContext().getMessage(), message);
    }

    /** Test that the action errors out properly if the message can not be decoded. */
    @Test public void testThrowException() throws Exception {
        decoder.setThrowException(true);

        DecodeMessage action = new DecodeMessage(decoder);
        action.setId("test");
        action.initialize();

        Event result = action.doExecute(new MockRequestContext(), profileCtx);

        ActionTestingSupport.assertEvent(result, DecodeMessage.UNABLE_TO_DECODE);
    }

    /**
     * Mock implementation of {@link MessageDecoder } which either returns a  
     * {@link MessageContext} with a mock message or throws a {@link MessageDecodingException}.
     */
    class MockMessageDecoder extends AbstractMessageDecoder<MockMessage> {

        /** Whether a {@link MessageDecodingException} should be thrown by {@link #doDecode()}. */
        private boolean throwException = false;
        
        /** The mock message to produce (optional). */
        private MockMessage message;

        /**
         * Constructor.
         *
         */
        public MockMessageDecoder(MockMessage mockMessage) {
            super();
            message = mockMessage;
        }

        /**
         * Sets whether a {@link MessageDecodingException} should be thrown by {@link #doDecode()}.
         * 
         * @param shouldThrowDecodeException true if an exception should be thrown, false if not
         */
        public void setThrowException(final boolean shouldThrowDecodeException) {
            throwException = shouldThrowDecodeException;
        }

        /** {@inheritDoc} */
        protected void doDecode() throws MessageDecodingException {
            if (throwException) {
                throw new MessageDecodingException();
            } else {
                MessageContext<MockMessage> messageContext = new MessageContext<>();
                if (message != null) {
                    messageContext.setMessage(message);
                } else {
                    messageContext.setMessage(new MockMessage());
                }
                setMessageContext(messageContext);
            }
        }

    }
}
