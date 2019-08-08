# ServiceComb 101

Service-comb-java-chassis is SDK for ServiceComb. And it defines provider model, handler model,
and transport model.

When run the BMI sample code, what happens? I will try to lift the veil.


## @EnableServiceComb

Story starts at **EnableServiceComb** annotation, which imports **ServiceCombSpringConfiguration** into Spring Boot 
and makes ServiceComb available.

```java
@SpringBootApplication
@EnableServiceComb
public class CalculatorApplication {

  public static void main(String[] args) {
    SpringApplication.run(CalculatorApplication.class, args);
  }
}
```
What does **ServiceCombSpringConfiguration** do? 
* It imports old style xml configured beans to **ApplicationContext**, which are defined in "classpath*:META-INF/spring/*.bean.xml".
* Make ServiceComb listen to **ApplicationReadyEvent**

```java
@Configuration
@ImportResource("classpath*:META-INF/spring/*.bean.xml")
class ServiceCombSpringConfiguration {
  @Inject
  public void setCseApplicationListener(CseApplicationListener cseApplicationListener) {
    cseApplicationListener.setInitEventClass(ApplicationReadyEvent.class);
  }
}
```
In the start log there is line looks like below. And __CseApplicationListener__ is declared in such bean.xml.
And then with the JSR 330 Inject annotation, ServiceComb makes itself  relies on SpringBoot lifecycle event.
 
      Loading XML bean definitions from URL [jar:file:/.../java-chassis-core-1.2.0.jar!/META-INF/spring/cse.bean.xml

## SCBEngine

According to the name **SCBEngine**, it seems mean engine of ServiceComb. When spring boot application is ready, SCBEngine
begins the init work. 

SCBEngine holds below objects:

1. ProducerProviderManager and ConsumerProviderManager which belong to Provider model of ServiceComb 
2. MicroserviceMeta
3. TransportManager belongs to transport model.
4. SchemaListenerManager
5. Collection of BootListener implementation, which makes ServiceComb components working on event.
6. Google EventBus.

Before the application runs, what we have is: 
1. Microservice definition meta data microservice.yml
2. Annotation marked java as the implementation of the microservice, such as RestSchema.
```java
@RestSchema(schemaId = "calculatorRestEndpoint")
@RequestMapping(path = "/")
public class CalculatorRestEndpoint implements CalculatorEndpoint 
```

There is the first question, how does ServiceComb parse out the 


