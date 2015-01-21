package cn.edu.sjtu.omnilab.livewee.logproducer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogProcessor {

    private static final Logger logger =
            LogManager.getLogger(LogProcessor.class.getName());

    public static String cleanse(String message) throws IOException {

        // Message codes we require
        final int[] CODE_AUTHREQ = {501091, 501092, 501109};
        final int[] CODE_AUTHRES = {501093, 501094, 501110};
        final int[] CODE_DEAUTH = {501105, 501080, 501098, 501099, 501106, 501107, 501108, 501111};
        final int[] CODE_ASSOCREQ = {501095, 501096, 501097};
        final int[] CODE_ASSOCRES = {501100, 501101, 501112};
        final int[] CODE_DISASSOCFROM = {501102, 501104, 501113};
        final int[] CODE_USERAUTH = {522008, 522042, 522038};
        final int[] CODE_USRSTATUS = {522005, 522006, 522026};
        final int[] CODE_USERROAM = {500010};

        // Generic patterns to extract message fields:
        // MAC address
        final String regMAC = "([0-9a-f]{2}:){5}[0-9a-f]{2}";
        // IP address
        final String regIP = "(\\d{1,3}\\.){3}\\d{1,3}";
        // Datetime and year
        final String regPrefix = "(?<time>\\w+\\s+\\d+\\s+(\\d{1,2}:){2}\\d{1,2}\\s+\\d{4})";
        // User MAC address
        final String regUserMac = String.format("(?<umac>%s)", regMAC);
        // WiFi AP info
        final String regApInfo = String.format("(?<apip>%s)-(?<apmac>%s)-(?<apname>[\\w-]+)", regIP, regMAC);
        
        /**
         * Message-specific filters.
         * NOTE:
         * Before Oct. 14, 2013, WiFi networks in SJTU deploy the IP address of 111.186.0.0/18.
         * After that (or confirmedly Oct. 17, 2013), local addresses are utlized to save the IP
         * resources. New IP addresses deployed in WiFi networks are:
         * Local1: 1001 111.186.16.1/20, New: 10.185.0.0/16
         * Local2: 1001 111.186.33.1/21, New: 10.186.0.0/16
         * Local3: 1001 111.186.40.1/21, New: 10.188.0.0/16
         * Local4: 1001 111.186.48.1/20, New: 10.187.0.0/16
         * Local5: 1001 111.186.0.1/20, New: 10.184.0.0/16
         */
        final Pattern REG_AUTHREQ = Pattern.compile(String.format(
                "%s(.*)Auth\\s+request:\\s+%s:?\\s+(.*)AP\\s+%s",
                regPrefix, regUserMac, regApInfo), Pattern.CASE_INSENSITIVE);
        final Pattern REG_AUTHRES = Pattern.compile(String.format(
                "%s(.*)Auth\\s+(success|failure):\\s+%s:?\\s+AP\\s+%s",
                regPrefix, regUserMac, regApInfo), Pattern.CASE_INSENSITIVE);
        final Pattern REG_DEAUTH = Pattern.compile(String.format(
                "%s(.*)Deauth(.*):\\s+%s:?\\s+(.*)AP\\s+%s",
                regPrefix, regUserMac, regApInfo), Pattern.CASE_INSENSITIVE);
        final Pattern REG_ASSOCREQ = Pattern.compile(String.format(
                "%s(.*)Assoc(.*):\\s+%s(.*):?\\s+(.*)AP %s",
                regPrefix, regUserMac, regApInfo), Pattern.CASE_INSENSITIVE);
        final Pattern REG_DISASSOCFROM = Pattern.compile(String.format(
                "%s(.*)Disassoc(.*):\\s+%s:?\\s+AP\\s+%s",
                regPrefix, regUserMac, regApInfo), Pattern.CASE_INSENSITIVE);
        final Pattern REG_USERAUTH = Pattern.compile(String.format(
                "%s(.*)\\s+username=(?<uname>[^\\s]+)\\s+MAC=%s\\s+IP=(?<uip>%s)(.+)(AP=(?<apname>[^\\s]+))?",
                regPrefix, regUserMac, regIP), Pattern.CASE_INSENSITIVE);
        final Pattern REG_USRSTATUS = Pattern.compile(String.format(
                        "%s(.*)MAC=%s\\s+IP=(?<uip>(111\\.\\d+|10\\.18[4-8])(\\.\\d+){2})",
                        regPrefix, regUserMac), Pattern.CASE_INSENSITIVE);

        // remove invalid messages
        String[] chops = new String[0];
        try {
            chops = message.split("<", 3);
        } catch (Exception e) {
            logger.warn(e.toString());
            return null;
        }

        // remove messages without valid codes
        if (chops.length < 3 || chops[2].length() == 0 ||
                chops[2].charAt(0) != '5') {
            logger.warn("The message does not start with number '5'.");
            return null;
        }

        int messageCode = Integer.valueOf(chops[2].split(">", 2)[0]);
        String filteredMessage = null;

        if (hasCodes(messageCode, CODE_AUTHREQ)) { // Auth request
            Matcher matcher = REG_AUTHREQ.matcher(message);
            if (matcher.find()) {
                String usermac = matcher.group("umac").replaceAll(":", "");
                String time = formatTime(matcher.group("time"));
                filteredMessage = String.format("%s\t%s\t%s\t%s\n",
                        usermac, time, "AuthRequest", matcher.group("apname"));
            }
        } else if (hasCodes(messageCode, CODE_DEAUTH)) { // Deauth from and to
            Matcher matcher = REG_DEAUTH.matcher(message);
            if (matcher.find()) {
                String usermac = matcher.group("umac").replaceAll(":", "");
                String time = formatTime(matcher.group("time"));
                filteredMessage = String.format("%s\t%s\t%s\t%s\n",
                        usermac, time, "Deauth", matcher.group("apname"));
            }
        } else if (hasCodes(messageCode, CODE_ASSOCREQ)) { // Association request
            Matcher matcher = REG_ASSOCREQ.matcher(message);
            if (matcher.find()) {
                String usermac = matcher.group("umac").replaceAll(":", "");
                String time = formatTime(matcher.group("time"));
                filteredMessage = String.format("%s\t%s\t%s\t%s\n",
                        usermac, time, "AssocRequest", matcher.group("apname"));
            }
        } else if (hasCodes(messageCode, CODE_DISASSOCFROM)) { // Disassociation
            Matcher matcher = REG_DISASSOCFROM.matcher(message);
            if (matcher.find()) {
                String usermac = matcher.group("umac").replaceAll(":", "");
                String time = formatTime(matcher.group("time"));
                filteredMessage = String.format("%s\t%s\t%s\t%s\n",
                        usermac, time, "Disassoc", matcher.group("apname"));
            }
        } else if (hasCodes(messageCode, CODE_USERAUTH)) {  //username information, User authentication
            Matcher matcher = REG_USERAUTH.matcher(message);
            if (matcher.find()) {
                String usermac = matcher.group("umac").replaceAll(":", "");
                String time = formatTime(matcher.group("time"));
                // apname is null if it is not there
                filteredMessage = String.format("%s\t%s\t%s\t%s\t%s\t%s\n",
                        usermac, time, "UserAuth", matcher.group("apname"),
                        matcher.group("uname"), matcher.group("uip"));
            }
        } else if (hasCodes(messageCode, CODE_USRSTATUS)) { // User entry update status
            Matcher matcher = REG_USRSTATUS.matcher(message);
            if (matcher.find()) {
                String usermac = matcher.group("umac").replaceAll(":", "");
                String time = formatTime(matcher.group("time"));
                String iPInfo = "IPAllocation";    // IP bond
                if (messageCode == 522005) {
                    iPInfo = "IPRecycle";
                }
                /* From the output, we see multiple IPAllocation message between
                 * the first allocation of specific IP and its recycling action.
                 */
                filteredMessage = String.format("%s\t%s\t%s\t%s\n",
                        usermac, time, iPInfo, matcher.group("uip"));
            }
        }

        if (filteredMessage == null) {
            logger.warn("Not the message what we want.");
        }

        return filteredMessage;
    }

    /**
     * Helper to check if specific message code is contained.
     */
    private static boolean hasCodes(int messageCode, int[] codes) {
        boolean flag = false;
        for (int i : codes) {
            if (messageCode == i) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * This function is used to change the date format
     * from "May 4 2013" to "2013-05-04".
     */
    private static String formatTime(String date_string) {

        //Prepare for the month name for date changing
        TreeMap<String, String> month_tmap = new TreeMap<String, String>();
        month_tmap.put("Jan", "01");
        month_tmap.put("Feb", "02");
        month_tmap.put("Mar", "03");
        month_tmap.put("Apr", "04");
        month_tmap.put("May", "05");
        month_tmap.put("Jun", "06");
        month_tmap.put("Jul", "07");
        month_tmap.put("Aug", "08");
        month_tmap.put("Sep", "09");
        month_tmap.put("Oct", "10");
        month_tmap.put("Nov", "11");
        month_tmap.put("Dec", "12");

        //change the date from "May 4" to "2013-05-04"
        String date_reg = "(?<month>\\w+)\\s+(?<day>\\d+)\\s+(?<time>(\\d{1,2}:){2}\\d{1,2})\\s+(?<year>\\d{4})";
        Pattern date_pattern = Pattern.compile(date_reg);
        Matcher date_matcher = date_pattern.matcher(date_string);

        if (!date_matcher.find())
            return null;

        String year_string = date_matcher.group("year");

        //change the month format
        String month_string = date_matcher.group("month");
        if (month_tmap.containsKey(month_string)) {
            month_string = month_tmap.get(month_string);
        } else {
            logger.error("Can not find the month!!!");
        }

        //change the day format
        String day_string = date_matcher.group("day");
        int day_int = Integer.parseInt(day_string);
        if (day_int < 10) {
            day_string = "0" + Integer.toString(day_int);
        } else {
            day_string = Integer.toString(day_int);
        }

        String time_string = date_matcher.group("time");

        return String.format("%s-%s-%s %s", year_string, month_string,
                day_string, time_string);
    }
}
