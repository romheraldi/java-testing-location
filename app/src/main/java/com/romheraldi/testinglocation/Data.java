package com.romheraldi.testinglocation;

public class Data {
    public String nameStore;
    public String latLong;
    public Float accuracy;
    public String dataId;

    public Data() {
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setNameStore(String nameStore) {
        this.nameStore = nameStore;
    }

    public void setLatLong(String latLong) {
        this.latLong = latLong;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    public String getNameStore() {
        return nameStore;
    }

    public String getLatLong() {
        return latLong;
    }

    public Float getAccuracy() {
        return accuracy;
    }


}
