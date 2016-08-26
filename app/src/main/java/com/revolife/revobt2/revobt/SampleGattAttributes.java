/*
 *  2016 Revo.life USeeBand
 *
 * Приложение создано при поддержке Теплицы социальных технологий.
 */

package com.revolife.revobt2.revobt;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {

    final public static String macAdrCmp1 = "02:80:E1:00:34:12";
    final public static String DevNameCmp1 = "BlueNRG";

    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "02366E80-CF3A-11E1-9AB4-0002A5D5C51B";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        //attributes.put("02366E80-CF3A-11E1-9AB4-0002A5D5C51B", "Heart Rate Service");
        // Sample Characteristics.
        attributes.put("340a1b80-cf4b-11e1-ac36-0002a5d5c51b", "Data");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
