/*
   -  This class is an HTTP client built in a "restful" way to interact with the weather.gov API --> thus is called a rest client

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

    - Right now we are using Springboot's RestClient to build the rest client which is an abstraction over the low level HTTP calls, but 
        the structure remains the same regardless of the library used to build the rest client(we just specified the structure above)

*/





package com.practice.mcp_server; //package statement adds this class to the namespace com.practice.mcp_server (all java files work like this )

import java.util.List; //import statements will add all necessary classes/namespaces that we wanna use in this file ( all java files work like this )
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Service // @Service annotation marks this class as a Spring service component

public class WeatherService {

	private final RestClient restClient; // declares a private final variable restClient of type RestClient


    // Constructor to initialize the RestClient with base URL and default headers
	public WeatherService() {
		this.restClient = RestClient.builder()
			.baseUrl("https://api.weather.gov") // sets the base URL for the RestClient
			.defaultHeader("Accept", "application/geo+json") // sets the default Accept header to specify the expected response format
			.defaultHeader("User-Agent", "WeatherApiClient/1.0 (your@email.com)") // sets the User-Agent header to identify the client application
			.build(); // spring RestClient uses .builder() pattern to create the RestClient instance
	}


    //Points record defintions
  	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Points(@JsonProperty("properties") Props properties) {
		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Props(@JsonProperty("forecast") String forecast) {
		}
	}

  //forecast record definitions
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Forecast(@JsonProperty("properties") Props properties)
  {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Props(@JsonProperty("periods") List<Period> periods)
    {
        public record Period(@JsonProperty("number") Integer number, @JsonProperty("name") String name,
				@JsonProperty("startTime") String startTime, @JsonProperty("endTime") String endTime,
				@JsonProperty("isDaytime") Boolean isDayTime, @JsonProperty("temperature") Integer temperature,
				@JsonProperty("temperatureUnit") String temperatureUnit,
				@JsonProperty("temperatureTrend") String temperatureTrend,
				@JsonProperty("probabilityOfPrecipitation") Map probabilityOfPrecipitation,
				@JsonProperty("windSpeed") String windSpeed, @JsonProperty("windDirection") String windDirection,
				@JsonProperty("icon") String icon, @JsonProperty("shortForecast") String shortForecast,
				@JsonProperty("detailedForecast") String detailedForecast){}
    }

   }

    //alert record definitions
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Alert(@JsonProperty("features") List<Feature> features) {

		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Feature(@JsonProperty("properties") Properties properties) {
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Properties(@JsonProperty("event") String event, @JsonProperty("areaDesc") String areaDesc,
				@JsonProperty("severity") String severity, @JsonProperty("description") String description,
				@JsonProperty("instruction") String instruction) {
		}
	}
 

  @Tool(description = "Get weather forecast for a specific latitude/longitude")
  public String getWeatherForecastByLocation(
      double latitude,   // Latitude coordinate
      double longitude   // Longitude coordinate
  ) {


    //points is just retrieving an object that matches the structure of the record Points defined above
    var points = restClient.get().uri("/points/{latitude},{longitude}", latitude, longitude)
			               .retrieve()
			               .body(Points.class);

    //we use our points object to get the forecast url; we then define the data as the same as the Forecast class defined via records above
    var forecast = restClient.get().uri(points.properties().forecast()).retrieve().body(Forecast.class); 



    //.stream() converts the list into a stream to allow functional operations like map and collect
    //.map() applies the given function to each element in the stream and returns a new stream
	String forecastText = forecast.properties().periods().stream().map(p -> {
		return String.format("""
					%s:
					Temperature: %s %s
					Wind: %s %s
					Forecast: %s
					""", p.name(), p.temperature(), p.temperatureUnit(), p.windSpeed(), p.windDirection(),
					p.detailedForecast());
		}).collect(Collectors.joining());

	return forecastText;
  }

  @Tool(description = "Get weather alerts for a US state")
  public String getAlerts(
      @ToolParam(description = "Two-letter US state code (e.g. CA, NY)") String state
  ) {
        Alert alert = restClient.get().uri("/alerts/active/area/{state}", state).retrieve().body(Alert.class);

		return alert.features()
			.stream()
			.map(f -> String.format("""
					Event: %s
					Area: %s
					Severity: %s
					Description: %s
					Instructions: %s
					""", f.properties().event(), f.properties.areaDesc(), f.properties.severity(),
					f.properties.description(), f.properties.instruction()))
			.collect(Collectors.joining("\n"));
  }

}