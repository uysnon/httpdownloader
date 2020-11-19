package com.avgorkin.httpdownloader.model.data;

/**
 * Виды доступности ресурса
 */
public enum ResourceAvailabilityStates {
    /**
     * Неизвестно, доступен ресурс или нет (проверка не проводилась)
     */
    NOT_DEFINED("неизвестно"),
    /**
     * Ресурс доступен
     */
    AVAILABLE("доступно"),
    /**
     * Ресурс недоступен
     */
    NOT_AVAILABILE("недоступно");

    String title;

    ResourceAvailabilityStates(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean isPositive(){
        return this == AVAILABLE;
    }
}
