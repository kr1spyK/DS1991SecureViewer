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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

/**
 * We gotta do the thing. The secure iButton thing.
 */

public class iBSV {

    static Scanner CONSOLE = new Scanner(System.in);
    DSPortAdapter dsportadapter;
    public static void main(String[] args) throws Exception {

        DSPortAdapter adapter;

        try {
            adapter = OneWireAccessProvider.getDefaultAdapter();

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
             * TRANSPORT: DS1991 only supports primative memory functions and some unique commands
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
        System.out.println("3. read subkey");
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
         * Check if class adapter matches container field, otherwise do
         * noresetsearch on (currentadapter) and update container if located.
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
                    System.out.println("TODO: change button");
                    break;
                case '2': // Read scratchpad contents.
                    System.out.println("Reading scratchpad...");
                    readScratchpad(currentiButton);
                    pressEnterToContinue();
                    break;
                case '3': // Reads one subkey.
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

        // mainmenuOptions();

        do {
            // currentiButton = new OneWireContainer02((DSPortAdapter) null, "69420420CBDEFF02");
            System.out.printf("%ncurrent iButton: %s%n", currentiButton.getAddressAsString());
            mainmenuOptions();
            System.out.print("Select option: ");
            menuItem = CONSOLE.next().charAt(0);
            switch (menuItem) {
                case '1': 
                    chooseDS1991();
                    break;
                case '2':
                    System.out.println("TODO: interface for password checking");
                    pressEnterToContinue();
                    viewDS1991(currentiButton);
                    pressEnterToContinue();
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

    private static OneWireContainer02 chooseDS1991() {
        /**
         * Get current adapter, display DS1991 devices, return container
         */
        OneWireContainer02 owc02 = new OneWireContainer02();
        int buttonSel = -1;
        do {
            System.out.println("Choose device: (does nothing fornow)");
            while (!CONSOLE.hasNextInt()) {
                System.out.println("Invalid selection");
                CONSOLE.next();
            }
            buttonSel = CONSOLE.nextInt();
        } while (buttonSel < 0);
        System.out.printf(" iButton: %d%n", buttonSel);

        return owc02;
    }

    private static void viewDS1991(OneWireContainer02 onewirecontainer02) throws Exception {

        System.out.printf(" %s%n", onewirecontainer02.getAddressAsString());

        byte scratchpadBuffer[] = new byte[64];

        scratchpadBuffer = readScratchpad(onewirecontainer02);
        // System.out.println(Convert.toHexString(scratchpadBuffer[1]));

        getSubkeys(onewirecontainer02);
    }

    private static OneWireContainer02 grabFirstContainer02(DSPortAdapter adapter) throws Exception {

        // find first DS1991 (family code 0x02).
        adapter.targetFamily(Convert.toInt("02"));
        OneWireContainer owd = adapter.getFirstDeviceContainer();
        if (owd == null || !Address.isValid(owd.getAddress())) {
            // System.out.println("No DS1991 devices found!");
            OneWireIOException e = new OneWireIOException("No DS1991 devices found!");
            throw e;
        }

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

    private static List<Byte[]> getSubkeys(OneWireContainer02 onewirecontainer02) throws Exception {
        List<Byte[]> idList = new ArrayList<>(3);

        System.out.println("SubKeys:");
        for (int i = 0; i < 3; i++) {
            displaySubkey(onewirecontainer02, i);
        }

        return idList;
    }

    private static byte[] parsePassword(String str) {
        return str.getBytes();
    }

    // DS1991 command structure, three bytes: [command|address|inverse address]
    // readSubKey: [0x66|{subkey#,address from 0x10 to 0x3F}|inverse address]
    private static void displaySubkey(OneWireContainer02 onewirecontainer02, int key) throws Exception {
        byte[] buf = new byte[64];
        String defaultPW = "UUUUUUUU"; // hardcoded default password
        String pass = "0x0909240304031901";
        String passi = "0x0119030403240909";

        Long almaHarmony = Long.decode(passi);
        defaultPW = Convert.toHexString(defaultPW.getBytes());
        // pawg = toByteArray(defaultPW or alma)
        byte[] pawg = Convert.toByteArray(almaHarmony);
        onewirecontainer02.readSubkey(buf, key, pawg);

        String hexStr = Convert.toHexString(buf, " ");
        // String printStr = hexToAscii(hexStr);

        getSubkeyheader(Convert.toHexString(buf));
        printResponse(hexStr, 1);
    }

    private static void printResponse(String hexString, int printAsBlock) {
        if (printAsBlock != 0) {
            hexString = hexString.replaceAll("(.{24})", "$1\n");
        }
        System.out.printf("%s%n%n", hexString);
    }

    private static void getSubkeyheader(String str) {
        System.out.print("ID: 0x" + str.substring(0, 16) + " | " + "transmitted-pw: 0x" + str.substring(16, 32) + " ");
        System.out.println("[" + hexToAscii(str.substring(32)) + "]");
        System.out.println("     '" + hexToAscii(str.substring(0, 16)) + "' | " + hexToAscii(str.substring(16, 32)));
    }

    private static byte[] readScratchpad(OneWireContainer02 owc02) throws Exception {
        byte[] scratchpad = new byte[64];

        System.out.println("Scratchpad:");
        scratchpad = owc02.readScratchpad();
        String str = Convert.toHexString(scratchpad, " ");
        System.out.println("[" + hexToAscii(str) + "]");
        System.out.println(str);

        return scratchpad;
    }

    private static void clearScratchpad(OneWireContainer02 odc) throws OneWireIOException, OneWireException {
        byte buf[] = new byte[64];

        System.out.println("Clearing scratchpad...");
        Arrays.fill(buf, (byte) 'U');
        odc.writeScratchpad(00, buf);
    }

    // Takes hex coded string and converts printable values to symbols, otherwise
    // keep hex value.
    private static String hexToAscii(String hexStr) {
        return hexToAscii(hexStr, "");
    }

    private static String hexToAscii(String hexStr, String delimiter) {
        StringBuilder output = new StringBuilder("");
        hexStr = hexStr.replaceAll("\\s+", "");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            int thebyte = Integer.parseInt(str, 16);

            if (Character.isISOControl((char) thebyte)) {
                // output.append(" ");
                output.append(".");
            } else if (Character.isWhitespace((char) thebyte)) {
                output.append(" ");
            } else {
                output.append((char) thebyte);
                // output.append(" ");
            }
            output.append(delimiter);
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
