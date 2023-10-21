# desensitization-spring-boot
将[desensitization](https://github.com/allurx/desensitization) 库集成到spring-boot中实现数据自动脱敏。
实现原理是基于spring-aop对全局方法进行拦截脱敏处理，默认会对当前spring-boot工程启动类所在的包及其子包下所有**需要**脱敏处理的方法进行拦截。
当然你也可以在spring的配置文件中通过desensitization开头的配置参数编写自己的切点表达式或者编写一个名称为**desensitizationAdvisor**的Advisor
添加到spring上下文中以便更好地控制脱敏。
# 用法
## spring-boot版本
3.1.5
## maven依赖
```xml
<dependency>
  <groupId>red.zyc.boot</groupId>
  <artifactId>desensitization-spring-boot-starter</artifactId>
  <version>2.0.0</version>
</dependency>
```
## 注意
默认情况下只会对基于Spring内置的`ResponseEntity`类型返回值的方法进行必要的脱敏处理。而通常情况下我们系统中都会自定义一个类似的响应实体
```java
@Getter
@Setter
public class CustomizedResponse<T> {

    private T data;

    private String code;

    private String message;

    public CustomizedResponse() {}

    public CustomizedResponse(T data, String code, String message) {
        this.data = data;
        this.code = code;
        this.message = message;
    }

}
```
对自定义类型进行脱敏处理时我们需要配置一个类型解析器来解析该类型
```java
@Configuration
public class DesensitizationConfig {
    
    @Bean
    public TypeParser<CustomizedResponse<Object>, AnnotatedParameterizedType> typeParser() {
        return new CustomizedResponseTypeParser();
    }
    
    public static class CustomizedResponseTypeParser implements TypeParser<CustomizedResponse<Object>, AnnotatedParameterizedType>, AopInfrastructureBean {

        private final int order = AnnotationParser.randomOrder();

        @Override
        public CustomizedResponse<Object> parse(CustomizedResponse<Object> response, AnnotatedParameterizedType annotatedParameterizedType) {
            AnnotatedType typeArgument = annotatedParameterizedType.getAnnotatedActualTypeArguments()[0];
            Object erased = AnnotationParser.parse(response.getData(), typeArgument);
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
```
该配置是用来解析CustomizedResponse类型的对象，通常情况下我们只需要对响应的实际数据(data)进行脱敏即可。
将上面的类型解析器添加到Spring上下文中之后，接下来我们只需将脱敏注解标记到需要脱敏的方法返回对象的泛型参数上就能完成CustomizedResponse类型数据的自动脱敏处理。
# 例子
## 脱敏ResponseEntity类型的数据
1. [需要脱敏的方法](https://github.com/allurx/desensitization-spring-boot/blob/master/desensitization-spring-boot-samples/desensitization-spring-boot-sample-web/src/main/java/red/zyc/desensitization/boot/sample/web/controller/ResponseEntityDesensitizationController.java)
2. [测试用例](https://github.com/allurx/desensitization-spring-boot/blob/master/desensitization-spring-boot-samples/desensitization-spring-boot-sample-web/src/test/java/red/zyc/desensitization/boot/sample/web/ResponseEntityDesensitizationTest.java)
## 脱敏CustomizedResponse类型的数据
1. [需要脱敏的方法](https://github.com/allurx/desensitization-spring-boot/blob/master/desensitization-spring-boot-samples/desensitization-spring-boot-sample-web/src/main/java/red/zyc/desensitization/boot/sample/web/controller/CustomizedResponseDesensitizationController.java)
2. [测试用例](https://github.com/allurx/desensitization-spring-boot/blob/master/desensitization-spring-boot-samples/desensitization-spring-boot-sample-web/src/test/java/red/zyc/desensitization/boot/sample/web/CustomizedResponseDesensitizationTest.java)
# License
[Apache License 2.0](https://github.com/allurx/desensitization-spring-boot/blob/master/LICENSE.txt)
