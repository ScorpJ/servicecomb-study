# ServiceComb 101

Service-comb-java-chassis is SDK for ServiceComb. And it defines provider model, handler model,
and transport model.

When start ServiceComb application what happens? How ServiceComb register service into Service Center?


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
* It imports xml style bean definitions into **ApplicationContext** from "classpath*:META-INF/spring/*.bean.xml".
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
And in the starting log there are lines look like below. There **ConfigurationSpringInitializer** in *foundation-config-1.2.0.jar!/META-INF/spring/cse.bean.xml*
and __CseApplicationListener__ in *java-chassis-core-1.2.0.jar!/META-INF/spring/cse.bean.xml* are special at this point.

 
      Loading XML bean definitions from URL [jar:file:.../handler-loadbalance-1.2.0.jar!/META-INF/spring/cse.bean.xml]
      Loading XML bean definitions from URL [jar:file:.../foundation-vertx-1.2.0.jar!/META-INF/spring/cse.bean.xml]
      Loading XML bean definitions from URL [jar:file:.../java-chassis-core-1.2.0.jar!/META-INF/spring/cse.bean.xml]
      Loading XML bean definitions from URL [jar:file:.../foundation-config-1.2.0.jar!/META-INF/spring/cse.bean.xml]
      Loading XML bean definitions from URL [jar:file:...handler-bizkeeper-1.2.0.jar!/META-INF/spring/cse.bean.xml]

## ConfigurationSpringInitializer
**ConfigurationSpringInitializer** is an EnvironmentAware bean. And when the setEnvironment method is invoked, ServiceComb ConfigUtil works there to create a local config.

ClassLoader will load all *microservice.yaml* files and the **MicroserviceConfigLoader** will hold all the microservice meta data.
User can also provide comma separated system property *servicecomb.configurationSource.additionalUrls* to config the customized meta data url.

ServiceComb uses a SPI mechanism for **ConfigCenterConfigurationSource** to init config center if a provider exists.
However the microservice meta data is my focus here.  


## Service Producer - how ServiceComb know your server declaration
ServiceComb supports RPC, JAX-RS and Spring MVC style microservice declaration. And there are **PojoProducers** and **RestProducers** to parser the meta data.

Both **RestProducers** and **PojoProducers** are spring BeanPostProcessor beans, and they parser **RestSchema** and **RestController** or **RpcSchema**  respectively at phrase postProcessAfterInitialization. 
A **ProducerMeta** is generated and hold by the producer bean itself or its delegate.  


```java
@RestSchema(schemaId = "calculatorRestEndpoint")
@RequestMapping(path = "/")
public class CalculatorRestEndpoint implements CalculatorEndpoint{
    ...
} 
```

## SCBEngine

Depends on how user starts ServiceComb, SCBEngine will be initiated on ContextRefreshedEvent (Spring application) or ApplicationReadyEvent (Spring Boot application).

SCBEngine begins the initiate handler model, provider model (both producer and consumer), and transport model respectively. And then
Start the schedule task to communicate with Service Center.

**ProducerProviderManager** is used to produce SchemaMeta for each service. 
ServiceName in *microservice.yaml* and SchemaId in **RestSchema** or **RpcSchema** passed to instance of 
**ProducerSchemaFactory**. ProducerSchemaFactory reflects the service implementation bean and uses SwaggerGenerator to generate the service contract.
And then all these information is hold by the SchemaMeta, which will be cached and sent to Service Center.

At last, a instance of MicroserviceServiceCenterTask is scheduled at a fixed rate to do the MicroserviceRegisterTask, MicroserviceInstanceRegisterTask, MicroserviceWatchTask and MicroserviceInstanceHeartbeatTask.
 
Below code snippet is an example of such a contract.  

```
swagger: "2.0"
info:
  version: "1.0.0"
  title: "swagger definition for org.apache.servicecomb.samples.bmi.CalculatorRestEndpoint"
  x-java-interface: "cse.gen.bmi.calculator.calculatorRestEndpoint.CalculatorRestEndpointIntf"
basePath: "/"
consumes:
- "application/json"
produces:
- "application/json"
paths:
  /bmi:
    get:
      operationId: "calculate"
      parameters:
      - name: "height"
        in: "query"
        required: false
        type: "number"
        format: "double"
      - name: "weight"
        in: "query"
        required: false
        type: "number"
        format: "double"
      responses:
        200:
          description: "response of 200"
          schema:
            $ref: "#/definitions/BMIViewObject"
definitions:
  BMIViewObject:
    type: "object"
    properties:
      result:
        type: "number"
        format: "double"
      instanceId:
        type: "string"
      callTime:
        type: "string"
    x-java-class: "org.apache.servicecomb.samples.bmi.BMIViewObject
```


