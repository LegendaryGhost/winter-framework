# A file designed to track the development of the winter framework.

## Sprint 0 (FrontController)

- [x] FrontController servlet
  - [x] processRequest method
- [x] Jar creation script

## Sprint 1 (List of controllers)

- [x] Controller annotation
- [x] Scan the package specified by the user to get the list of controllers
- [x] Display the list of controller for all url
- [x] Tell the user how to specify the package to scan in web xml

## Sprint 2 (URL mapping to each server)

- [x] Get the relative URL (the URL part after the base URL)
- [x] GetMapping annotation
- [x] URL mapping to the controllers' methods
- [x] URL not found exception
- [x] Avoid double url mapping (1 url mapped to multiple controllers' method)

## Sprint 3 (Display of the controller's method return value)
- [x] Controller instantiation (Singleton pattern)
- [x] Method invocation

## Sprint 4 (ModelView)
- [x] ModelView class
  - [x] String url
  - [x] HashMap<String, Object> data
- [x] Bind attributes to the request
- [x] Forward the request to the view

## Sprint 5 (Error handling)
- [x] Not existing controller package
- [x] Empty controller package
- [x] Similar URL mapping
- [x] URL not found
- [x] Unsupported controller's method return type

## Sprint 6 (Request parameter binding)
- [x] Get the list of parameters for each mapping's method
- [x] Get the names of the parameters
- [x] Bind the method's parameters with the request's parameters by convention (bind them by their name)
- [x] Bind the request's parameters using an annotation 

## Sprint 7 (Request parameter binding for objects)
- [x] Look for the object parameter's field name
- [x] Get their value from the request
- [x] Create an annotation for the object parameter's fields
### Sprint 7 bis
- [x] Throw an error when the parameter of the endpoint isn't annotated

## Sprint 8 (Custom session)
- [x] Create the session class
- [x] Inject the instance of the session into the controllers' instance

## Sprint 9 (API)
- [x] Create the @RestController class annotation
- [x] Create the @RestEndPoint method annotation
- [x] In the FrontServlet, check if the controllers or methods are annotated
  - [x] While invoking the controller's method, return a JSON response if it is the case
    - [x] Serialize the data attribute if the Object is a ModelView instance
      - [x] Otherwise, serialize it immediately
