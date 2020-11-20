/*---------------------------------------------------------------------------
 * Copyright (C) 1999,2000 Maxim Integrated Products, All Rights Reserved.
 * Copyright (C) 2020 KrispyK, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL MAXIM INTEGRATED PRODUCTS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Maxim Integrated Products
 * shall not be used except as stated in the Maxim Integrated Products
 * Branding Policy.
 *---------------------------------------------------------------------------
 */

package la.krispy.onewire.ibsv;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.OneWireContainer02;
import com.dalsemi.onewire.utils.Address;
import com.dalsemi.onewire.utils.Convert;
import com.dalsemi.onewire.utils.Convert.ConvertException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * We gotta do the thing. The secure iButton thing. DS1991 Multikey iButton has
 * deprecated security In this program, data is manipulated bytewise and hex
 * values without a symbol are represented by a dot '.', just like in TMEX
 * Runtime Environment Secure Viewer. Initalized fields are filled with 'U' =
 * 0x55 = ASCII code 85. Default password is eight spaces (0x20/ASCII code 32).
 * ID and password fields shall only accept eight bytes, shorter entries are
 * padded with spaces to meet hardware requirements of the DS1991.
 */

public class iBSV {
    static Scanner CONSOLE = new Scanner(System.in);
    static DSPortAdapter dsportadapter;

    public static void main(String[] args) throws Exception {

        // DSPortAdapter adapter;

        try {
            setDSPortAdapter();
            DSPortAdapter adapter = getDSPortAdapter();

            // SESSION: Negotiate exclusive use of 1-Wire bus
            adapter.beginExclusive(true);

            /**
             * LINK: connect to 1-Wire bus
             * 
             */
            // print program header, clear previous search restrictions
            initiBSV(adapter);

            /**
             * NETWORK: Device discovery and selection
             */
            // We will focus on a single device (DS1991 family).
            OneWireContainer02 onewirecontainer02 = grabFirstContainer02(adapter);
            System.out.printf("=== %s ===%n%s%n===        ===%n", onewirecontainer02.getName(),
                    onewirecontainer02.getDescription());

            /**
             * TRANSPORT: DS1991 only supports primative memory functions and some unique
             * commands
             */
            mainMenu(onewirecontainer02);
            // viewDS1991(onewirecontainer02);

            // unblock adapter and free the port
            adapter.endExclusive();
            adapter.freePort();

            // Testing program running
        } catch (OneWireIOException e) {
            System.out.println(e + " Adapter communication failure");
        } catch (OneWireException e) {
            System.out.println(e + " can't find Adapter/Port.");
            return;
        }
    }

    private static void operationOptions() {
        System.out.println("1. change button");
        System.out.println("2. read scratchpad");
        System.out.println("3. save subkey");
        System.out.println("4. write scratchpad");
        System.out.println("5. clear scratchpad");
        System.out.println("6. write subkey from file");
        System.out.println("7. copy scratchpad");
        System.out.println("9. return to main menu");
        System.out.println("0. Quit");
    }

    private static void operationsMenu(OneWireContainer02 owc02) throws Exception {
        OneWireContainer02 currentiButton = owc02;
        /**
         * Check if class adapter matches container field, otherwise do noresetsearch on
         * (currentadapter) and update container if located.
         */

        boolean back = false;
        int menuItem;

        do {
            System.out.println();
            System.out.printf("%ncurrent iButton: %s%n", currentiButton.getAddressAsString());
            operationOptions();
            System.out.print("Select option: ");

            menuItem = CONSOLE.next().charAt(0);
            switch (menuItem) {
                case '1': // Scans for new DS1991 devices and prompts selection.
                    chooseDS1991();
                    break;
                case '2': // Read scratchpad contents.
                    System.out.println("Reading scratchpad...");
                    readScratchpad(currentiButton);
                    pressEnterToContinue();
                    break;
                case '3': // Save subkey contents to file.
                    System.out.println("TODO: select one subkey (use 'dump DS1991' to read all)");
                    pressEnterToContinue();
                    break;
                case '4': // Write scratchpad.
                    System.out.println("TODO: write scratchpad from file");
                    pressEnterToContinue();
                    break;
                case '5': // Clear scratchpad.
                    clearScratchpad(currentiButton);
                    readScratchpad(currentiButton);
                    pressEnterToContinue();
                    break;
                case '6':
                    System.out.println("TODO: write subkeys from file");
                    pressEnterToContinue();
                    break;
                case '7':
                    System.out.println("TODO: command copy scratchpad");
                    pressEnterToContinue();
                    break;
                case '9':
                    back = true;
                    break;
                case '0':
                case 'q':
                case 'Q':
                    System.exit(0);
                default:
                    System.out.print("Invalid selection");
            }
        } while (!back);
    }

