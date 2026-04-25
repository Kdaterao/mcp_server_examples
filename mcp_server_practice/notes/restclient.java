/*
   -  WeatherService.java has a class that is an HTTP client built in a "restful" way to interact with the weather.gov API --> thus is called a rest client

   -  The way its structured in java is that we utilize a class structure to define the client and its methods

   - creating client structure in java involves defining a class with a constructor that initializes the client with a base URL and default headers, 
     and methods that correspond to specific API endpoints.

   - methods are /GET,  /POST, /PUT, /DELETE  
   *-----------structure of the rest client in java --------------*
   className {
   
    constructor()
    {
        *code which initializes the rest client goes here*
        *we define the base url and any default headers here*
        *headers are values that tell the api: who we are, how long to maintain the connection(to prevent infinite hanging), what type of data we accept etc)
        *api's usually require certain headers to be set to process the request correctly*

        *format for headers is usually ("header_name", "header_value")*
    }

    method1(params) {
        *code which uses the rest client to call a specific endpoint goes here*
        *methods are /GET,  /POST, /PUT, /DELETE  depending on what action we want to perform on the api*

   }

   (...other methods)

   }
    
    *--------------------------------------------------------------*

    - Right now we are using Springboot's RestClient to build the rest client 
      which is an abstraction over the low level HTTP calls, but the structure 
      remains the same regardless of the library used to build the rest client(we just specified the structure above)

*/




