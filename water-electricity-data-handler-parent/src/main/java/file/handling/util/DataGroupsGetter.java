package file.handling.util;

import common.DataType;

public class DataGroupsGetter {
    private static final String[] ELECTRICITY_GROUPS = {
            "0. Дома с неуказанной категорией",
            "1. Многоквартирные дома, не оборудованные лифтами и электроотопительными и электронагревательными установками для целей горячего водоснабжения",
            "2. Многоквартирные дома, оборудованные лифтами и не оборудованные электроотопительными и электронагревательными установками для целей горячего водоснабжения",
            "3. Многоквартирные дома, не оборудованные лифтами и оборудованные электроотопительными и (или) электронагревательными установками для целей горячего водоснабжения, в отопительный период",
            "4. Многоквартирные дома, не оборудованные лифтами и оборудованные электроотопительными и (или) электронагревательными установками для целей горячего водоснабжения, вне отопительного периода",
            "5. Категория №5",
            "6. Категория №6",
            "7. Категория №7",
            "8. Категория №8",
            "9. Дома не попадающие в другие категории"
    };

    private static final String[] WATER_GROUPS = {
            "0. Дома с неуказанной категорией",
            "1. Многоквартирные дома с централизованным холодным и горячим водоснабжением, водоотведением",
            "2. Многоквартирные дома без водонагревателей с централизованным холодным водоснабжением и водоотведением, оборудованные раковинами, мойками и унитазами",
            "3. Многоквартирные дома с централизованным холодным водоснабжением без централизованного водоотведения",
            "4. Многоквартирные дома с централизованным холодным водоснабжением без централизованного водоотведения",
            "5. Дома, не попадающие в другие категории"
    };


    public static String getGroup(int groupNumber, DataType dataType) {
        switch (dataType) {
            case WATER:
                return getGroupsForWater(groupNumber);
            case ELECTRICITY:
                return getGroupsForElectricity(groupNumber);
        }
        return null;
    }

    private static String getGroupsForElectricity(int groupNumber) {
        int electricityGroupsLength = ELECTRICITY_GROUPS.length;
        if (groupNumber < electricityGroupsLength) {
            return ELECTRICITY_GROUPS[groupNumber];
        } else {
            return ELECTRICITY_GROUPS[electricityGroupsLength - 1];
        }
    }

    private static String getGroupsForWater(int groupNumber) {
        int waterGroupsLength = WATER_GROUPS.length;
        if (groupNumber < waterGroupsLength) {
            return WATER_GROUPS[groupNumber];
        } else {
            return WATER_GROUPS[waterGroupsLength - 1];
        }
    }


}