    private static void mainmenuOptions() {
        System.out.println("1. change button");
        System.out.println("2. check password");
        System.out.println("3. dump DS1991");
        System.out.println("4. operate button");
        System.out.println("0. Quit");
    }

    private static void mainMenu(OneWireContainer02 owc02) throws Exception {
        OneWireContainer02 currentiButton = owc02;
        chooseDS1991();

        boolean quit = false;
        int menuItem;

        do {
            // currentiButton = new OneWireContainer02((DSPortAdapter) null,
            // "69420420CBDEFF02");
            System.out.printf("%ncurrent iButton: %s%n", currentiButton.getAddressAsString());
            mainmenuOptions();
            System.out.print("Select option: ");
            menuItem = CONSOLE.next().charAt(0);
            switch (menuItem) {
                case '1':
                    chooseDS1991();
                    break;
                case '2':
                    viewDS1991(currentiButton);
                    break;
                case '3':
                    System.out.println("TODO DUMP TO FILE");
                    pressEnterToContinue();
                    break;
                case '4':
                    operationsMenu(currentiButton);
                    break;
                case '0':
                case 'q':
                case 'Q':
                    quit = true;
                    // break;
                    return;
                default:
                    System.out.println("Invalid selection");
            }
        } while (!quit);
    }

    @SuppressWarnings("unchecked")
    private static OneWireContainer02 chooseDS1991() {
        /**
         * Get current adapter, display DS1991 devices, return container
         */
        DSPortAdapter adapter = getDSPortAdapter();
        Enumeration<OneWireContainer> owContainerEnum = Collections.emptyEnumeration();
        Iterator<OneWireContainer> newlist = Collections.emptyIterator();
        try {
            owContainerEnum = adapter.getAllDeviceContainers();
            newlist = owContainerEnum.asIterator();
        } catch (OneWireException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        OneWireContainer02 owc02 = grabFirstContainer02(adapter);

        int buttonSel = -1;
        do {
            System.out.println("Choose device: (does nothing fornow)");
            while (owContainerEnum.hasMoreElements()) {
                OneWireContainer test = owContainerEnum.nextElement();
                System.out.println(test.getAddressAsString());
            }
            while (!CONSOLE.hasNextInt()) {
                System.out.println("Invalid selection");
                CONSOLE.next();
            }
            System.out.println("TODO: Add changebutton logic");
            buttonSel = CONSOLE.nextInt();
        } while (buttonSel < 0);
        System.out.printf(" iButton: %d%n", buttonSel);

        return owc02;
    }

    private static byte[] scanFieldName(String name) {
        System.out.printf("Enter %s (hexString): ", name);
        // TODO: code to interpret string.
        String str = CONSOLE.next();
        byte[] ino = {};
        try {
            ino = Convert.toByteArray(str);
        } catch (ConvertException e) {
            System.out.print("scanFieldName " + e);
            e.printStackTrace();
        }
        return checkforEightBytes(ino);
    }

    private static byte[] checkforEightBytes(byte[] ino) {
        String pad = "        ";
        byte[] eightField = pad.getBytes();

        if (ino.length > 8) {
            // return Arrays.copyOf(original, newLength))
        }
        for (int i = 0; i < ino.length; i++) {
            eightField[i] = ino[i];
        }
        return eightField;
    }

    private static byte[] selectPassword() {
        Long zeroed = 0x0000000000000000L;
        //   Alma Harmony "0x0909240304031901" // MSB string
        Long almaHarmony = 0x0119030403240909L; // LSB string
        Long allU = 0x5555555555555555L;

        System.out.println("0. empty (eight spaces)");
        System.out.println("1. zeroed (00000000)");
        System.out.println("2. init (eight 'U's)");
        System.out.println("3. Alma Harmony code");
        System.out.println("9. enter password");
        System.out.println("Select a code: ");
        byte[] pwd = {};
        int selection = -1;

        do {
            while (!CONSOLE.hasNextInt()) { CONSOLE.next();}
            selection = CONSOLE.nextInt();
        } while (selection < 0);

        switch (selection) {
            case 0:
                break;
            case 1:
                pwd = Convert.toByteArray(zeroed);
                break;
            case 2:
                pwd = Convert.toByteArray(allU);
                break;
            case 3:
                pwd = Convert.toByteArray(almaHarmony);
                break;
            case 9:
                System.out.println("TODO passwrod parsing");
                break;
            default:
                System.out.println("No password input");
                break;
        }
        return pwd;
    }

    private static void viewDS1991(OneWireContainer02 onewirecontainer02) throws Exception {

        // Long allU = 0x5555555555555555L;
        // byte[] Ubytes = Convert.toByteArray(allU);
        // byte[] almaBytes = Convert.toByteArray(almaHarmony);

        System.out.printf("%s%n", onewirecontainer02.getAddressAsString());

        byte[] pwd = selectPassword();

        List<byte[]> subkeyList = getSubkeysList(onewirecontainer02, pwd);
        for (byte[] subkey : subkeyList) {
            displaySubkey(subkey);
            // System.out.println(Convert.toHexString(subkey, " "));
        }
    }

    private static OneWireContainer02 grabFirstContainer02(DSPortAdapter adapter) {
        // find first DS1991 (family code 0x02).
        adapter.targetFamily(0x02);
        OneWireContainer owd = new OneWireContainer();
        try {
            owd = adapter.getFirstDeviceContainer();
        } catch (OneWireException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // if (owd == null || !Address.isValid(owd.getAddress())) {
        //     // System.out.println("No DS1991 devices found!");
        //     OneWireIOException e = new OneWireIOException("No DS1991 devices found!");
        //     throw e;
        // }
        return new OneWireContainer02(adapter, owd.getAddress());
    }

    private static void initiBSV(DSPortAdapter adapter) throws OneWireIOException, OneWireException {
        System.out.println();
        System.out.println("SecureViewer for DS1991 iButton - Java console app");
        System.out.println("AdapterVersion: " + adapter.getAdapterVersion() + "; Port: " + adapter.getPortName()
                + "; canDeliverPower: " + adapter.canDeliverPower() + ", smart power: "
                + adapter.canDeliverSmartPower());

        adapter.setSearchAllDevices();
        adapter.setSpeed(DSPortAdapter.SPEED_REGULAR);

        System.out.println("Detected 1-Wire devices:");
        for (@SuppressWarnings("unchecked")
        Enumeration<OneWireContainer> owd_enum = adapter.getAllDeviceContainers(); owd_enum.hasMoreElements();) {
            OneWireContainer owd = owd_enum.nextElement();

            System.out.printf("%s%n", owd.toString());
        }
        System.out.println();
    }

    private static List<byte[]> getSubkeysList(OneWireContainer02 onewirecontainer02, byte[] pwd) throws Exception {
        List<byte[]> subkeyList = new ArrayList<>(3);
        byte[] buf = new byte[64];
        for (int i = 0; i < 3; i++) {
            buf = getSubkey(onewirecontainer02, i, pwd);
            subkeyList.add(buf);
        }
        return subkeyList;
    }

    private static byte[] getSubkey(OneWireContainer02 onewirecontainer02, int key, byte[] pwd) throws Exception {
        Long defaultPswd = 0x2020202020202020L; // Eight spaces = " "

        // toByteArray constructs a LSByte byte array.
        byte[] passwd = (pwd.length == 0) ? Convert.toByteArray(defaultPswd) : pwd;

        return onewirecontainer02.readSubkey(key, passwd);
    }

    // DS1991 command structure, three bytes: [command|address|inverse address]
    // readSubKey: [0x66|{subkey#,address from 0x10 to 0x3F}|inverse address]
    private static void displaySubkey(byte[] subkey) {
        showSubkeyHeader(subkey);
        printAsBlock(subkey, true);
        // System.out.println();
    }

    private static void printAsBlock(byte[] buf, boolean printAsBlock) {
        String hexString = Convert.toHexString(buf, " ");
        if (printAsBlock) {
            hexString = hexString.replaceAll("(.{24})", "$1\n");
        }
        System.out.println(hexString);
    }

    private static void showSubkeyHeader(byte[] buf) {
        String id = Convert.toHexString(buf, 0, 8);
        String ps = Convert.toHexString(buf, 8, 8);
        String sd = Convert.toHexString(buf, 16, 48);

        System.out.print("ID: 0x" + id + " | " + "transmitted-pw: 0x" + ps + " ");
        System.out.println("[" + hexToISO8859(sd) + "]");
        System.out.println("     '" + hexToISO8859(id) + "' | '" + hexToISO8859(ps) + "'");
    }

    private static byte[] readScratchpad(OneWireContainer02 owc02) throws Exception {
        byte[] scratchpad = new byte[64];

        System.out.println("Scratchpad:");
        scratchpad = owc02.readScratchpad();
        String str = Convert.toHexString(scratchpad, " ");
        System.out.println("[" + hexToISO8859(str) + "]");
        System.out.println(str);

        return scratchpad;
    }

    private static void clearScratchpad(OneWireContainer02 odc) throws OneWireIOException, OneWireException {
        byte buf[] = new byte[64];

        if (!(confirmChoice("Clear Scratchpad?"))) {
            System.out.println("scratchpad not cleared");
            return;
        }
        System.out.println("reInitalizing scratchpad...");
        Arrays.fill(buf, (byte) 'U');
        odc.writeScratchpad(00, buf);
    }

    private static void setDSPortAdapter() {
        try {
            dsportadapter = OneWireAccessProvider.getDefaultAdapter();
        } catch (OneWireException e) {
            System.out.println(e + " adapter unchanged");
            e.printStackTrace();
        }
    }

    private static DSPortAdapter getDSPortAdapter() {
        try {
            while (!dsportadapter.adapterDetected()) {
                setDSPortAdapter();
            }
        } catch (OneWireException e) {
            e.printStackTrace();
        }
        return dsportadapter;
    }

    private static Boolean confirmChoice(String msg) {
        Boolean yn = null;
        System.out.print(msg + " [y/n]: ");
        while (yn == null) {
            char in = CONSOLE.next().charAt(0);
            switch (in) {
                case 'y':
                case 'Y':
                    yn = true;
                    break;
                case 'n':
                case 'N':
                    yn = false;
                    break;
                default:
                    System.out.print("[y/n]?: ");
            }
        }
        return yn;
    }

    // Takes hex coded string and converts printable values to symbols, otherwise convert to dot (.)
    private static String hexToISO8859(String hexStr) {
        StringBuilder output = new StringBuilder("");
        hexStr = hexStr.replaceAll("\\s+", "");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            int thebyte = Integer.parseInt(str, 16);

            if (Character.isISOControl(thebyte)) {
                output.append(".");
            } else {
                output.append((char) thebyte);
            }
        }
        return output.toString();
    }

    private static void pressEnterToContinue() {
        System.out.print("Press Enter to continue...");
        try {
            System.in.read();
        }  
        catch(Exception e) {}  
    }
}
