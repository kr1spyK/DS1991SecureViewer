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
import com.dalsemi.onewire.utils.Convert;

import java.util.Arrays;
import java.util.Enumeration;

/**
 * We gotta do the thing. The secure iButton thing.
 */ 

public class iBSV {
    public static void main(String[] args) throws Exception {

        OneWireContainer owd;
        DSPortAdapter adapter;
        boolean adapterdetected = false;
        
    while (!adapterdetected) {
        try {
            // get the default adapter
            adapter = OneWireAccessProvider.getDefaultAdapter();
            adapterdetected = adapter.adapterDetected();

            System.out.println();
            System.out.println("SecureViewer for DS1991 iButton - Java console app");
            System.out.println("AdapterVersion: " + adapter.getAdapterVersion()
                            + "; Port: " + adapter.getPortName());
            
            System.out.println("canDeliverPower: "
                            + adapter.canDeliverPower()
                            + ", smart power: "
                            + adapter.canDeliverSmartPower());

            /**
             * SESSION: Negotiate exclusive use of 1-Wire bus
             */
            adapter.beginExclusive(true);
            
            /**
             * LINK: connect to 1-Wire bus 
             */
            // clear previous search restrictions
            adapter.setSearchAllDevices();
            adapter.targetAllFamilies();
            adapter.setSpeed(DSPortAdapter.SPEED_REGULAR);

            /**
             * NETWORK: Device discovery and selection
             */
            System.out.println();
            System.out.println("Detected 1-Wire devices:");
            for (@SuppressWarnings("unchecked")
                Enumeration<OneWireContainer> owd_enum = adapter.getAllDeviceContainers();
                        owd_enum.hasMoreElements(); ) {
                owd = owd_enum.nextElement();

                System.out.printf("%s%n", owd.toString());
            }

            //     DS1991 MultiKey, family code 02 (hex).
            adapter.targetFamily(Convert.toInt("02"));
            // get first DS1991 that we see, for now.
            owd = adapter.getFirstDeviceContainer();
            Long owdAddress = owd.getAddressAsLong();
            OneWireContainer02 onewirecontainer02 = new OneWireContainer02(adapter, owd.getAddressAsLong()); 

            System.out.printf("=== %s ===%n%s%n", owd.getName(), owd.getDescription());
            System.out.printf("=== %n working with: %s%n", owd.getAddressAsString());

            byte scratchpad[] = new byte[64];

            // TODO: Create clear scratchpad method
            System.out.println("Clearing scratchpad...");
            Arrays.fill(scratchpad, (byte) 0x55);
            onewirecontainer02.writeScratchpad(00, scratchpad);

            System.out.println("Read Scratchpad:");
            System.out.println(Convert.toHexString(onewirecontainer02.readScratchpad(), " "));
            System.out.println(onewirecontainer02.readScratchpad());

            /**
             * TRANSPORT: DS1991 only supports primative memory functions and some
             * unique commands
             */
            // Writing scratchpad takes int starting address (0x00 to 0x3F) & byte[] data.
            String strData = "The quest for hot dogs is on";
            /* byte[] */ scratchpad = strData.getBytes();
            onewirecontainer02.writeScratchpad(Convert.toInt("00"), scratchpad);
            System.out.println("writing message to scratchpad...");
            System.out.println(Convert.toHexString(onewirecontainer02.readScratchpad(), " "));

            // unblock adapter and free the port
            adapter.endExclusive();
            adapter.freePort();
            
            //Testing program running 
            break;
        }
        catch (OneWireIOException e) {
            System.out.println(e + " Adapter communication failure");
            continue;                
        }
        catch (OneWireException e) {
            System.out.println(e + " can't find Adapter/Port.");
            return;
        }
    }
    }
}
