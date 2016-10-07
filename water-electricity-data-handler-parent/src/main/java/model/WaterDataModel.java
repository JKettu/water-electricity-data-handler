package model;

/**
 * Created by Jay on 14.07.2016.
 */
public class WaterDataModel implements Comparable<WaterDataModel>{
    private String address=""; //адрес
    private int group; //группа
    private int region; //регион
    private int biggestFloor; // максимальный этаж
    private int smallestFloor; //минимальный этаж
    private double joint; // общая площадь помещений, входящих в состав общего имущества в многоквартирных домах
    private int people; //численность проживающих жителей в i-м многоквартирном доме или жилом доме
    private String hasColdWaterAccountingDevice; //Наличие коллективного (общедомового) прибора учета холодной воды
    private String hasHotWaterAccountingDevice; //Наличие коллективного (общедомового) прибора учета горячей воды
    private double expenseHouseCold; //Расход холодной воды по показаниям коллективного (общедомового) прибора учета за отопительный период
    private double expenseHouseHot; //Расход горячей воды по показаниям коллективного (общедомового) прибора учета за отопительный период

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getBiggestFloor() {
        return biggestFloor;
    }

    public void setBiggestFloor(int floor) {
        this.biggestFloor = floor;
    }

    public int getSmallestFloor() {
        return smallestFloor;
    }

    public void setSmallestFloor(int floor) {
        this.smallestFloor = floor;
    }

    public int getRegion() {
        return region;
    }

    public void setRegion(int region) {
        this.region = region;
    }

    public double getJoint() {
        return joint;
    }

    public void setJoint(double joint) {
        this.joint = joint;
    }

    public int getPeople() {
        return people;
    }

    public void setPeople(int people) {
        this.people = people;
    }

    public double getExpenseHouseCold() {
        return expenseHouseCold;
    }

    public void setExpenseHouseCold(double expenseHouseCold) {
        this.expenseHouseCold = expenseHouseCold;
    }

    public double getExpenseHouseHot() {
        return expenseHouseHot;
    }

    public void setExpenseHouseHot(double expenseHouseHot) {
        this.expenseHouseHot = expenseHouseHot;
    }

    public String getColdWaterAccountingDevice() {
        return hasColdWaterAccountingDevice;
    }

    public void setColdWaterAccountingDevice(String hasColdWaterAccountingDevice) {
        this.hasColdWaterAccountingDevice = hasColdWaterAccountingDevice;
    }

    public String getHotWaterAccountingDevice() {
        return hasHotWaterAccountingDevice;
    }

    public void setHotWaterAccountingDevice(String hasHotWaterAccountingDevice) {
        this.hasHotWaterAccountingDevice = hasHotWaterAccountingDevice;
    }

    @Override
    public int compareTo(WaterDataModel o) {
        return (this.getGroup() - o.getGroup());
    }
}
