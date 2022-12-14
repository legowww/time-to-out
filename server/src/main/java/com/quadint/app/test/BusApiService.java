package com.quadint.app.test;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class BusApiService {
    private static final String SERVICE_KEY = "X4cMPRltQhalSiXM8QgHsuAOK1%2FasF494602CvtfRMOEOyTmY1h9UOxgzYax5T1oPy%2Bq1m9BtXlsHzznuJFxew%3D%3D";
    private static final String STATION_API_URL = "http://apis.data.go.kr/6280000/busArrivalService/getAllRouteBusArrivalList";
    private static final String BUS_API_URL = "http://apis.data.go.kr/6280000/busLocationService/getBusRouteLocation";

    public List<Bus> stationInformation(String bstopId)  {
        try {
            StringBuilder url = getStationInformationURL(bstopId);
            JSONObject json = XML.toJSONObject(url.toString());
            JSONObject serviceResult = (JSONObject) json.get("ServiceResult");
            JSONObject msgHeader = (JSONObject) serviceResult.get("msgHeader");
            int resultCode = Integer.parseInt(msgHeader.get("resultCode").toString());
            int totalCount = Integer.parseInt(msgHeader.get("totalCount").toString());

            List<Bus> result = new ArrayList<>();
            if (resultCode == 0) {
                JSONObject msgBody = (JSONObject) serviceResult.get("msgBody");
                if (totalCount == 1) {
                    JSONObject item = (JSONObject) msgBody.get("itemList");
                    String ROUTEID = item.get("ROUTEID").toString();
                    String BSTOPID = item.get("BSTOPID").toString();
                    int arrivalestimatetime = Integer.parseInt(item.get("ARRIVALESTIMATETIME").toString());
                    result.add(new Bus(ROUTEID, BSTOPID, arrivalestimatetime));
                }
                else {
                    JSONArray items = (JSONArray) msgBody.get("itemList");
                    for (int i = 0; i < items.length(); ++i) {
                        JSONObject item = (JSONObject) items.get(i);
                        String ROUTEID = item.get("ROUTEID").toString();
                        String BSTOPID = item.get("BSTOPID").toString();
                        String BUS_NUM_PLATE = item.get("BUS_NUM_PLATE").toString();
                        String LATEST_STOP_NAME = item.get("LATEST_STOP_NAME").toString();
                        String LATEST_STOP_ID = item.get("LATEST_STOP_ID").toString();
                        int arrivalestimatetime = Integer.parseInt(item.get("ARRIVALESTIMATETIME").toString());
                        System.out.println(BUS_NUM_PLATE + " " + ROUTEID + "????????? " + LATEST_STOP_NAME + "("
                                + LATEST_STOP_ID + ")?????? " + BSTOPID + "????????? ??????" + arrivalestimatetime + "??? ??? ?????????.");
                        result.add(new Bus(ROUTEID, BSTOPID, arrivalestimatetime));
                    }
                }
            }
            return result;
        } catch (IOException e) {
            /**
             * IOException(checked exception)??? RuntimeException(unchecked exception) ?????? ????????????.
             * unchecked exception ????????? @Controller ??????????????? throws ??? ???????????? ????????? ??????.
             */
            throw new RuntimeException("SA");
        }
    }

    public void busInformation(String routeId) {
        try {
            StringBuilder url = getBusInformationURL(routeId);
            JSONObject json = XML.toJSONObject(url.toString());
            JSONObject serviceResult = (JSONObject) json.get("ServiceResult");
            JSONObject msgHeader = (JSONObject) serviceResult.get("msgHeader");
            int resultCode = Integer.parseInt(msgHeader.get("resultCode").toString());
            int totalCount = Integer.parseInt(msgHeader.get("totalCount").toString());

            if (resultCode == 0) {
                JSONObject msgBody = (JSONObject) serviceResult.get("msgBody");
                if (totalCount == 1) {
                    JSONObject item = (JSONObject) msgBody.get("itemList");
                }
                else {
                    JSONArray items = (JSONArray) msgBody.get("itemList");
                    for (int i = 0; i < items.length(); ++i) {
                        JSONObject item = (JSONObject) items.get(i);
                        /**
                         * 8??? ??????
                         * 0: ??????(?????????)- ????????? ?????? ??????
                         * 1: ??????(?????????)-????????? ??????
                         */
                        String DIRCD = item.get("DIRCD").toString();
                        String BUS_NUM_PLATE = item.get("BUS_NUM_PLATE").toString();
                        String LATEST_STOP_ID = item.get("LATEST_STOP_ID").toString();
                        String LATEST_STOP_NAME = item.get("LATEST_STOP_NAME").toString();
                        System.out.println(BUS_NUM_PLATE + "??????(" + DIRCD + "): " + LATEST_STOP_NAME + " (" + LATEST_STOP_ID + ")");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("SA");
        }
    }

    private StringBuilder getStationInformationURL(String bstopId) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(STATION_API_URL);
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + SERVICE_KEY);
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("15", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("bstopId", "UTF-8") + "=" + URLEncoder.encode(bstopId, "UTF-8"));
        return setRequest(urlBuilder);
    }

    private StringBuilder getBusInformationURL(String routeId) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(BUS_API_URL);
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=" + SERVICE_KEY);
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("35", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("routeId","UTF-8") + "=" + URLEncoder.encode(routeId, "UTF-8"));
        return setRequest(urlBuilder);
    }

    private StringBuilder setRequest(StringBuilder urlBuilder) throws IOException {
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        conn.setRequestProperty("Accept", "text/xml");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        System.out.println(url);
        return sb;
    }
}
