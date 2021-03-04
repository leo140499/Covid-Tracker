package com.Msoftwares.CovidTracker;

import com.Msoftwares.CovidTracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CovidDataService {

    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    private List<LocationStats> allLocationStats = new ArrayList<>();

    public List<LocationStats> getAllLocationStats() {
        return allLocationStats;
    }

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")
    public void searchCovData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        //CSV FILE READER
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
            LocationStats locationStat = new LocationStats();
            locationStat.setState(record.get("Province/State"));
            locationStat.setCountry(record.get("Country/Region"));
           int latestCases = Integer.parseInt(record.get(record.size()-1));
           int previousDayCases = Integer.parseInt(record.get(record.size()-2));
           locationStat.setLatestTotalCases(latestCases);
           locationStat.setDifferenceFromPreviousDay(latestCases - previousDayCases);

            newStats.add(locationStat);
        }
        this.allLocationStats = newStats;
        }
    }


