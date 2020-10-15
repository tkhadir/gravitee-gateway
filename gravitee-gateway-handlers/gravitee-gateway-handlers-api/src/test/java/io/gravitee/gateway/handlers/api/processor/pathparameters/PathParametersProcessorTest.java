/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.gateway.handlers.api.processor.pathparameters;

import io.gravitee.common.util.LinkedMultiValueMap;
import io.gravitee.common.util.MultiValueMap;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.handler.Handler;
import io.gravitee.gateway.handlers.api.path.Path;
import io.gravitee.gateway.handlers.api.path.PathResolver;
import io.gravitee.gateway.handlers.api.policy.api.ApiPolicyResolver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PathParametersProcessorTest {

    private PathParametersProcessor processor;

    @Mock
    private ExecutionContext context;

    @Mock
    private Request request;

    @Mock
    private PathResolver pathResolver;

    @Mock
    private Path path;

    @Mock
    private Handler<ExecutionContext> next;

    private static final String PATH_INFO = "/store/myStore/order/190783";
    private static final String PATH = "/store/:storeId/order/:orderId";
    private static final Pattern PATTERN = Pattern.compile("/store/([a-zA-Z0-9\\-._~%!$&'()* +,;=:@/]+)/order/([a-zA-Z0-9\\-._~%!$&'()* +,;=:@/]+)");

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(context.request()).thenReturn(request);
        when(pathResolver.resolve(any())).thenReturn(path);
        when(path.getPath()).thenReturn(PATH);

        processor = new PathParametersProcessor(pathResolver);
        processor.handler(next);
    }

    @Test
    public void shouldInitPathInContext() {
        processor.handle(context);

        verify(context, times(1)).setAttribute(ApiPolicyResolver.API_RESOLVED_PATH, path);
        verify(context, times(1)).setAttribute(ExecutionContext.ATTR_RESOLVED_PATH, PATH);
        verify(context, times(1)).request();
    }

    @Test
    public void shouldDoNothingIfNoParamNamesInPath() {
        when(path.getPathParamNames()).thenReturn(Collections.emptyList());
        processor.handle(context);

        verify(context, times(1)).request(); // one time in getResolvedPath()
        verify(path, never()).getPattern();
    }

    @Test
    public void shouldAddPathParamInContext() {
        MultiValueMap<String, String> pathParams = new LinkedMultiValueMap<>();
        when(request.pathInfo()).thenReturn(PATH_INFO);
        when(request.pathParameters()).thenReturn(pathParams);

        when(path.getPathParamNames()).thenReturn(Arrays.asList("storeId", "orderId"));
        when(path.getPattern()).thenReturn(PATTERN);
        processor.handle(context);

        verify(context, times(4)).request(); // one time in getResolvedPath()
        verify(request, times(1)).pathInfo();
        verify(path, times(1)).getPattern();
        
        assertEquals(2, pathParams.size());
        assertEquals("myStore", pathParams.getFirst("storeId"));
        assertEquals("190783", pathParams.getFirst("orderId"));
    }

}
