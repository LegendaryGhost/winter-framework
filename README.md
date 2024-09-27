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

In the example above, the controllers_package init parameter is set to mg.winter.controller,
which is the package that the winter-framework will scan for Controllers.
You should replace this value with the package of your project that contains your Controllers.

## II - Usage

### 1) Controller and endpoint

#### a) Controller and endpoint creation

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
import mg.tiarintsoa.controller.ModelView;

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

#### b) Parameters binding

You can bind the parameters of a request to the parameters of an endpoint using the **@RequestParameter** attribute.
Its value will be the name of the request parameter that will be bind to it.
Here is an example:

**URL**: "/employee?firstname=John&lastname=Doe"

**Controller:**
```java
import mg.tiarintsoa.annotation.Controller;
import mg.tiarintsoa.annotation.GetMapping;
import mg.tiarintsoa.annotation.RequestParameter;
import mg.tiarintsoa.controller.ModelView;

@Controller
public class TestController {

    @GetMapping("/employee")
    public ModelView employee(@RequestParameter("firstname") String firstname, @RequestParameter("lastname") String lastname) {
        ModelView modelView = new ModelView("employee.jsp");
        modelView.addObject("firstname", firstname);
        modelView.addObject("lastname", lastname);
        return modelView;
    }

}
```

Request parameters binding also works with objects.
The objects' field value will be set based on their name and the field name following this pattern : **"\<parameterName>.\<fieldName>"**. 
Here is an example for more clarity:

**URL**: "/employee?employee.firstname=John&employee.lastname=Doe"

**Entity:**
```java
import mg.tiarintsoa.annotation.RequestSubParameter;

public class Employee {
    @RequestSubParameter("firstname")
    private String firstname;
    
    @RequestSubParameter("lastname")
    private String lastname;

    public Employee() {}

    // ...
}
```

**Controller:**
```java
import mg.tiarintsoa.annotation.Controller;
import mg.tiarintsoa.annotation.GetMapping;
import mg.tiarintsoa.annotation.RequestParameter;
import mg.tiarintsoa.controller.ModelView;
import mg.winter.entity.Employee;

@Controller
public class TestController {

    @GetMapping("/employee")
    public ModelView employee(@RequestParameter("employee") Employee emp) {
        ModelView modelView = new ModelView("employee.jsp");
        modelView.addObject("emp", emp);
        return modelView;
    }

}
```

**NB**:
- Parameter binding only supports String or Object having String fields.
- If no binding can be applied then the parameter will be set to null.
- Objects' class must contain an **empty constructor**

### REST API

If you want to make a REST controller, annotate the class with the @RestAPI annotation.
The return value of all its method will be sent as a JSON response.
If the return value is an instance of **ModelView** then the view will be ignored and
its **data** attribute will be the response body instead.

```java
import mg.tiarintsoa.annotation.Controller;
import mg.tiarintsoa.annotation.GetMapping;
import mg.tiarintsoa.annotation.RestAPI;
import mg.tiarintsoa.controller.ModelView;
import mg.winter.entity.Employee;
import java.util.ArrayList;
import java.util.List;

@Controller
@RestAPI
public class TestRestController {

    @GetMapping("/json/emp")
    public ModelView empList() {
        ModelView modelView = new ModelView();
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("Tiarintsoa", "Mbolatsiory"));
        employees.add(new Employee("John", "Doe"));
        employees.add(new Employee("Jeanne", "Doe"));
        modelView.addObject("message", "Here is the employee list");
        modelView.addObject("employees", employees);
        return modelView;
    }

    @GetMapping("/json/emp/1")
    public Employee empDetails() {
        return new Employee("Tiarintsoa", "Mbolatsiory");
    }

}
```

### 3) Session

To use the session, you can add a field of type **WinterSession** in your controller.
It will be automatically detected and injected by the winter framework.
The **WinterSession** class contains 3 generic method:

- void add(String key, Object value)
- Object get(String key)
- void delete(String key)

Its use case will look like this:

```java
import mg.tiarintsoa.annotation.Controller;
import mg.tiarintsoa.annotation.GetMapping;
import mg.tiarintsoa.annotation.RequestParameter;
import mg.tiarintsoa.controller.ModelView;
import mg.tiarintsoa.session.WinterSession;

@Controller
public class LoginController {

    private WinterSession session;

    /**
     * Handles the login form submission
     * @param email the email
     * @param password the password
     * @return the view
     */
    @GetMapping("/login")
    public ModelView login(@RequestParameter("email") String email, @RequestParameter("password") String password) {
        session.add("email", email);
        session.add("password", password);

        return new ModelView("home.jsp");
    }

    @GetMapping("/my-info")
    public ModelView myInfo() {
        ModelView modelView = new ModelView("my-info.jsp");
        modelView.addObject("email", session.get("email"));
        modelView.addObject("password", session.get("password"));
        return modelView;
    }

}
```
