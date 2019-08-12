# Springboot 自定义starter

# 一.概念理解
1.Starters作用是帮我们引入相关jar包，可以没有任何代码.比如MyBatis在项目中都依赖什么jar包，Starter的pom文件就可以导入所有依赖.因此，可以把Starter理解为一定场景资源的管理器.<br/>
2.如何编写自动配置？将需要启动就加载的自动配置类,配置在META-INF/spring.factories.<br/>
3.自定义Starter模式,也是Spring推荐的方式：<br/>
启动器只用来做依赖导入：customize-spring-boot-autoconfigure<br/>
专门来写一个自动配置模块：customize-spring-boot-autoconfigure<br/>
启动器依赖自动配置;<br/>
其它项目中只需要引入启动器(Starter)就能正常使用<br/>

# 二.自定义starter流程
1.建自动配置工程customize-spring-boot-autoconfigure，添加Starter依赖:</br>
```
  <dependencies>
    <!-- 引入spring-boot-starter,所有starter的基本配置 -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter</artifactId>
      <version>2.1.4.RELEASE</version>
      <scope>compile</scope>
    </dependency>
  </dependencies>
```
编写配置类代码
```
@ConfigurationProperties(prefix = "customize.hello")
public class HelloProperties {

    private String prefix;
    private String sufix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSufix() {
        return sufix;
    }

    public void setSufix(String sufix) {
        this.sufix = sufix;
    }
}

```
编写场景相关业务代码
```
public class HelloService {

    HelloProperties helloProperties;

    public String sayHello(String name){
        StringBuilder temp = new StringBuilder();
        temp.append(helloProperties.getPrefix());
        temp.append(name);
        temp.append(helloProperties.getSufix());
        System.out.println(temp);
        return temp.toString();
    }

    public HelloProperties getHelloProperties() {
        return helloProperties;
    }

    public void setHelloProperties(HelloProperties helloProperties) {
        this.helloProperties = helloProperties;
    }
}
```
编写自动配置类代码
```
@Configuration
// web应用生效
@ConditionalOnWebApplication(
        type = ConditionalOnWebApplication.Type.SERVLET
)
@EnableConfigurationProperties(HelloProperties.class)
public class HelloAutoConfiguration {

    @Autowired
    private HelloProperties helloProperties;

    @Bean
    public HelloService helloService(){
        HelloService service = new HelloService();
        service.setHelloProperties(helloProperties);
        return service;
    }

}
```
按照约定添加自动配置类文件
```
// 路径：resources/META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.sky.starter.HelloAutoConfiguration
```

2.建自动配置工程customize-spring-boot-starter，添加autoconfigure工程依赖:</br>
```
  <!-- 启动器 -->
  <dependencies>
    <!-- 引入自动配置模块 -->
    <dependency>
      <groupId>com.sky.starter</groupId>
      <artifactId>customize-spring-boot-autoconfigure</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
  </dependencies>
```
3.建测试工程，引入Starter项目后，启动项目访问Controller
```
@RestController
public class DemoController {
    @Autowired
    private HelloService helloService;

    @RequestMapping("/demo/{name}")
    public Object demo(@PathVariable("name") String name){

        return helloService.sayHello(name);
    }
}
```
也可以在配置文件中配置定义的属性
```
customize:
  hello:
    prefix: "Hello , "
    sufix: " !"
```


# 三.自定义starter自动配置探究
1.Springboot 项目启动类@SpringBootApplication中的注解@EnableAutoConfiguration
```
@SpringBootApplication
public class App {
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        SpringApplication.run(App.class,args);
    }
}
```
2.进入@EnableAutoConfiguration
```
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import({AutoConfigurationImportSelector.class})
public @interface EnableAutoConfiguration {
    String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";

    Class<?>[] exclude() default {};

    String[] excludeName() default {};
}
```
3.进入AutoConfigurationImportSelector源码
```
public String[] selectImports(AnnotationMetadata annotationMetadata) {
    if (!this.isEnabled(annotationMetadata)) {
        return NO_IMPORTS;
    } else {
        AutoConfigurationMetadata autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader);
        AutoConfigurationImportSelector.AutoConfigurationEntry autoConfigurationEntry = this.getAutoConfigurationEntry(autoConfigurationMetadata, annotationMetadata);
        return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
    }
}

protected AutoConfigurationImportSelector.AutoConfigurationEntry getAutoConfigurationEntry(AutoConfigurationMetadata autoConfigurationMetadata, AnnotationMetadata annotationMetadata) {
    if (!this.isEnabled(annotationMetadata)) {
        return EMPTY_ENTRY;
    } else {
        AnnotationAttributes attributes = this.getAttributes(annotationMetadata);
        // 中调用了SpringFactoriesLoader.loadFactoryNames()来加载约定文件中配置的类，包括自定义的自动配置类
        List<String> configurations = this.getCandidateConfigurations(annotationMetadata, attributes);
        configurations = this.removeDuplicates(configurations);
        Set<String> exclusions = this.getExclusions(annotationMetadata, attributes);
        this.checkExcludedClasses(configurations, exclusions);
        configurations.removeAll(exclusions);
        configurations = this.filter(configurations, autoConfigurationMetadata);
        this.fireAutoConfigurationImportEvents(configurations, exclusions);
        return new AutoConfigurationImportSelector.AutoConfigurationEntry(configurations, exclusions);
    }
}
```
四.自定义starter自动配置总结
```
启动器用来做依赖导入，包括引入自动配置模的jar包资源<br/>
其它项目中只需要引入启动器(Starter)就能正常使用<br/>
应用项目中Springboot启动类通过自动配置注解引入自定义类，条件满足时，会加载自定义自动配置内容.
```

