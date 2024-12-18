# winter-framework

A personal web MVC framework built on top of the Java servlet API

## I - Installation

1 - Download the **winter-framework.jar** file and add it to the project libraries

2 - Map all url("/") to the **mg.tiarintsoa.controller.FrontController** class in the **web.xml** file.

3 - Don't forget to set an init parameter to precise which package of your project should be scanned by the winter-framework 
for **Controllers**

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
            <param-value>com.example.controller</param-value>
        </init-param>
    </servlet>
    
    <!-- Servlet mapping -->
    <servlet-mapping>
        <servlet-name>FrontController</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>

</web-app>
```

In the example above, the controllers_package init parameter is set to mg.example.controller,
which is the package that the winter-framework will scan for Controllers.
You should replace this value with the package of your project that contains your Controllers.

## II - Usage

### 1) Controller and endpoints

#### a) Controller and endpoint creation

Make a class a **controller** by placing it in the **controllers' package** you specified above.
Then annotate the class with the **@Controller** annotation.

Create an endpoint by annotating a controller's method with the **@UrlMapping** annotation.
The value of the annotation will be the URL mapped to it.
By default, the verb of the endpoint will be the GET verb, but you can specify it with the @Get or @Post annotation. 
Each endpoint must return a String representing the response's body or a ModelView.
The URL in the ModelView represents the URL to the view and the data it contains
will be mapped into an HashMap of the attributes which will be bind to the request.

Here is an example:

```java
import mg.tiarintsoa.annotation.Controller;
import mg.tiarintsoa.annotation.Post;
import mg.tiarintsoa.annotation.UrlMapping;
import mg.tiarintsoa.controller.ModelView;

@Controller
public class TestController {

    @UrlMapping("/")
    public ModelView message() {
        ModelView mw = new ModelView("message.jsp");
        mw.addObject("message", "Hello world !");
        return mw;
    }

    @UrlMapping("/end-point-2")
    @Post
    public String endPoint2() {
        return "End point 2";
    }

    public void notAnEndPoint() {
    }

}
```

**Warning:**
- Don't assign a URL and verb pair to more than one method.
- Controller's method should only return a String or a ModelView 

#### b) Parameters binding

You can bind the parameters of a request to the parameters of an endpoint using the **@RequestParameter** annotation.
Its value will be the name of the request parameter that will be bind to it.
Here is an example:

**URL**: "/employee?firstname=John&lastname=Doe"

**Controller:**

```java
import mg.tiarintsoa.annotation.Controller;
import mg.tiarintsoa.annotation.UrlMapping;
import mg.tiarintsoa.annotation.RequestParameter;
import mg.tiarintsoa.controller.ModelView;

@Controller
public class TestController {

    @UrlMapping("/employee")
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
import mg.tiarintsoa.annotation.RequestParameter;

public class Employee {
    @RequestParameter("firstname")
    private String firstname;
    
    @RequestParameter("lastname")
    private String lastname;

    public Employee() {}

    // ...
}
```

**Controller:**
```java
import mg.tiarintsoa.annotation.Controller;
import mg.tiarintsoa.annotation.UrlMapping;
import mg.tiarintsoa.annotation.RequestParameter;
import mg.tiarintsoa.controller.ModelView;
import mg.winter.entity.Employee;

@Controller
public class TestController {

    @UrlMapping("/employee")
    public ModelView employee(@RequestParameter("employee") Employee emp) {
        ModelView modelView = new ModelView("employee.jsp");
        modelView.addObject("emp", emp);
        return modelView;
    }

}
```

**NB**:
- Parameter binding only supports String, int/Integer, double/Double or Object having fields of the precedent type.
- If no binding can be applied then the parameter will be set to null or its primitive type's default value.
- Objects' class must contain an **empty constructor**

#### c) Verb

The winter-framework only supports GET and POST verbs for now.
You can specify it using the @Get or @Post annotation.
If no verb is specified, the GET verb will be applied by default.

```java
import mg.tiarintsoa.annotation.*;
import mg.tiarintsoa.controller.ModelView;
import mg.tiarintsoa.enumeration.RequestVerb;
import mg.winter.entity.Employee;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TestRestController {

    @UrlMapping("/employees")
    public ModelView empList() {
        ModelView modelView = new ModelView("employees.jsp");
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("Tiarintsoa", "Mbolatsiory"));
        employees.add(new Employee("Henintsoa", "Paul"));
        employees.add(new Employee("Ryan", "Lizka"));
        modelView.addObject("message", "Here is the employee list");
        modelView.addObject("employees", employees);
        return modelView;
    }

