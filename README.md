# winter-framework

A personnal web MVC framework built with Java

## I - Installation

1 - Download the **winter-framework.jar** file and add it to the project libraries

2 - Map all url("/") to the **mg.tiarintsoa.controller.FrontController** class in the **web.xml** file.

3 - Don't forget to set an init parameter to precise which package of your project should the winter-framework
scan for **Controllers**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
                             http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
    
    <!-- Servlet naming -->
    <servlet>
        <servlet-name>FrontController</servlet-name>
        <servlet-class>mg.tiarintsoa.controller.FrontController</servlet-class>
        <init-param>
            <param-name>controllers_package</param-name>
            <param-value>mg.winter.controller</param-value>
        </init-param>
    </servlet>
    
    <!-- Servlet mapping -->
    <servlet-mapping>
        <servlet-name>FrontController</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>

</web-app>
```

In the example above, the controllers_package init parameter is set to mg.winter.controller, which is the package that the winter-framework will scan for Controllers. You should replace this value with the package of your project that contains your Controllers.

## II - Usage

### 1) Controller and endpoint

Make a class a **controller** by placing it in the **controllers' package** you specified above.
Then annotate the class with the **@Controller** annotation.

Create a GET endpoint by annotating a controller's method with the **@GetMapping** annotation.
The value of the annotation will be the URL mapped to it.
Each endpoint must return a String representing the response's body
or a ModelView. The url in the ModelView represents the URL to the view and the data
will be a HashMap of the attributes which will be bind to the request.

Here is an example:

```java
import mg.tiarintsoa.annotation.Controller;
import mg.tiarintsoa.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping("/")
    public ModelView message() {
        ModelView mw = new ModelView("message.jsp");
        mw.addObject("message", "Hello world !");
        return mw;
    }

    @GetMapping("/end-point-2")
    public String endPoint2() {
        return "End point 2";
    }

    public void notAnEndPoint() {}

}
```

**Warning:**
- Don't assign a single URL to more than one method.
- Controller's method should only return a String or a ModelView 