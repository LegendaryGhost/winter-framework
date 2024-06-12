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
- [ ] Bind the request's parameters using an annotation 
