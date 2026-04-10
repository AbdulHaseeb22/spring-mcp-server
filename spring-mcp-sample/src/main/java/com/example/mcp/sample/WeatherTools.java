package com.example.mcp.sample;

import com.example.mcp.annotations.McpParam;
import com.example.mcp.annotations.McpServer;
import com.example.mcp.annotations.McpTool;
import org.springframework.stereotype.Component;

/**
 * Example MCP server component that exposes weather-related tools.
 */
@Component
@McpServer("weather")
public class WeatherTools {

    @McpTool(name = "get_current_weather",
             description = "Returns the current weather for the given city.")
    public String getCurrentWeather(
            @McpParam(name = "city", description = "City name, e.g. 'London'") String city) {
        // Stub — replace with a real weather API call
        return "Sunny, 22°C in " + city;
    }

    @McpTool(name = "get_forecast",
             description = "Returns a N-day weather forecast for the given city.")
    public String getForecast(
            @McpParam(name = "city",  description = "City name")          String city,
            @McpParam(name = "days",  description = "Number of days (1-7)") int days) {
        return "Forecast for " + city + " over " + days + " day(s): mostly sunny.";
    }
}