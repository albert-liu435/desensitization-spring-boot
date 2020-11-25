/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package red.zyc.desensitization.boot.sample.web.config;

import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import red.zyc.desensitization.boot.autoconfigure.ResponseEntityTypeResolver;
import red.zyc.desensitization.boot.sample.web.model.CustomizedResponse;
import red.zyc.desensitization.resolver.TypeResolver;
import red.zyc.desensitization.resolver.TypeResolvers;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;

/**
 * 脱敏配置
 *
 * @author zyc
 */
@Configuration
public class DesensitizationConfig {

    /**
     * 将{@link CustomizedResponseTypeResolver}注册到spring中
     *
     * @return {@link CustomizedResponseTypeResolver}
     */
    @Bean
    public TypeResolver<CustomizedResponse<Object>, AnnotatedParameterizedType> typeResolver() {
        return new CustomizedResponseTypeResolver();
    }

    /**
     * 自定义脱敏解析器用来解析{@link CustomizedResponse}类型的数据
     *
     * @see ResponseEntityTypeResolver
     */
    public static class CustomizedResponseTypeResolver implements TypeResolver<CustomizedResponse<Object>, AnnotatedParameterizedType>, AopInfrastructureBean {

        private final int order = TypeResolvers.randomOrder();

        @Override
        public CustomizedResponse<Object> resolve(CustomizedResponse<Object> response, AnnotatedParameterizedType annotatedParameterizedType) {
            AnnotatedType typeArgument = annotatedParameterizedType.getAnnotatedActualTypeArguments()[0];
            Object erased = TypeResolvers.resolve(response.getData(), typeArgument);
            return new CustomizedResponse<>(erased, response.getMessage(), response.getCode());
        }

        @Override
        public boolean support(Object value, AnnotatedType annotatedType) {
            return value instanceof CustomizedResponse && annotatedType instanceof AnnotatedParameterizedType;
        }

        @Override
        public int order() {
            return order;
        }
    }
}
