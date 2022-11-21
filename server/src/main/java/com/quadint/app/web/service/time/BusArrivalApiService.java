package com.quadint.app.web.service.time;

import com.quadint.app.domain.time.BusTime;
import com.quadint.app.domain.time.Time;
import com.quadint.app.domain.time.BusTimeResponse;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;

@Service
public class BusArrivalApiService {
    private static final String SERVICE_KEY = "X4cMPRltQhalSiXM8QgHsuAOK1%2FasF494602CvtfRMOEOyTmY1h9UOxgzYax5T1oPy%2Bq1m9BtXlsHzznuJFxew%3D%3D";
    private static final String BUS_API_URL = "http://apis.data.go.kr/6280000/busArrivalService/getBusArrivalList";

    public BusTimeResponse getTimeResponse(String bstopId, String routeId) {
        LocalDateTime currTime = LocalDateTime.now();
        BusTimeResponse busTimeResponse = BusTimeResponse.createBusTimeResponse(routeId);
        String currBstopId = bstopId;

        //3개 추출
        for (int i = 0; i < 3; ++i) {
            BusTime bus = getBusArrivalStationTime(currBstopId, routeId);
            if (bus != null) {
                LocalDateTime newTime = Time.calculateTime(currTime, bus.getArrivalTimeSec());
                busTimeResponse.addTime(Time.createTime(newTime));

                currBstopId = bus.getLastBstopId();
                currTime = newTime;
            }
            else {
                break;
            }
        }
        return busTimeResponse;
    }


    private BusTime getBusArrivalStationTime(String bstopId, String routeId)  {
        try {
            StringBuilder url = getBusArrivalStationUrl(bstopId, routeId);
            JSONObject json = XML.toJSONObject(url.toString());
            JSONObject serviceResult = (JSONObject) json.get("ServiceResult");
            JSONObject msgHeader = (JSONObject) serviceResult.get("msgHeader");
            int resultCode = Integer.parseInt(msgHeader.get("resultCode").toString());
            int totalCount = Integer.parseInt(msgHeader.get("totalCount").toString());

            if (resultCode == 0) {
                JSONObject msgBody = (JSONObject) serviceResult.get("msgBody");
                if (totalCount == 1) {
                    JSONObject item = (JSONObject) msgBody.get("itemList");
                    String ROUTEID = item.get("ROUTEID").toString();
                    String bstopid = item.get("BSTOPID").toString();
                    String BUS_NUM_PLATE = item.get("BUS_NUM_PLATE").toString();
                    String LATEST_STOP_NAME = item.get("LATEST_STOP_NAME").toString();
                    String LATEST_STOP_ID = item.get("LATEST_STOP_ID").toString();
                    int arrivalestimatetime = Integer.parseInt(item.get("ARRIVALESTIMATETIME").toString());
                    System.out.println(BUS_NUM_PLATE + " " + ROUTEID + "버스가 " + LATEST_STOP_NAME + "(" + LATEST_STOP_ID + ")에서 " + bstopid + "정류장 도착" + arrivalestimatetime + "초 전 입니다.");
                    return new BusTime(LATEST_STOP_ID, arrivalestimatetime);
                }
                else {
                    return null;
                }
            }
        } catch (IOException e) {
            /**
             * IOException(checked exception)을 RuntimeException(unchecked exception) 으로 처리했다.
             * unchecked exception 이므로 @Controller 메서드에서 throws 를 사용하지 않아도 된다.
             */
            throw new RuntimeException("미구현");
        }
        throw new RuntimeException("미구현");
    }

    private StringBuilder getBusArrivalStationUrl(String bstopId, String routeId) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(BUS_API_URL);
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + SERVICE_KEY);
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("15", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("bstopId", "UTF-8") + "=" + URLEncoder.encode(bstopId, "UTF-8"));
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
        return sb;
    }
}