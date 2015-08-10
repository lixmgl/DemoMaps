package com.example.fangyichen.demomaps;
import org.apache.commons.validator.routines.InetAddressValidator;



/**
 * Created by Xiaomeng Li on 8/10/15.
 * This class is used to validate IPv4 address and convert format between string to long or vice versa
 * Based on the idea: http://technojeeves.com/index.php/21-convert-ip-address-to-number
 */
public class IpConverter {
    private static InetAddressValidator myValidator = new InetAddressValidator();

    /**
     * @param address IPv4 address
     * @return true if the string is valid IPv4 address format
     */
    public static boolean IsValidIpAddress(String address){
        return myValidator.isValidInet4Address(address);
    }

    /**
     * convert an IPv4 address from string to long
     * @param ipAddress string format IPv4 address
     * @return long format IPv4 address
     */
    public static long ipToLong(String ipAddress) {
        long result = 0;
        String[] atoms = ipAddress.split("\\.");
        for (int i = 3; i >= 0; i--) {
            result |= (Long.parseLong(atoms[3 - i]) << (i * 8));
        }
        return result & 0xFFFFFFFF;
    }

    /**
     * convert an IPv4 address from long to string
     * @param ip long format IPv4 address
     * @return string format IPv4 address
     */
    public static String longToIp(long ip) {
        StringBuilder sb = new StringBuilder(15);
        for (int i = 0; i < 4; i++) {
            sb.insert(0, Long.toString(ip & 0xff));
            if (i < 3) {
                sb.insert(0, '.');
            }
            ip >>= 8;
        }
        return sb.toString();
    }
}
