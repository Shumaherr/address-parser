package com.fmlogistic.addressparser.service;

import com.fmlogistic.addressparser.model.Address;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParsingService {

    private final String API_KEY = "AIzaSyB_x-34LU1z9_bbHwgvDr2gcHGmVJw_Oq8";
    private final String API_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private final String API_URL2 = "https://geocode-maps.yandex.ru/1.x/?geocode=";

    public Address parsAddress(String line) {
        Address result = new Address();

        try
        {
            String placeId = getPlaceId(line);
            if(!placeId.equals(null))
            {

                if(!parsWithGoogle(line, placeId).equals(null))
                {
                    result = parsWithGoogle(line, placeId);
                }
                else
                {
                    result = parsWithYandex(line);
                }

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //TODO
        if (line.contains("Кв")) {
            result.setFlat(line.substring(line.indexOf("Кв") + 2, line.length()).replaceAll("[^0-9]", ""));
        } else if (line.contains("кв")) {
            result.setFlat(line.substring(line.indexOf("кв") + 2, line.length()).replaceAll("[^0-9]", ""));
        }
        return result;

    }

    private String getPlaceId(String addressLine) throws MalformedURLException {
        String params = "query=" + addressLine + "&language=ru&key=" + API_KEY;
        URL url = new URL(API_URL + params);
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 200) {
                JsonParser parser = JsonParserFactory.getJsonParser();
                Map<String, Object> responseMap = parser.parseMap(connection.getResponseMessage());
                if (responseMap.get("status").toString().equals("OK")) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) responseMap.get("results");
                    return results.get(0).get("place_id").toString();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private Address parsWithGoogle(String addressLine, String placeId) throws MalformedURLException {
        String params = "placeid=" + placeId + "&language=ru" + "&key=" + API_KEY;
        URL url = new URL(API_URL + params);
        Address result = new Address();
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 200) {
                JSONObject response = new JSONObject(connection.getResponseMessage());
                if (response.getString("status").equals("OK")) {
                    JSONArray addressComponents = response.getJSONObject("result").getJSONArray("address_components");
                    for (Object o : addressComponents) {
                        JSONObject component = (JSONObject) o;
                        switch (component.getJSONArray("types").getString(0)) {
                            case "postal_code":
                                result.setPostalCode(component.getString("long_name"));
                                break;
                            case "country":
                                result.setCountry(component.getString("long_name"));
                                break;
                            case "administrative_area_level_1":
                                result.setState(component.getString("long_name"));
                                break;
                            case "administrative_area_level_2":
                                result.setArea(component.getString("long_name"));
                                break;
                            case "locality":
                                result.setCity(component.getString("long_name"));
                                break;
                            case "route":
                                result.setStreet(component.getString("long_name"));
                                break;
                            case "street_number":
                                result.setHouse(component.getString("long_name"));
                                break;
                            case "subpremise":
                                result.setFlat(component.getString("long_name"));
                                break;
                        }
                    }
                    return result;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private Address parsWithYandex(String addressLine) throws MalformedURLException {
        String params = addressLine + "&format=json";
        URL url = new URL(API_URL2 + params);

        Address result = new Address();
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            org.json.JSONObject obj1 = new org.json.JSONObject(connection.getResponseMessage());


            org.json.JSONObject address = obj1.getJSONObject("response").getJSONObject("GeoObjectCollection").getJSONArray("featureMember").getJSONObject(0).getJSONObject("GeoObject").getJSONObject("metaDataProperty").getJSONObject("GeocoderMetaData").getJSONObject("Address");

            try {
                result.setPostalCode( address.getString("postal_code"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            org.json.JSONArray addressComponents = address.getJSONArray("Components");

            for (int i = 0; i < addressComponents.length(); i++) {
                JSONObject addressobject = addressComponents.getJSONObject(i);

                switch (addressobject.getString("kind")) {

                    case "house":
                        result.setHouse(addressComponents.getJSONObject(i).getString("name"));
                        break;
                    case "province":
                        result.setState(addressComponents.getJSONObject(i).getString("name"));
                        break;
                    case "street":
                        result.setStreet(addressComponents.getJSONObject(i).getString("name"));
                        break;
                    case "area":
                        result.setArea(addressComponents.getJSONObject(i).getString("name"));
                        break;
                    case "locality":
                        result.setCity(addressComponents.getJSONObject(i).getString("name"));
                        break;
                    case "country":
                        result.setCountry(addressComponents.getJSONObject(i).getString("name"));
                        break;
                }

            }
            } catch(ProtocolException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
        return result;
        }

    }