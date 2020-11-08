/*---------------------------------------------------------------------------
 * Copyright (C) 2010 Maxim Integrated Products, All Rights Reserved.
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

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.PasswordContainer;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.application.tag.TaggedDevice;
import com.dalsemi.onewire.utils.*;

/**
 * A Password Viewer for integration in the OneWireViewer.
 * The DS1977, DS1922, DS1923, DS2422 and all devices that implement <code>PasswordContainer</code> 
 * are supported by this viewer.  This viewer provides an interface for password management.  Please 
 * note that this viewer does not support the DS1991 or DS1425 password-protected memory devices.
 *
 * @author Maxim
 * @version 1.01
 */
public class PasswordViewer extends Viewer
   implements Pollable, Runnable
{
   /* string constants */
   private static final String strTitle = "PasswordViewer";
   private static final String strTab = "Password";
   private static final String strTip = "Sets passwords for password-protected memory device";
   private static final String naString = "N/A";

   /* container variables */
   private PasswordContainer container  = null;

   private boolean setSoftwarePasswordsClicked = false;
   private boolean setDevicePasswordsClicked = false;
   private boolean setPasswordEnableClicked = false;
   private boolean isFirstTimeRun = false;

   /* feature labels */
   private JLabel[] lblFeature = null, lblFeatureHdr = null;

   private String[] strHeader = { "Has Read-Only Password? ",
                                  "Has Full-Access Password? ",
                                  "Is Software Read-Only Password Set? ",
                                  "Is Software Full-Access Password Set? "};

   /* indices for feature labels */
   private static final int TOTAL_FEATURES = 4;
   private static final int HAS_READ_ONLY_PASSWORD = 0;
   private static final int HAS_FULL_ACCESS_PASSWORD = 1;
   private static final int IS_READ_ONLY_PASSWORD_SET = 2;
   private static final int IS_FULL_ACCESS_PASSWORD_SET = 3;


   /* buttons */

   /**
    * Creates a new PasswordViewer.
    */
   public PasswordViewer()
   {
      super(strTitle);

      // set the version
      majorVersionNumber = 1;
      minorVersionNumber = 1;

      //
      // Info panel
      //

      JPanel featurePanel = new JPanel(new GridLayout(TOTAL_FEATURES, 2, 3, 3));
      JScrollPane featureScroll = new JScrollPane(featurePanel,
                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      featureScroll.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createEtchedBorder(), "Info"));

      lblFeatureHdr = new JLabel[TOTAL_FEATURES];
      lblFeature = new JLabel[TOTAL_FEATURES];
      for (int i = 0; i < TOTAL_FEATURES; i++)
      {
         lblFeatureHdr[i] = new JLabel(strHeader[i], JLabel.RIGHT);
         lblFeatureHdr[i].setOpaque(true);
         lblFeatureHdr[i].setFont(fontBold);
         lblFeatureHdr[i].setForeground(Color.black);
         lblFeatureHdr[i].setBackground(Color.lightGray);

         lblFeature[i] = new JLabel("", JLabel.LEFT);
         lblFeature[i].setOpaque(true);
         lblFeature[i].setFont(fontPlain);
         lblFeature[i].setForeground(Color.black);
         lblFeature[i].setBackground(Color.lightGray);

         featurePanel.add(lblFeatureHdr[i]);
         featurePanel.add(lblFeature[i]);
      }
      add(featureScroll, BorderLayout.NORTH);
     
      //
      // Config panel
      //
      
      JPanel configPanel = new JPanel(new FlowLayout());
      configPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createEtchedBorder(), "Config"));
      
      // Set Software Passwords button
      JButton setSoftwarePasswords = new JButton("Set Software Passwords");
      setSoftwarePasswords.addActionListener( new ActionListener()
         {
            public void actionPerformed(ActionEvent ae)
            {
               setSoftwarePasswordsClicked = true;
            }
         }
      );
      configPanel.add(setSoftwarePasswords);

      
      // Set Device Password
      JButton setDevicePasswords = new JButton("Set Device Passwords");
      setDevicePasswords.addActionListener( new ActionListener()
         {
            public void actionPerformed(ActionEvent ae)
            {
               setDevicePasswordsClicked = true;
            }
         }
      );
      configPanel.add(setDevicePasswords);

      // Set Password Enable
      JButton setPasswordEnable = new JButton("Enable/Disable Passwords on Device");
      setPasswordEnable.addActionListener( new ActionListener()
         {
            public void actionPerformed(ActionEvent ae)
            {
               setPasswordEnableClicked = true;
            }
         }
      );
      configPanel.add(setPasswordEnable);

      add(configPanel, BorderLayout.CENTER);

      clearContainer();
   }

   /**
    * Checks if this viewer supports the supplied TaggedDevice.
    *
    * @param td - TaggedDevice to check for viewer support.
    *
    * @return 'true' if this viewer supports the provided
    *   TaggedDevice.
    */
   public boolean containerSupported(TaggedDevice td)
   {
      // not yet supported as a tagged device
      return false;
   }

   /**
    * Sets the container by providing a TaggedDevice.
    *
    * @param td TaggedDevice representing this device
    */
   public void setContainer(TaggedDevice td)
   {
      // not yet supported as a tagged device
      return;
   }

   /**
    * Checks if this viewer supports the supplied container.
    *
    * @param owc - container to check for viewer support.
    *
    * @return 'true' if this viewer supports the provided
    *   container.
    */
   public boolean containerSupported(OneWireContainer owc)
   {
      return (owc instanceof PasswordContainer);
   }

   /**
    * Sets the container for this viewer.
    *
    * @param owc OneWireContainer of this viewer
    */
   public void setContainer(OneWireContainer owc)
   {
      // ensure that the container was cleared previously
      if(this.adapter!=null || this.container!=null || this.romID!=null)
         clearContainer();

      if(owc!=null)
      {
         synchronized(syncObj)
         {
            this.adapter = owc.getAdapter();
            this.container = (PasswordContainer)owc;
            this.romID = owc.getAddressAsString();
            this.setSoftwarePasswordsClicked = false;
            this.setDevicePasswordsClicked = false;
            this.setPasswordEnableClicked = false;
            this.isFirstTimeRun = true;
         }
         setStatus(VERBOSE, "Performing first read. . .");
      }
   }

   /**
    * Clears the reference to the device container.
    */
   public void clearContainer()
   {
      synchronized(syncObj)
      {
         this.adapter = null;
         this.container = null;
         this.romID = null;
         this.setSoftwarePasswordsClicked = false;
         this.setDevicePasswordsClicked = false;
         this.setPasswordEnableClicked = false;           
      }
      setStatus(VERBOSE, "No Device");
   }

   /**
    * Returns <code>true</code> if Viewer still has pending tasks it must
    * complete.
    *
    * @return <code>true</code> if Viewer still has pending tasks it must
    * complete.
    */
    public boolean isBusy()
    {
       //return hasRunTasks();
       // should never have any tasks enqueued, but not serviced
       return false;
    }

   /**
    * 'run' method that is called continuously to service
    * GUI interaction, such as button click events.  Also
    * performs one-time setup of viewer.
    */
   public void run()
   {
      DSPortAdapter l_adapter = null;
      PasswordContainer l_container = null;
      String l_romID = null;
      synchronized(syncObj)
      {
         l_adapter = this.adapter;
         l_container = this.container;
         l_romID = this.romID;
      }
      if (l_container.hasReadOnlyPassword())
         lblFeature[HAS_READ_ONLY_PASSWORD].setText("TRUE");
      else
         lblFeature[HAS_READ_ONLY_PASSWORD].setText("FALSE");

      if (l_container.hasReadWritePassword())
         lblFeature[HAS_FULL_ACCESS_PASSWORD].setText("TRUE");
      else
         lblFeature[HAS_FULL_ACCESS_PASSWORD].setText("FALSE");

      try
      {
         if (l_container.isContainerReadOnlyPasswordSet())
            lblFeature[IS_READ_ONLY_PASSWORD_SET].setText("TRUE");
         else
            lblFeature[IS_READ_ONLY_PASSWORD_SET].setText("FALSE");

         if (l_container.isContainerReadWritePasswordSet())
            lblFeature[IS_FULL_ACCESS_PASSWORD_SET].setText("TRUE");
         else
            lblFeature[IS_FULL_ACCESS_PASSWORD_SET].setText("FALSE");

      }
      catch (com.dalsemi.onewire.OneWireException owe)
      {
         setStatus(ERRMSG, "Error reading from device: " + owe);
      }

      if(l_adapter!=null)
      {
         if (this.setSoftwarePasswordsClicked)
         {
            setStatus(VERBOSE, "Setting Software Passwords...");
            try
            {
               l_adapter.beginExclusive(true);

               StringBuffer strReadOnlyPassword = new StringBuffer("");
               StringBuffer strFullAccessPassword = new StringBuffer("");

               getPasswordsFromUser(strReadOnlyPassword, l_container.getReadOnlyPasswordLength(), 
                  strFullAccessPassword, l_container.getReadWritePasswordLength());

               byte[] readPassword = null;
               byte[] fullAccessPassword = null;

               readPassword = convertPasswordStringToByteArray(strReadOnlyPassword.toString(), l_container.getReadOnlyPasswordLength());
               fullAccessPassword = convertPasswordStringToByteArray(strFullAccessPassword.toString(), l_container.getReadWritePasswordLength());

               //
               // Copy passwords to the container
               //

               if (this.setSoftwarePasswordsClicked)
               {

                  if (strReadOnlyPassword.length() > 0)
                  {
                     l_container.setContainerReadOnlyPassword(readPassword, 0);
                  }
                  if (strFullAccessPassword.length() > 0)
                  {
                     //JOptionPane.showMessageDialog(null, "full access password = " + strFullAccessPassword);
                     l_container.setContainerReadWritePassword(fullAccessPassword, 0);
                  }
                     
                  setStatus(VERBOSE, "Done setting Software passwords.");

               }
            }
            catch (Exception e)
            {
               setStatus(ERROR, "Error reading device! " + e.toString());
               
            }
            finally
            {
               l_adapter.endExclusive();
               this.setSoftwarePasswordsClicked = false;
            }
         }

         else if (this.setDevicePasswordsClicked)
         {
            setStatus(VERBOSE, "Prompting for passwords to write to device...");
            try
            {
               l_adapter.beginExclusive(true);

               StringBuffer strReadOnlyPassword = new StringBuffer("");
               StringBuffer strFullAccessPassword = new StringBuffer("");

               getPasswordsFromUser(strReadOnlyPassword, l_container.getReadOnlyPasswordLength(),
                  strFullAccessPassword, l_container.getReadWritePasswordLength());


               byte[] readPassword = null;
               byte[] fullAccessPassword = null;

               readPassword = convertPasswordStringToByteArray(strReadOnlyPassword.toString(), l_container.getReadOnlyPasswordLength());
               fullAccessPassword = convertPasswordStringToByteArray(strFullAccessPassword.toString(), l_container.getReadWritePasswordLength());


               //
               // Write passwords directly to the part
               //

               if (strReadOnlyPassword.length() > 0)
               {
                  l_container.setDeviceReadOnlyPassword(readPassword, 0);
               }
               if (strFullAccessPassword.length() > 0)
               {
                  l_container.setDeviceReadWritePassword(fullAccessPassword, 0);
               }

               setStatus(VERBOSE, "Done setting Device passwords.");

            }
            catch (Exception e)
            {
               setStatus(ERROR, "Error writing to device! " + e.toString());
               JOptionPane.showMessageDialog(null, "Error writing to device! " + e.toString());
            }
            finally
            {
               l_adapter.endExclusive();
               this.setDevicePasswordsClicked = false;
            }
         }

         else if (this.setPasswordEnableClicked)
         {
            setStatus(VERBOSE, "Enabling Passwords...");
            try
            {
               int i = JOptionPane.showConfirmDialog(PasswordViewer.this,
                   "Click 'Yes' to Enable Passwords, 'No' to Disable Passwords");
               if (i == JOptionPane.CANCEL_OPTION)
               {
                  l_adapter.beginExclusive(true);
                  setStatus(VERBOSE, "Cancelled enabling/disabling passwords.");
               }
               else if (i == JOptionPane.YES_OPTION)
               {
                  l_adapter.beginExclusive(true);
                  if (l_container.isContainerReadOnlyPasswordSet() && l_container.isContainerReadWritePasswordSet())
                  {
                     l_container.setDevicePasswordEnableAll(true);
                     setStatus(VERBOSE, "Done enabling passwords.");
                  }
                  else
                  {
                     JOptionPane.showMessageDialog(PasswordViewer.this, "Set Container Password(s) first!", "alert", JOptionPane.ERROR_MESSAGE);
                  }
               }
               else if (i == JOptionPane.NO_OPTION)
               {
                  l_adapter.beginExclusive(true);
                  if (l_container.isContainerReadOnlyPasswordSet() && l_container.isContainerReadWritePasswordSet())
                  {
                     l_container.setDevicePasswordEnableAll(false);
                     setStatus(VERBOSE, "Done disabling passwords.");
                  }
                  else
                  {
                     JOptionPane.showMessageDialog(PasswordViewer.this, "Set Container Password(s) first!", "alert", JOptionPane.ERROR_MESSAGE);
                  }
               }
            }
            catch (Exception e)
            {
               setStatus(ERROR, "Error enabling/disabling passwords! " + e.toString());
               JOptionPane.showMessageDialog(PasswordViewer.this, e.toString(), "alert", JOptionPane.ERROR_MESSAGE);
            }
            finally
            {
               this.setPasswordEnableClicked = false;
               l_adapter.endExclusive();
            }
         }         
      }
      if (isFirstTimeRun)
      {
         isFirstTimeRun = false;
         setStatus(VERBOSE, "Done Setting up viewer");
      }

      // sleep (probably not necessary)
      try
      {
         Thread.sleep(5);
      }
      catch (Exception e)
      {
         setStatus(ERRMSG, "Error during run loop. " + e);
      }

   }

   /**
    * 'convertPasswordStringToByteArray' method is called when retrieving passwords from a user.
    * Returns a vector
    */
   public byte[] convertPasswordStringToByteArray(String strPassword, int passwordLength)
   {
      byte[] bytePassword = new byte[passwordLength];
      if(strPassword!=null && strPassword.length()>0)
      {
         try
         {
            bytePassword = Convert.toByteArray(strPassword);
         }
         catch(com.dalsemi.onewire.utils.Convert.ConvertException ce)
         {
            setStatus(ERRMSG, "Error during hex string conversion: " + ce);
         }
         return bytePassword;
      }
      return null;
   }

   /**
    * 'getPasswordsFromUser' method is called when retrieving passwords from a user.
    * Returns the passwords in the same StringBuffers that were input parameters.  
    * They will be empty if the user clicked "cancel" or is not the correct
    * number of characters for the string.
    */
   public void getPasswordsFromUser(StringBuffer readPassword, int readPasswordLength, StringBuffer writePassword, int writePasswordLength)
   {
      //
      // Pop up an input dialog window to retrieve the 
      // container read-only password from user
      //
      String strReadPassword = JOptionPane.showInputDialog(PasswordViewer.this,
                                       "Read-Only Password -- enter 8 bytes in hexadecimal with spaces between bytes");

      // let's trim the received string and make sure it is good
      if (strReadPassword != null)
      {
         strReadPassword = strReadPassword.trim(); // delete white space from beginning and end of string
         if (strReadPassword.length() != readPasswordLength * 3 - 1) 
         {
            //readPassword.delete(0, readPassword.length() - 1);
            readPassword = null;  // if password not exactly 23 spaces (equivalent to 8 bytes), then set the buffer to null
         }
         else
         {
            readPassword.append(strReadPassword);  // put the password in the string buffer to return
         }
      }

      //
      // Pop up an input dialog window to retrieve the 
      // container full-access password from user
      //

      String strWritePassword = JOptionPane.showInputDialog(PasswordViewer.this,
                                      "Full Access Password -- enter 8 bytes in hexadecimal with spaces between bytes");

      // let's trim the received string and make sure it is good
      if (strWritePassword != null)
      {
         strWritePassword = strWritePassword.trim(); // delete white space from beginning and end of string

         if (strWritePassword.length() != writePasswordLength * 3 - 1) // if password not exactly 23 spaces (equivalent to 8 bytes), then set the buffer to null
         {
            writePassword = null;
         }
         else
         {
            writePassword.append(strWritePassword);  // put the password in the string buffer to return
         }
      }
   }

   /**
    * 'poll' method that is called at the current polling rate.
    * Read and update the display of the device status.
    */

   public void poll()
   {
      // sleep (probably not necessary)
      synchronized (syncObj)
      {
         try
         {
            Thread.sleep(10);
         }
         catch (Exception e)
         {
            setStatus(ERRMSG, "Error during polling. " + e);
         }
      }
   }

   
   /**
    * Gets the string that represents this Viewer's title
    *
    * @return viewer's title
    */
   public String getViewerTitle()
   {
      return strTab;
   }

   /**
    * Gets the string that represents this Viewer's description
    *
    * @return viewer's description
    */
   public String getViewerDescription()
   {
      return strTip;
   }

   /**
    * Create a complete clone of this viewer, including reference to
    * current container.  Used to display viewer in new window.
    */
   public Viewer cloneViewer()
   {
      PasswordViewer cv = new PasswordViewer();
      cv.setContainer((OneWireContainer)this.container);
      return cv;
   }
}