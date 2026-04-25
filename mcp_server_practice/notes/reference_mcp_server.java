


package org.springframework.ai.mcp.sample.server; 

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class WeatherService {

	private static final String BASE_URL = "https://api.weather.gov";

	private final RestClient restClient;

	public WeatherService() {

		this.restClient = RestClient.builder()
			.baseUrl(BASE_URL)
			.defaultHeader("Accept", "application/geo+json")
			.defaultHeader("User-Agent", "WeatherApiClient/1.0 (your@email.com)")
			.build();
	}


    //-------------record definitions-----------------

    //we use records here since it allows use to easily map the json response from the weather api into java objects
    //we also use @JsonIgnoreProperties to ignore any unknown properties in the json response that we don't care about(prevents errors during deserialization)
    //@JsonProperty is used to map json property names to java record fields when the names differ


    //the naming heirarchy for these records matches the api naming heirarchy

    //Points record defintions--> even though this hold forecast, it isnt the same foraecast record defined below its just an string url
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Points(@JsonProperty("properties") Props properties) {
		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Props(@JsonProperty("forecast") String forecast) {
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Forecast(@JsonProperty("properties") Props properties) {

		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Props(@JsonProperty("periods") List<Period> periods) { //List<type> is java's generic syntax for defining a list of specific type
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Period(@JsonProperty("number") Integer number, @JsonProperty("name") String name,
				@JsonProperty("startTime") String startTime, @JsonProperty("endTime") String endTime,
				@JsonProperty("isDaytime") Boolean isDayTime, @JsonProperty("temperature") Integer temperature,
				@JsonProperty("temperatureUnit") String temperatureUnit,
				@JsonProperty("temperatureTrend") String temperatureTrend,
				@JsonProperty("probabilityOfPrecipitation") Map probabilityOfPrecipitation,
				@JsonProperty("windSpeed") String windSpeed, @JsonProperty("windDirection") String windDirection,
				@JsonProperty("icon") String icon, @JsonProperty("shortForecast") String shortForecast,
				@JsonProperty("detailedForecast") String detailedForecast) {
		}


        
	}
    
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

	/**
	 * Get forecast for a specific latitude/longitude
	 * @param latitude Latitude
	 * @param longitude Longitude
	 * @return The forecast for the given location
	 * @throws RestClientException if the request fails
	 */
	@Tool(description = "Get weather forecast for a specific latitude/longitude")
	public String getWeatherForecastByLocation(double latitude, double longitude) {

		var points = restClient.get()
			.uri("/points/{latitude},{longitude}", latitude, longitude)
			.retrieve()
			.body(Points.class);//.body() method is used to extract the body of the response and convert it into the specified class type --> points.class returns the class type of Points (the record we defined abo)


        //this line converts the json into an object that we can naviagate via the record structure defined above(THIS IS WHY THE RECORDS MATFCH THE API NAMING SCHEME)
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

	/**
	 * Get alerts for a specific area
	 * @param state Area code. Two-letter US state code (e.g. CA, NY)
	 * @return Human readable alert information
	 * @throws RestClientException if the request fails
	 */
	@Tool(description = "Get weather alerts for a US state. Input is Two-letter US state code (e.g. CA, NY)")
	public String getAlerts(@ToolParam( description =  "Two-letter US state code (e.g. CA, NY") String state) {
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

	public static void main(String[] args) {
		WeatherService client = new WeatherService();
		System.out.println(client.getWeatherForecastByLocation(47.6062, -122.3321));
		System.out.println(client.getAlerts("NY"));
	}

}