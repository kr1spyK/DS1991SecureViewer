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
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.OneWireContainer02;
import com.dalsemi.onewire.utils.Address;
import com.dalsemi.onewire.utils.Convert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * We gotta do the thing. The secure iButton thing.
 */

public class iBSV {
    public static void main(String[] args) throws Exception {

        // OneWireContainer owd = null;
        DSPortAdapter adapter;
        // boolean adapterdetected = false;

        try {
            adapter = OneWireAccessProvider.getDefaultAdapter();

            /**
             * SESSION: Negotiate exclusive use of 1-Wire bus
             */
            adapter.beginExclusive(true);

            /**
             * LINK: connect to 1-Wire bus
             *
             * NETWORK: Device discovery and selection
             */
            // print program header, clear previous search restrictions
            initiBSV(adapter);

            OneWireContainer02 onewirecontainer02 = grabFirstContainer02(adapter);
            System.out.printf("=== %s ===%n%s%n", onewirecontainer02.getName(), onewirecontainer02.getDescription());

            /**
             * TRANSPORT: DS1991 only supports primative memory functions and some unique
             * commands
             */
            dumpDS1991(onewirecontainer02);

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

    private static void dumpDS1991(OneWireContainer02 onewirecontainer02) throws Exception {
        
        System.out.printf("=== %nworking with: %s%n", onewirecontainer02.getAddressAsString());

        byte scratchpadBuffer[] = new byte[64];

        System.out.println("Read Scratchpad:");
        scratchpadBuffer = onewirecontainer02.readScratchpad();
        String str = Convert.toHexString(scratchpadBuffer, " ");
        System.out.println("[" + hexToAscii(str) + "]");
        System.out.println(str);

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

    // DS1991 command structure, three bytes: [command|address|inverse address]
    // readSubKey: [0x66|{subkey#,address from 0x10 to 0x3F}|inverse address]
    private static List<Byte[]> getSubkeys(OneWireContainer02 onewirecontainer02) throws Exception {
        List<Byte[]> idList = new ArrayList<>(3);

        System.out.println("read SubKeys:");
        for (int i = 0; i < 3; i++) {
            displaySubkeys(onewirecontainer02, i);
        }

        return idList;
    }

    private static void displaySubkeys(OneWireContainer02 onewirecontainer02, int key) throws Exception {
        byte[] buf = new byte[64];
            String defaultPW = "UUUUUUUU"; // hardcoded default password
            Long alma = Long.decode("0x0909240304031901");
            // System.out.println(alma);
            onewirecontainer02.readSubkey(buf, key, Convert.toByteArray(alma));
            String hexStr = Convert.toHexString(buf, " ");
            String printStr = hexToAscii(hexStr);
            // getSubkeyheader(buf);
            // System.out.println("ID: '" + printStr.substring(0, 8) + "' " + "transmitted-pw: " + "");
            // System.out.println("[" + printStr.substring(16, printStr.length()) + "]");
            // System.out.println("[" + hexStr + "]");
            System.out.println(hexStr);
    }

    private static void getSubkeyheader(byte[] buf) {
        for(int i = 0; i < 8; i++) {
            System.out.print(buf[i]);
        }
        System.out.println();
    }

    private static void clearScratchpad(OneWireContainer02 odc)
            throws OneWireIOException, OneWireException {
        byte buf[] = new byte[64];

        System.out.println("Clearing scratchpad...");
        Arrays.fill(buf, (byte) 'U');
        odc.writeScratchpad(00, buf);
    }

    // Takes hex coded string and converts printable values to symbols.
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
                output.append(str);
            } else {
                output.append((char) thebyte);
            }
            output.append(delimiter);
	    }
	    return output.toString();
	}
}