    @Get
    @UrlMapping(value = "/employees/1")
    public Employee empDetails() {
        return new Employee("Tiarintsoa", "Mbolatsiory");
    }

    @Post
    @UrlMapping(value = "/employees/1")
    public Employee empDetailsPost() {
        return new Employee("Tiarintsoa", "Mbolatsiory");
    }

}
```

### 2) REST API

If you want to make a REST controller, annotate the class with the @RestController annotation.
The return value of all its method will be sent as a JSON response.
If the return value is an instance of **ModelView** then the view will be ignored and
its **data** attribute will be the response body instead.

```java
import mg.tiarintsoa.annotation.Controller;
import mg.tiarintsoa.annotation.UrlMapping;
import mg.tiarintsoa.annotation.RestController;
import mg.tiarintsoa.controller.ModelView;
import mg.winter.entity.Employee;
import java.util.ArrayList;
import java.util.List;

@Controller
@RestController
public class TestRestController {

    @UrlMapping("/api/employees")
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

    @UrlMapping("/api/employees/1")
    public Employee empDetails() {
        return new Employee("Tiarintsoa", "Mbolatsiory");
    }

}
```

You can also annotate a single end point in a controller

```java

import mg.tiarintsoa.annotation.UrlMapping;
import mg.tiarintsoa.annotation.RestEndPoint;
import mg.tiarintsoa.controller.ModelView;
import mg.winter.entity.Employee;

public class TestController {

    @UrlMapping("/")
    public ModelView message() {
        ModelView modelView = new ModelView("message.jsp");
        modelView.addObject("message", "Hello world !");
        return modelView;
    }

    @RestEndPoint
    @UrlMapping("/api/employees/2")
    public Employee empDetails() {
        return new Employee("Kevin", "Ramaro");
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

Here is a use case example:

```java
import mg.tiarintsoa.annotation.Controller;
import mg.tiarintsoa.annotation.Post;
import mg.tiarintsoa.annotation.UrlMapping;
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
    @UrlMapping("/login")
    @Post
    public ModelView login(@RequestParameter("email") String email, @RequestParameter("password") String password) {
        session.add("email", email);
        session.add("password", password);

        return new ModelView("home.jsp");
    }

    @UrlMapping("/my-info")
    public ModelView myInfo() {
        ModelView modelView = new ModelView("my-info.jsp");
        modelView.addObject("email", session.get("email"));
        modelView.addObject("password", session.get("password"));
        return modelView;
    }

}
```

### 4) Parameters validation

You can validate the parameters and their fields using the following annotations:
- @Required( message )
- @NotBlank( message )
- @Number( message )
- @Range( message, min, max ): specifies a range for a string's length or a number's value

Note that you don't have to specify the message values.

You have to specify with the @ErrorUrl annotation where the user should be redirected in case a validation error occurs

**Entity:**
```java
import mg.tiarintsoa.annotation.RequestParameter;
import mg.tiarintsoa.validation.annotation.NotBlank;
import mg.tiarintsoa.validation.annotation.Required;

public class Employee {
    @RequestParameter("firstname")
    @Required( message = "Le prénom est requis")
    private String firstname;

    @RequestParameter("lastname")
    @NotBlank( message = "Le nom de famille ne doit pas être vide")
    private String lastname;

    public Employee() {}

    // ...
}
```

**Controller:**
```java
package mg.winter.controller;

import mg.tiarintsoa.annotation.*;
import mg.tiarintsoa.controller.ModelView;
import mg.tiarintsoa.session.WinterSession;
import mg.tiarintsoa.validation.annotation.Number;
import mg.tiarintsoa.validation.annotation.Range;
import mg.tiarintsoa.validation.annotation.Required;
import mg.winter.entity.Employee;

@Controller
public class TestController {

    @UrlMapping("/form")
    public ModelView form() {
        return new ModelView("form.jsp");
    }

    @Get
    @UrlMapping("/employee")
    @ErrorUrl("/form")
    public ModelView employeeGet(
            @RequestParameter("employee") @Required Employee emp,
            @RequestParameter("age") @Number @Range(min = 18, max = 120) int age
    ) {
        ModelView modelView = new ModelView("employee.jsp");
        modelView.addObject("emp", emp);
        modelView.addObject("age", age);

        return modelView;
    }

}
```
