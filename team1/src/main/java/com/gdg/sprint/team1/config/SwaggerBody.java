package com.gdg.sprint.team1.config;

import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Swagger용 커스텀 RequestBody 어노테이션
 *
 * 생성 이유:
 * - Swagger의 @RequestBody와 Spring의 @RequestBody 이름 충돌 방지
 * - multipart/form-data에서 JSON 파트의 Content-Type을 명시하기 위해
 *
 * 사용법:
 * @SwaggerBody(content = @Content(
 *     encoding = @Encoding(name = "request", contentType = "application/json")
 * ))
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@RequestBody
public @interface SwaggerBody {

    @AliasFor(annotation = RequestBody.class)
    String description() default "";

    @AliasFor(annotation = RequestBody.class)
    Content[] content() default {};

    @AliasFor(annotation = RequestBody.class)
    boolean required() default false;

    @AliasFor(annotation = RequestBody.class)
    Extension[] extensions() default {};

    @AliasFor(annotation = RequestBody.class)
    String ref() default "";
}