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
                
                System.out.println("== Adapter support power: "
                                + adapter.canDeliverPower());
                System.out.println("== Adapter support smart power: "
                                + adapter.canDeliverSmartPower());

                // block adapter from other programs and threads
                adapter.beginExclusive(true);

                System.out.println("Detected 1-Wire devices:");
                for (@SuppressWarnings("unchecked")
                    Enumeration<OneWireContainer> owd_enum = adapter.getAllDeviceContainers();
                            owd_enum.hasMoreElements(); ) {
                    owd = owd_enum.nextElement();

                    System.out.printf("%-8s %n", owd.getAddressAsString());
                }

                /**
                 * TRANSPORT: DS1991 only supports primative memory functions and some
                 * unique commands
                 */

                // unblock adapter and free the port
                adapter.endExclusive();
                adapter.freePort();
                
                //Testing program running 
                break;
            }
            catch (OneWireIOException e) {
                System.out.println(e + "%nAdapter communication failure");
                continue;                
            }
            catch (OneWireException e) {
                System.out.println(e + "%nAdapter/Port not available.");
                continue;
            }
        }
    }
}
