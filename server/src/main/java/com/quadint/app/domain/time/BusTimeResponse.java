package com.quadint.app.domain.time;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * SUBWAY 클래스 추가 후
 * 상속 관계로 리팩토링 예정
 *              TimeResponse
 *      BusTimeResponse  SubwayTimeResponse
 */
public class BusTimeResponse {
    @Getter
    private String routeId;
    @Getter
    private List<Time> times = new ArrayList<>();

    private BusTimeResponse(String routeId) {
        this.routeId = routeId;
    }

    public static BusTimeResponse createBusTimeResponse(String routeId) {
        return new BusTimeResponse(routeId);
    }

    public void addTime(Time time) {
        times.add(time);
    }

    @Override
    public String toString() {
        return "BusTimeResponse{" +
                "routeId='" + routeId + '\'' +
                ", times=" + times +
                '}';
    }
}