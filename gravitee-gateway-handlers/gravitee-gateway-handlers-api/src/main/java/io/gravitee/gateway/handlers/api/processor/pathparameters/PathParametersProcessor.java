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

import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.core.processor.AbstractProcessor;
import io.gravitee.gateway.handlers.api.path.Path;
import io.gravitee.gateway.handlers.api.path.PathResolver;
import io.gravitee.gateway.handlers.api.policy.api.ApiPolicyResolver;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PathParametersProcessor extends AbstractProcessor<ExecutionContext> {

    private PathResolver pathResolver;

    public PathParametersProcessor(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    @Override
    public void handle(ExecutionContext context) {
        Path path = getResolvedPath(context);
        final List<String> pathParamNames = path.getPathParamNames();
        if (pathParamNames != null && !pathParamNames.isEmpty()) {
            final String pathInfo = context.request().pathInfo();
            final Pattern pattern = path.getPattern();
            final Matcher matcher = pattern.matcher(pathInfo);
            if (matcher.matches()) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    // group[0] stands for the whole string. Captured groups starts at index 1.
                    context.request().pathParameters().add(pathParamNames.get(i), matcher.group(i + 1));
                }
            }
        }
        next.handle(context);
    }

    private Path getResolvedPath(ExecutionContext context) {
        // Resolve the "configured" path according to the inbound request
        Path path = pathResolver.resolve(context.request());

        context.setAttribute(ApiPolicyResolver.API_RESOLVED_PATH, path);

        // TODO: deprecated ?
        // Not sure this is used by someone during policies processing
        // Perhaps it may be removed in the future
        context.setAttribute(ExecutionContext.ATTR_RESOLVED_PATH, path.getPath());
        return path;
    }
}
