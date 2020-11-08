/*---------------------------------------------------------------------------
 * Copyright (C) 2001 - 2011 Maxim Integrated Products, All Rights Reserved.
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

import java.util.*;
import javax.swing.*;
import javax.swing.SwingConstants;
import java.awt.event.*;
import java.awt.*;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.MissionContainer;
import com.dalsemi.onewire.container.HumidityContainer;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.application.tag.TaggedDevice;
import com.dalsemi.onewire.utils.Convert;
import com.dalsemi.onewire.container.OneWireContainer53;


/**
 * A Thermochron (DS1925) Viewer for integration in the OneWireViewer.
 * All Thermochron (DS1925) devices are supported by this viewer.  This
 * viewer displays the current mission information as well as the temperature
 * log.
 *
 * @author DS
 * @version 2.3
 */
public class MissionDS1925Viewer extends Viewer
   implements Pollable, Runnable
{
   /* string constants */
   private static final String strTitle = "MissionDS1925Viewer";
   private static final String strTab = "DS1925 Mission";
   private static final String strTip = "Inits a missionable device or displays mission status/results";
   private static final String naString = "N/A";

   public static final String MISSION_VIEWER_FAHRENHEIT = "mission.viewer.displayFahrenheit";
   public static final String MISSION_VIEWER_TCOMPENSATION = "mission.viewer.rhTemperatureCompensation";
   public static final String MISSION_VIEWER_LIMITSATDRIFT = "mission.viewer.rhLimitSaturationDrift";

   /* container variables */
   private MissionContainer container = null;
   private TaggedDevice taggedDevice = null;

   private JButton[] cmdButton;
   private static final int TOTAL_BUTTONS = 5;
   private static final int REFRESH_BTN = 0;
   private static final int START_BTN = 1;
   private static final int STOP_BTN = 2;
   private static final int LAST_BTN = 3;
   private static final int CLEAR_BTN = 4;

   /* visual components */
   public Plot temperaturePlot = null, dataPlot = null;
   private JTabbedPane tabbedResultsPane = null;
   private JScrollPane featureScroll = null,
                       temperatureScroll = null,
                       dataScroll = null;
   //private JTextArea alarmHistory = null;
   //private JTextArea histogram = null;

	private JCheckBox rhTempCompCheck = null; 
	private JCheckBox rhLimitSatDriftCheck = null;
	
	
   /* feature labels */
   private JLabel[] lblFeature = null, lblFeatureHdr = null;
   private String[] strHeader = {
      "Mission in Progress? ", "SUTA Mission? ", "Waiting for Temperature Alarm? ",
      "Sample Rate: ", "Mission Start Time: ", "Mission Sample Count: ",
      "Battery Fail Condition: ", "First Sample Timestamp: ",
      "Total Mission Samples: ", "Total Device Samples: ",
      "Temperature Logging: ", "Temperature High Alarm: ", "Temperature Low Alarm: ",
      "Data Logging: ", "Data High Alarm: ", "Data Low Alarm: "

   };
   /* indices for feature labels */

   private static final int TOTAL_FEATURES = 16;
   private static final int IS_ACTIVE = 0;
   private static final int MISSION_SUTA = 1;
   private static final int MISSION_WFTA = 2;
   private static final int SAMPLE_RATE = 3;
   private static final int MISSION_START = 4;
   private static final int MISSION_SAMPLES = 5;
   private static final int BATTERY_STATE = 6;
   private static final int FIRST_SAMPLE_TIMESTAMP = 7;
   private static final int TOTAL_SAMPLES = 8;
   private static final int DEVICE_SAMPLES = 9;
   private static final int TEMP_LOGGING = 10;
   private static final int TEMP_HIGH_ALARM = 11;
   private static final int TEMP_LOW_ALARM = 12;
   private static final int DATA_LOGGING = 13;
   private static final int DATA_HIGH_ALARM = 14;
   private static final int DATA_LOW_ALARM = 15;

   private volatile boolean pausePoll = false, pollRunning = false;
   private boolean bFahrenheit = false;
	private boolean bTCompensation = false;
	private boolean bLimitSatDrift = false;

   private static final java.text.NumberFormat nf =
      new java.text.DecimalFormat();
   private static final java.text.DateFormat df =
      java.text.DateFormat.getDateTimeInstance(
         java.text.DateFormat.SHORT, java.text.DateFormat.MEDIUM);

   /* single-instance Viewer Tasks */
   private final SetupViewerTask setupViewerTask = new SetupViewerTask();

   private String[] channelLabel = null;
   private boolean[] hasResolution = null;
   private double[][] channelResolution = null;
   
   private boolean forceIgnoreBackup = false;

   public MissionDS1925Viewer()
   {
      super(strTitle);

      bFahrenheit = ViewerProperties.getPropertyBoolean(
         MISSION_VIEWER_FAHRENHEIT, false);
      bTCompensation = ViewerProperties.getPropertyBoolean(
         MISSION_VIEWER_TCOMPENSATION, false);
      bLimitSatDrift = ViewerProperties.getPropertyBoolean(
         MISSION_VIEWER_LIMITSATDRIFT, false);
			
      // set the version
      majorVersionNumber = 2;
      minorVersionNumber = 3;	  

      nf.setMaximumFractionDigits(3);
      nf.setGroupingUsed(false);

      // tabbed results pane
      // This pane consists of the status panel, the temperature panel,
      // the histogram panel, and the alarm history panel.
      tabbedResultsPane = new JTabbedPane(SwingConstants.TOP);
         // feature panel
         JPanel featurePanel = new JPanel(new GridLayout(TOTAL_FEATURES, 2, 3, 3));
         featureScroll = new JScrollPane(featurePanel,
                          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            lblFeatureHdr = new JLabel[TOTAL_FEATURES];
            lblFeature = new JLabel[TOTAL_FEATURES];
            for(int i=0; i<TOTAL_FEATURES; i++)
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

         // Temperature panel
         JPanel dataPanel = new JPanel(new BorderLayout());
         dataScroll = new JScrollPane(dataPanel,
                                      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            dataPlot = new Plot(32,3920*32);  // set with maximum DS1925 log pages 3920
            dataPlot.setScale(30, 65);
         dataPanel.add(dataPlot, BorderLayout.CENTER);
         // Temperature panel
         JPanel temperaturePanel = new JPanel(new BorderLayout());
         temperatureScroll = new JScrollPane(temperaturePanel,
                                       JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            temperaturePlot = new Plot(32,3920*32);  // set with maximum DS1925 log pages 3920  
            temperaturePlot.setScale(60, 85);
         temperaturePanel.add(temperaturePlot, BorderLayout.CENTER);

      cmdButton = new JButton[TOTAL_BUTTONS];
      // Refresh Panel for Refresh Button.
		JPanel commandPanel = new JPanel();
      commandPanel.setLayout(new GridBagLayout());		
      GridBagConstraints c = new GridBagConstraints();	
		
      JScrollPane commandScroll = new JScrollPane(commandPanel);
         JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
         buttonPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createEtchedBorder(), "Command Buttons"));
			

            cmdButton[REFRESH_BTN] = new JButton("Refresh Mission Results");
            cmdButton[REFRESH_BTN].addActionListener(new ActionListener()
               {
                  public void actionPerformed(ActionEvent ae)
                  {
                     for(int btn=0; btn<TOTAL_BUTTONS; btn++)
                        cmdButton[btn].setEnabled(false);
                     setupViewerTask.reloadResults = true;
                     enqueueRunTask(setupViewerTask);
                  }
               }
            );
            cmdButton[START_BTN] = new JButton("Start New Mission");
            cmdButton[START_BTN].addActionListener(new ActionListener()
               {
                  public void actionPerformed(ActionEvent ae)
                  {
                     InitMissionPanel imp = new InitMissionPanel(channelLabel,
                        hasResolution, channelResolution);
                     int result = JOptionPane.showConfirmDialog(MissionDS1925Viewer.this, imp,
                        "Initialize New Mission", JOptionPane.OK_CANCEL_OPTION);
                     if(result!=JOptionPane.CANCEL_OPTION)
                     {
                        boolean[] channelEnabled, enableAlarm;
                        boolean rollover, syncClock, suta;
                        int rate, delay;
                        double[] low, high, resolution;

                        boolean atLeastOneEnabled = false;
                        channelEnabled = new boolean[imp.chkChannelEnabled.length];
                        for(int i=0; i<channelEnabled.length; i++)
                        {
                           channelEnabled[i] = imp.chkChannelEnabled[i].isSelected();
                           atLeastOneEnabled |= channelEnabled[i];
                        }
                        if(!atLeastOneEnabled)
                        {
                           setStatus(ERRMSG, "No channels enabled for mission.");
                           return;
                        }

                        syncClock = imp.chkSyncClock.isSelected();
                        suta = imp.chkSUTA.isSelected();

                        try
                        {
                           rate = Integer.parseInt(imp.txtSampleRate.getText());
                           delay = Integer.parseInt(imp.txtStartDelay.getText());
                           low = new double[imp.txtLowAlarm.length];
                           high = new double[low.length];
                           enableAlarm = new boolean[low.length];
                           resolution = new double[low.length];
                           for(int i=0; i<low.length; i++)
                           {
                              enableAlarm[i] = imp.chkAlarmEnabled[i].isSelected();
                              if(enableAlarm[i])
                              {
                                 low[i] = Double.parseDouble(imp.txtLowAlarm[i].getText());
                                 high[i] = Double.parseDouble(imp.txtHighAlarm[i].getText());
                              }
                              resolution[i] = Double.parseDouble(imp.lstResolution[i].getSelectedValue().toString());
                              if(bFahrenheit)
                              {
                                 low[i] = Convert.toCelsius(low[i]);
                                 high[i] = Convert.toCelsius(high[i]);
                              }
                           }
                        }
                        catch(NumberFormatException nfe)
                        {
                           setStatus(ERRMSG, "Bad Number Format: " + nfe);
                           return;
                        }

                        synchronized(syncObj)
                        {
                           for(int btn=0; btn<TOTAL_BUTTONS; btn++)
                              cmdButton[btn].setEnabled(false);
                           InitMissionTask imt = new InitMissionTask(adapter, container,
                                    channelEnabled,
                                    syncClock, suta, rate,
                                    delay, enableAlarm, low, high, resolution);

                           enqueueRunTask(imt);
                           setupViewerTask.reloadResults = true;
                           enqueueRunTask(setupViewerTask);
                        }
                     }
                  }
               }
            );
            cmdButton[STOP_BTN] = new JButton("Disable Mission");
            cmdButton[STOP_BTN].addActionListener(new ActionListener()
               {
                  public void actionPerformed(ActionEvent ae)
                  {
                     synchronized(syncObj)
                     {
                        for(int i=0; i<TOTAL_BUTTONS; i++)
                           cmdButton[i].setEnabled(false);
                        enqueueRunTask(new DisableMissionTask(adapter, container));
                        enqueueRunTask(setupViewerTask);
                     }
                  }
               }
            );
            cmdButton[LAST_BTN] = new JButton("Ignore Backup");
            cmdButton[LAST_BTN].addActionListener(new ActionListener()
               {
                  public void actionPerformed(ActionEvent ae)
                  {
                     synchronized(syncObj)
                     {
                        for(int i=0; i<TOTAL_BUTTONS; i++)
                           cmdButton[i].setEnabled(false);
                        forceIgnoreBackup = !forceIgnoreBackup;
                        if (forceIgnoreBackup)
                           cmdButton[LAST_BTN].setLabel("Allow Backup");
                        else                        
                           cmdButton[LAST_BTN].setLabel("Ignore Backup");
                        enqueueRunTask(setupViewerTask);
                     }
                  }
               }
            );
            cmdButton[CLEAR_BTN] = new JButton("Clear Mission Log");
            cmdButton[CLEAR_BTN].addActionListener(new ActionListener()
               {
                  public void actionPerformed(ActionEvent ae)
                  {
                     synchronized(syncObj)
                     {
                        for(int i=0; i<TOTAL_BUTTONS; i++)
                           cmdButton[i].setEnabled(false);
                        enqueueRunTask(new ClearLogTask(adapter, container));
                        enqueueRunTask(setupViewerTask);
                     }
                  }
               }
            );
            
         buttonPanel.add(cmdButton[REFRESH_BTN]);
         buttonPanel.add(cmdButton[START_BTN]);
         buttonPanel.add(cmdButton[STOP_BTN]);
         buttonPanel.add(cmdButton[LAST_BTN]);
         buttonPanel.add(cmdButton[CLEAR_BTN]);

 			// flow layout master panel to hold 2 other panels -- temp and humidity
         JPanel graphSettingsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			graphSettingsPanel.setBorder(BorderFactory.createTitledBorder(			
			BorderFactory.createEtchedBorder(), "Graph Settings")); 
			JPanel tGraphSettingsPanel = new JPanel(new GridLayout(2, 1, 3, 3));			
			tGraphSettingsPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createEtchedBorder(), "Temperature"));

            ButtonGroup group = new ButtonGroup();
            JCheckBox fahCheck = new JCheckBox("Fahrenheit", bFahrenheit);
            fahCheck.setFont(fontPlain);
            group.add(fahCheck);
            fahCheck.addActionListener(new ActionListener()
               {
                  public void actionPerformed(ActionEvent ae)
                  {
                     if(container!=null)
                     {
                        bFahrenheit = true;
                        ViewerProperties.setPropertyBoolean(
                           MISSION_VIEWER_FAHRENHEIT, true);
                        enqueueRunTask(setupViewerTask);
                     }
                  }
               }
            );
            JCheckBox celCheck = new JCheckBox("Celsius", !bFahrenheit);
            celCheck.setFont(fontPlain);
            group.add(celCheck);
            celCheck.addActionListener(new ActionListener()
               {
                  public void actionPerformed(ActionEvent ae)
                  {
                     if(container!=null)
                     {
                        bFahrenheit = false;
                        ViewerProperties.setPropertyBoolean(
                           MISSION_VIEWER_FAHRENHEIT, false);
                        enqueueRunTask(setupViewerTask);
                     }
                  }
               }
            );
            rhTempCompCheck = new JCheckBox("%RH Temperature Compensation", bTCompensation);
            rhTempCompCheck.setFont(fontPlain);
            rhTempCompCheck.addActionListener(new ActionListener()
               {
                  public void actionPerformed(ActionEvent ae)
                  {
                     if(container!=null)
                     {
                        bTCompensation = !bTCompensation;
                        ViewerProperties.setPropertyBoolean(
                           MISSION_VIEWER_TCOMPENSATION, bTCompensation);
                        enqueueRunTask(setupViewerTask);
                     }
                  }
               }
            );
				rhLimitSatDriftCheck = new JCheckBox("Limit %RH Saturation Drift " + "\r\n" + "to 0% and 100%", bLimitSatDrift);
            rhLimitSatDriftCheck.setFont(fontPlain);
            rhLimitSatDriftCheck.addActionListener(new ActionListener()
               {
                  public void actionPerformed(ActionEvent ae)
                  {
                     if(container!=null)
                     {
                        bLimitSatDrift = !bLimitSatDrift;
                        ViewerProperties.setPropertyBoolean(
                           MISSION_VIEWER_LIMITSATDRIFT, bLimitSatDrift);
                        enqueueRunTask(setupViewerTask);
                     }
                  }
               }
            );
         tGraphSettingsPanel.add(fahCheck);
         tGraphSettingsPanel.add(celCheck);
			//hGraphSettingsPanel.add(rhTempCompCheck);
			//hGraphSettingsPanel.add(rhLimitSatDriftCheck);
			graphSettingsPanel.add(tGraphSettingsPanel);
		   //graphSettingsPanel.add(hGraphSettingsPanel);

      
      c.fill = GridBagConstraints.HORIZONTAL;
      c.ipady = 5;  
      c.weightx = 1.0;
      c.gridwidth = 3;
      c.gridx = 0;
      c.gridy = 0;
		commandPanel.add(buttonPanel,c);
		
      //commandPanel.add(passwordPanel);
      c.fill = GridBagConstraints.HORIZONTAL;
      c.ipady = 6;      
      c.weightx = 1.0;
      c.gridwidth = 3;
      c.gridx = 0;
      c.gridy = 1;		
      commandPanel.add(graphSettingsPanel,c); 
		
      // add components to viewer
      add(commandScroll, BorderLayout.NORTH);
      add(tabbedResultsPane, BorderLayout.CENTER);

      clearContainer();
		rhTempCompCheck.setEnabled(false); 
		rhLimitSatDriftCheck.setEnabled(false); 
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
      return ((owc instanceof MissionContainer) && !(owc instanceof HumidityContainer));
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
            try
            {
               for(int btn=0; btn<TOTAL_BUTTONS; btn++)
                  cmdButton[btn].setEnabled(false);
               this.adapter = owc.getAdapter();
               this.container = (MissionContainer)owc;
               this.romID = owc.getAddressAsString();

               this.tabbedResultsPane.removeAll();
               tabbedResultsPane.addTab("Status",null,featureScroll,
                  "Mission Status");
               tabbedResultsPane.addTab("Temperature Data Log",null,temperatureScroll,
                  "Graphs the mission's temperature log");
               this.tabbedResultsPane.setEnabledAt(1, false);

               this.channelLabel = new String[this.container.getNumberMissionChannels()];
               this.hasResolution = new boolean[this.channelLabel.length];
               this.channelResolution = new double[this.channelLabel.length][];
               for(int i=0; i<this.channelLabel.length; i++)
               {
                  this.channelLabel[i] = this.container.getMissionLabel(i);
                  this.channelResolution[i] = this.container.getMissionResolutions(i);
                  this.hasResolution[i] = this.channelResolution[i].length>1;
               }
               this.lblFeature[MissionDS1925Viewer.DATA_LOGGING].setVisible(
                  channelLabel.length==2);
               this.lblFeatureHdr[MissionDS1925Viewer.DATA_LOGGING].setVisible(
                  channelLabel.length==2);
               this.lblFeature[MissionDS1925Viewer.DATA_LOW_ALARM].setVisible(
                  channelLabel.length==2);
               this.lblFeatureHdr[MissionDS1925Viewer.DATA_LOW_ALARM].setVisible(
                  channelLabel.length==2);
               this.lblFeature[MissionDS1925Viewer.DATA_HIGH_ALARM].setVisible(
                  channelLabel.length==2);
               this.lblFeatureHdr[MissionDS1925Viewer.DATA_HIGH_ALARM].setVisible(
                  channelLabel.length==2);
               if(channelLabel.length==2)
               {
                  if (channelLabel[1].equalsIgnoreCase("Humidity"))
                  {
                     tabbedResultsPane.addTab("Humidity Data Log", null, dataScroll,
                        "Graphs the mission's data log");
                  }
                  else
                  {
                     tabbedResultsPane.addTab(channelLabel[1], null, dataScroll,
                        "Graphs the mission's data log");
                  }
                  this.tabbedResultsPane.setEnabledAt(2, false);
               }

            }
            catch(Exception e)
            {
               e.printStackTrace();
               setStatus(ERRMSG, "Error getting channel descriptions: " + e.getMessage());
            }
         }
         enqueueRunTask(setupViewerTask);
      }
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
    * Clears the reference to the device container.
    */
   public void clearContainer()
   {
      synchronized(syncObj)
      {
         this.adapter = null;
         this.container = null;
         this.romID = null;
      }
      try
      {
         setStatus(VERBOSE, "No Device");
         this.dataPlot.resetPlot();
         this.temperaturePlot.resetPlot();
         for(int i=0; i<TOTAL_FEATURES; i++)
            lblFeature[i].setText("");
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
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
       // only return true if the task is not just thesetup task
       return (runList.size()>0)
          && ( (runList.size()>1) || (runList.indexOf(setupViewerTask)<0) );
    }

   /**
    * 'run' method that is called continuously to service
    * GUI interaction, such as button click events.  Also
    * performs one-time setup of viewer.
    */
   public void run()
   {
      while(executeRunTask())
         /* no-op */;
   }

   /**
    * 'poll' method that is called at the current polling rate.
    * Read and update the display of the device status.
    */
   public void poll()
   {
      synchronized(syncObj)
      {
         if(this.pausePoll)
            return;
         this.pollRunning = true;
      }

      while(executePollTask())
         /* no-op */;

      synchronized(syncObj)
      {
         pollRunning = false;
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
      ThermochronViewer tv = new ThermochronViewer();
      tv.setContainer((OneWireContainer)this.container);
      return tv;
   }

   /**
    * NewMissionTask encapsulates the action of initializing a new mission for
    * a Thermochron Device.
    */
   protected class InitMissionTask extends ViewerTask
   {
      DSPortAdapter task_adapter;
      MissionContainer task_container;
      boolean task_rollover, task_syncClock, task_suta;
      int task_rate, task_delay;
      boolean[] task_enableAlarm, task_channelEnabled;
      double[] task_low, task_high, task_resolution;

      public InitMissionTask(DSPortAdapter adapter, MissionContainer container,
         boolean[] channelEnabled, boolean syncClock, boolean suta, int rate,
         int delay, boolean[] enableAlarm, double[] low, double[] high, double[] resolution)
      {
         task_adapter = adapter;
         task_container = container;
         task_channelEnabled = channelEnabled;
         task_syncClock = syncClock;
         task_suta = suta;
         task_rate = rate;
         task_delay = delay;
         task_enableAlarm = enableAlarm;
         task_low = low;
         task_high = high;
         task_resolution = resolution;
      }

      public void executeTask()
      {
         if(task_adapter==null || task_container==null)
            return;
         try
         {
            task_adapter.beginExclusive(true);

            boolean  missionActive = task_container.isMissionRunning();

            // disable current mission
            if (missionActive)
               task_container.stopMission();
            else
            {
               // perform a temperature conversion to make sure RTC has been enable once 
               byte[] state = task_container.readDevice();
               ((OneWireContainer53)task_container).doTemperatureConvert(state);
            }

            boolean anyAlarmsEnabled = false;
            if(task_enableAlarm!=null && task_high!=null && task_low!=null)
            {
               for(int i=0; i<task_enableAlarm.length; i++)
               {
                  if(task_enableAlarm[i])
                  {
                     anyAlarmsEnabled = true;
                     task_container.setMissionAlarm(i, MissionContainer.ALARM_HIGH, task_high[i]);
                     task_container.setMissionAlarm(i, MissionContainer.ALARM_LOW, task_low[i]);
                  }
                  task_container.setMissionAlarmEnable(i, MissionContainer.ALARM_HIGH, task_enableAlarm[i]);
                  task_container.setMissionAlarmEnable(i, MissionContainer.ALARM_LOW, task_enableAlarm[i]);
                  task_container.setMissionResolution(i, task_resolution[i]);
               }
            }
            
            ((OneWireContainer53)task_container).setStartUponTemperatureAlarmEnable(task_suta && anyAlarmsEnabled);

            task_container.startNewMission(task_rate, task_delay,
                                           task_rollover, task_syncClock,
                                           task_channelEnabled);

            setStatus(MESSAGE, " New Mission Initialized!");
         }
         catch(Exception e)
         {
            setStatus(ERRMSG, "Initialize Mission error; " + e.toString());
            e.printStackTrace();
         }
         finally
         {
            task_adapter.endExclusive();
         }
      }
   }

   /**
    * DisableMissionTask encapsulates the action of disabling a mission for
    * a Thermochron Device.
    */
   protected class DisableMissionTask extends ViewerTask
   {
      DSPortAdapter task_adapter = null;
      MissionContainer task_container = null;

      public DisableMissionTask(DSPortAdapter adapter, MissionContainer container)
      {
         task_adapter = adapter;
         task_container = container;
      }

      public void executeTask()
      {
         if(task_adapter==null || task_container==null)
            return;
         try
         {
            task_adapter.beginExclusive(true);
            boolean  missionActive = task_container.isMissionRunning();

            // disable current mission
            if(missionActive)
            {
               task_container.stopMission();
               setStatus(MESSAGE, " Mission Disabled!");
            }
            else
               setStatus(MESSAGE, " No Mission in Progress!");
         }
         catch(Exception e)
         {
            setStatus(ERRMSG, "Initialize Mission error: " + e.toString());
            e.printStackTrace();
         }
         finally
         {
            task_adapter.endExclusive();
         }
      }
   }

   /**
    * ClearLogTask encapsulates the action of clearing the log on
    * a Thermochron Device. This is required coming out of freshness. 
    */
   protected class ClearLogTask extends ViewerTask
   {
      DSPortAdapter task_adapter = null;
      MissionContainer task_container = null;

      public ClearLogTask(DSPortAdapter adapter, MissionContainer container)
      {
         task_adapter = adapter;
         task_container = container;
      }

      public void executeTask()
      {
         if(task_adapter==null || task_container==null)
            return;
         try
         {
            task_adapter.beginExclusive(true);

            // check if mission
            if(task_container.isMissionRunning())
               setStatus(MESSAGE, " Mission Running, can not clear log!");
            else
            {
               task_container.clearMissionResults();
               setStatus(MESSAGE, " Log cleared.");
            }
         }
         catch(Exception e)
         {
            setStatus(ERRMSG, "Clear Log error: " + e.toString());
            e.printStackTrace();
         }
         finally
         {
            task_adapter.endExclusive();
         }
      }
   }
   
   /**
    * SetupViewerTask encapsulates the action of setting up the viewer for
    * a Thermochron Device.  Since this class is, essentially,
    * parameter-less (or, rather it grabs its parameters from the current state
    * of the viewer) only one instance is really necessary.
    */
   protected class SetupViewerTask extends ViewerTask
   {
      public boolean reloadResults = false;

      public void executeTask()
      {
		   double sample = 0.0;
         String newline = System.getProperty("line.separator");
         String missionResults = "";
         DSPortAdapter l_adapter = null;
         MissionContainer l_container = null;
		   OneWireContainer53 owc53 = null;
         synchronized(syncObj)
         {
            if(adapter==null || container==null)
               return;
            l_adapter = adapter;
            l_container = container;
			   owc53 = (OneWireContainer53) container;
         }

         MissionDS1925Viewer.this.tabbedResultsPane.setSelectedIndex(0);
         MissionDS1925Viewer.this.tabbedResultsPane.setEnabledAt(1, false);
         if(MissionDS1925Viewer.this.tabbedResultsPane.getTabCount()==3)
            MissionDS1925Viewer.this.tabbedResultsPane.setEnabledAt(2, false);
         setStatus(VERBOSE, "Setting up viewer");
         try
         {
            l_adapter.beginExclusive(true);
            boolean success = true;

            owc53.checkMissionBackup(forceIgnoreBackup);
            
            boolean  missionActive = l_container.isMissionRunning();
            boolean  usingBackupMission = ((OneWireContainer53)l_container).getUseBackupMissionFlag();
            
            // Special case where we don't have a battery fail condition
            if (!usingBackupMission && !forceIgnoreBackup)
            {
               // hide the 'force' button 
               cmdButton[LAST_BTN].setVisible(false);
            }
            
            missionResults = missionResults + "1-Wire/iButton Part Number: " + ((OneWireContainer53)l_container).getName() + newline;
            missionResults = missionResults + "1-Wire/iButton Registration Number: " + ((OneWireContainer53)l_container).getAddressAsString() + newline;
            if(reloadResults || !l_container.isMissionLoaded())
            {               
               int pagesToGo = 0, pagesTotal = 0;
               success = false;
               double percent = 0.0; 
               try
               {
                  do
                  {
                     // Read a page of log, returns pages to go
                     pagesToGo = ((OneWireContainer53)l_container).loadMissionResultsPartial(pagesToGo == 0);
                     
                     // Check for first read, record the maximum number of pages
                     if (pagesTotal == 0)
                        pagesTotal = pagesToGo;        
                        
                     // compute percentage remaining                         
                     if (pagesTotal != 0)
                        percent = ((double)pagesToGo/(double)pagesTotal) * (double)100.0;
                     
                     // Show Status.  If battery expired then this is only an estimate based on the entire log size. 
                     if (usingBackupMission)
                        lblFeature[MISSION_SAMPLES].setText("Battery Fail Condition, estimate download remaining: " + String.format("%.0f",percent) + "%"); 
                     else
                        lblFeature[MISSION_SAMPLES].setText("Remaining to download: " + String.format("%.0f",percent) + "%");  
                  }
                  while (pagesToGo != 0);  
                  success = true;               
               }
               catch(Exception e)
               {                  
                  e.printStackTrace();
               }
            }
            reloadResults = !success;

            int sample_rate = l_container.getMissionSampleRate(0);       
            
            lblFeature[IS_ACTIVE].setText(" " + missionActive);
            lblFeature[MISSION_SUTA].setText(" "
               + ((OneWireContainer53)l_container).isMissionSUTA());
            lblFeature[MISSION_WFTA].setText(" "
               + ((OneWireContainer53)l_container).isMissionWFTA());

            lblFeature[SAMPLE_RATE].setText(" Every " +
                           (sample_rate) + " second(s)");

            if (((OneWireContainer53)l_container).isMissionWFTA())
            {
                lblFeature[MISSION_START].setText(" Waiting for temperature to start Mission");  
            }               
            else
            {
               lblFeature[MISSION_START].setText(" " +
                           new Date(l_container.getMissionTimeStamp(0)));
            }

            int sample_count = l_container.getMissionSampleCount(0);
            lblFeature[MISSION_SAMPLES].setText(" " + nf.format(sample_count));
                           
            lblFeature[BATTERY_STATE].setText(" " + usingBackupMission);
                           
            if ((sample_count>0) || usingBackupMission)
            {
               lblFeature[FIRST_SAMPLE_TIMESTAMP].setText(" " + 
                             new Date(l_container.getMissionSampleTimeStamp(0,0)));
            }
            else
            {
               lblFeature[FIRST_SAMPLE_TIMESTAMP].setText(" First sample not yet logged");
            }
            
            lblFeature[TOTAL_SAMPLES].setText(" " + sample_count);

            if(l_container.getMissionChannelEnable(0))
               lblFeature[TEMP_LOGGING].setText(" " + l_container.getMissionResolution(0) + " C");
            else
               lblFeature[TEMP_LOGGING].setText(" disabled");

            if(l_container.getMissionChannelEnable(0) &&
               l_container.getMissionAlarmEnable(0, MissionContainer.ALARM_HIGH))
            {
               if(bFahrenheit)
               {
                  // read the high temperature alarm setting
                  String highAlarmText = nf.format(
                     Convert.toFahrenheit(
                        l_container.getMissionAlarm(0,MissionContainer.ALARM_HIGH)));
                  lblFeature[TEMP_HIGH_ALARM].setText(" " + highAlarmText + " °F");
               }
               else
               {
                  // read the high temperature alarm setting
                  String highAlarmText = nf.format(
                        l_container.getMissionAlarm(0,MissionContainer.ALARM_HIGH));
                  lblFeature[TEMP_HIGH_ALARM].setText(" " + highAlarmText + " °C");
               }
               if(l_container.hasMissionAlarmed(0, MissionContainer.ALARM_HIGH))
                  lblFeature[TEMP_HIGH_ALARM].setText(
                           lblFeature[TEMP_HIGH_ALARM].getText() + " (ALARMED)");
            }
            else
            {
               lblFeature[TEMP_HIGH_ALARM].setText(" disabled");
            }
            if(l_container.getMissionChannelEnable(0) &&
               l_container.getMissionAlarmEnable(0, MissionContainer.ALARM_LOW))
            {
               if(bFahrenheit)
               {
                  // read the low temperature alarm setting
                  String lowAlarmText = nf.format(
                     Convert.toFahrenheit(
                        l_container.getMissionAlarm(0,MissionContainer.ALARM_LOW)));
                  lblFeature[TEMP_LOW_ALARM].setText(" " + lowAlarmText + " °F");
               }
               else
               {
                  // read the low temperature alarm setting
                  String lowAlarmText = nf.format(
                        l_container.getMissionAlarm(0,MissionContainer.ALARM_LOW));
                  lblFeature[TEMP_LOW_ALARM].setText(" " + lowAlarmText + " °C");
               }
               if(l_container.hasMissionAlarmed(0, MissionContainer.ALARM_LOW))
                  lblFeature[TEMP_LOW_ALARM].setText(
                           lblFeature[TEMP_LOW_ALARM].getText() + " (ALARMED)");
            }
            else
            {
               lblFeature[TEMP_LOW_ALARM].setText(" disabled");
            }

            if (usingBackupMission)            
               lblFeature[DEVICE_SAMPLES].setText(" " + (((OneWireContainer53)l_container).getDeviceSampleCount() + sample_count));
            else
               lblFeature[DEVICE_SAMPLES].setText(" " + ((OneWireContainer53)l_container).getDeviceSampleCount());

            String useTempCal
               = com.dalsemi.onewire.OneWireAccessProvider.getProperty(
                  "DS1922H.useTemperatureCalibrationRegisters");
            
            missionResults += strHeader[IS_ACTIVE] + lblFeature[IS_ACTIVE].getText() + newline;
            missionResults += strHeader[MISSION_SUTA] + lblFeature[MISSION_SUTA].getText() + newline;
            missionResults += strHeader[MISSION_WFTA] + lblFeature[MISSION_WFTA].getText() + newline;
            missionResults += strHeader[SAMPLE_RATE] + lblFeature[SAMPLE_RATE].getText() + newline;
            missionResults += strHeader[MISSION_START] + lblFeature[MISSION_START].getText() + newline;
            missionResults += strHeader[MISSION_SAMPLES] + lblFeature[MISSION_SAMPLES].getText() + newline;
            missionResults += strHeader[BATTERY_STATE] + lblFeature[BATTERY_STATE].getText() + newline;
            missionResults += strHeader[FIRST_SAMPLE_TIMESTAMP] + lblFeature[FIRST_SAMPLE_TIMESTAMP].getText() + newline;
            missionResults += strHeader[TOTAL_SAMPLES] + lblFeature[TOTAL_SAMPLES].getText() + newline;
            missionResults += strHeader[DEVICE_SAMPLES] + lblFeature[DEVICE_SAMPLES].getText() + newline;
            missionResults += strHeader[TEMP_LOGGING] + lblFeature[TEMP_LOGGING].getText() + newline;
            missionResults += strHeader[TEMP_HIGH_ALARM] + lblFeature[TEMP_HIGH_ALARM].getText() + newline;
            missionResults += strHeader[TEMP_LOW_ALARM] + lblFeature[TEMP_LOW_ALARM].getText() + newline;
            missionResults += strHeader[DATA_LOGGING] + lblFeature[DATA_LOGGING].getText() + newline;
            missionResults += strHeader[DATA_HIGH_ALARM] + lblFeature[DATA_HIGH_ALARM].getText() + newline;
            missionResults += strHeader[DATA_LOW_ALARM] + lblFeature[DATA_LOW_ALARM].getText() + newline + newline;
            missionResults += "Date/Time,Unit,Value" + newline;

            temperaturePlot.setHeaderString(missionResults);
            
            temperaturePlot.resetPlot();
            if(l_container.getMissionChannelEnable(0))
            {
               if(bFahrenheit)
               {
                  // plot the temperature log
                  for(int i=0; i<l_container.getMissionSampleCount(0); i++)
                     temperaturePlot.addPoint(
                        Convert.toFahrenheit(
                           l_container.getMissionSample(0,i)),
                        df.format(
                           new Date(
                              l_container.getMissionSampleTimeStamp(0, i)))
                        + ",F");
               }
               else
               {
                  // plot the temperature log
                  for(int i=0; i<l_container.getMissionSampleCount(0); i++)
                     temperaturePlot.addPoint(
                        l_container.getMissionSample(0,i),
                        df.format(
                           new Date(
                              l_container.getMissionSampleTimeStamp(0, i)))
                        + ",C");
               }
               MissionDS1925Viewer.this.tabbedResultsPane.setEnabledAt(1, true);
            }

            dataPlot.setHeaderString(missionResults);
            dataPlot.resetPlot();
         }
         catch(Exception e)
         {
            e.printStackTrace();
            setStatus(ERRMSG, "Setup Error: " + e.toString());
         }
         finally
         {
            l_adapter.endExclusive();
         }
         setStatus(VERBOSE, "Done Setting up viewer...");
         for(int btn=0; btn<TOTAL_BUTTONS; btn++)
            cmdButton[btn].setEnabled(true);
      }
   }

   /**
    * Panel for querying all new mission parameters
    */
   class InitMissionPanel extends JPanel
   {
      JCheckBox[] chkChannelEnabled, chkAlarmEnabled;
      JCheckBox chkSyncClock, chkSUTA;
      JTextField txtSampleRate,txtStartDelay;
      JTextField[] txtLowAlarm, txtHighAlarm;
      JList[] lstResolution;
      //JCheckBox oneSecMissionTest;

      public InitMissionPanel(String[] channelLabels,
                              boolean[] hasResolution,
                              double[][] resolutions)
      {
         super(new BorderLayout(3,3));

         chkChannelEnabled = new JCheckBox[channelLabels.length];
         chkAlarmEnabled = new JCheckBox[channelLabels.length];
         txtLowAlarm = new JTextField[channelLabels.length];
         txtHighAlarm = new JTextField[channelLabels.length];
         lstResolution = new JList[channelLabels.length];

         JPanel tempGrid = new JPanel(new GridLayout(3,2));
         tempGrid.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Mission General"));
         JPanel tempBox1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         JPanel tempBox2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            // synchronize clock checkbox
            chkSyncClock = new JCheckBox("Synchronize Clock? ");
            chkSyncClock.setSelected(true);
            chkSyncClock.setFont(fontBold);
            chkSyncClock.setHorizontalAlignment(SwingConstants.CENTER);
            // sample rate input
            JLabel lblTemp = new JLabel("Sampling Rate (seconds) ");
            lblTemp.setFont(fontBold);
            lblTemp.setForeground(Color.black);
            tempBox1.add(lblTemp);
            txtSampleRate = new JTextField(6);
            txtSampleRate.setFont(fontPlain);
            txtSampleRate.setText("600");
            tempBox1.add(txtSampleRate);
            // start delay input
            lblTemp = new JLabel("Start Delay (minutes) ");
            lblTemp.setFont(fontBold);
            lblTemp.setForeground(Color.black);
            tempBox2.add(lblTemp);
            txtStartDelay = new JTextField(6);
            txtStartDelay.setFont(fontPlain);
            txtStartDelay.setText("0");
            tempBox2.add(txtStartDelay);

         tempGrid.add(chkSyncClock);
         tempGrid.add(tempBox1);
         //tempGrid.add(chkRollover);
         tempGrid.add(tempBox2);
         //tempGrid.add(oneSecMissionTest);

         add(tempGrid, BorderLayout.NORTH);

         tempGrid = new JPanel(new GridLayout(channelLabels.length,1));
         for(int i=0; i<channelLabels.length; i++)
         {
            boolean tempChan = channelLabels[i].equals("Temperature");
            JPanel tempPanel = new JPanel(new BorderLayout());
            tempPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Mission Channel: " + channelLabels[i]));
            JPanel leftPanel = new JPanel(new GridLayout(2, 1));
            JPanel rightPanel = new JPanel(new GridLayout(tempChan?4:3, 1));
            tempBox1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            tempBox2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JPanel tempBox3 = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JPanel tempBox4 = tempChan?new JPanel(new FlowLayout(FlowLayout.CENTER)):null;
               // enable channel checkbox
               chkChannelEnabled[i] = new JCheckBox("Enable Sampling? ");
               chkChannelEnabled[i].setSelected(true);
               chkChannelEnabled[i].setFont(fontBold);
               chkChannelEnabled[i].setHorizontalAlignment(SwingConstants.CENTER);
               chkChannelEnabled[i].setVisible(false);  // Hide enable because we always want to log temperature
               // Select resolution
               lblTemp = new JLabel("Resolution: ");
               lblTemp.setFont(fontBold);
               lblTemp.setForeground(Color.black);
               tempBox3.add(lblTemp);
               if(hasResolution[i])
               {
                  Vector v = new Vector(resolutions[i].length);
                  for(int j=0; j<resolutions[i].length; j++)
                     v.addElement(new Double(resolutions[i][j]));
                  lstResolution[i] = new JList(v);
                  lstResolution[i].setFont(fontBold);
                  lstResolution[i].setSelectedIndex(0);
                  tempBox3.add(lstResolution[i]);
               }
               else
               {
                  lstResolution[i] = new JList(new String[]{"N/A"});
                  lstResolution[i].setVisibleRowCount(1);
                  lstResolution[i].setFont(fontBold);
               }
               // enable alarm checkbox
               chkAlarmEnabled[i] = new JCheckBox("Enable Alarms?");
               chkAlarmEnabled[i].setSelected(false);
               chkAlarmEnabled[i].setFont(fontBold);
               chkAlarmEnabled[i].setHorizontalAlignment(SwingConstants.CENTER);
               // low alarm input
               if(channelLabels[i].equals("Temperature"))
               {
                  if(bFahrenheit)
                     lblTemp = new JLabel("Low Alarm? (°F)");
                  else
                     lblTemp = new JLabel("Low Alarm? (°C)");
               }
               else if(channelLabels[i].equals("Humidity"))
               {
                  lblTemp = new JLabel("Low Alarm? (%RH)");
               }
               else
               {
                  lblTemp = new JLabel("Low Alarm?");
               }
               lblTemp.setFont(fontBold);
               lblTemp.setForeground(Color.black);
               tempBox1.add(lblTemp);
               txtLowAlarm[i] = new JTextField(6);
               txtLowAlarm[i].setFont(fontPlain);
               tempBox1.add(txtLowAlarm[i]);
               // high alarm input
               if(tempChan)
               {
                  if(bFahrenheit)
                     lblTemp = new JLabel("High Alarm? (°F)");
                  else
                     lblTemp = new JLabel("High Alarm? (°C)");
               }
               else if(channelLabels[i].equals("Humidity"))
               {
                  lblTemp = new JLabel("High Alarm? (%RH)");
               }
               else
               {
                  lblTemp = new JLabel("High Alarm?");
               }
               lblTemp.setFont(fontBold);
               lblTemp.setForeground(Color.black);
               tempBox2.add(lblTemp);
               txtHighAlarm[i] = new JTextField(6);
               txtHighAlarm[i].setFont(fontPlain);
               tempBox2.add(txtHighAlarm[i]);
               if(tempChan)
               {
                  chkSUTA = new JCheckBox("Enable SUTA?");
                  tempBox4.add(chkSUTA);
               }
            leftPanel.add(chkChannelEnabled[i]);
            leftPanel.add(tempBox3);
            rightPanel.add(chkAlarmEnabled[i]);
            rightPanel.add(tempBox1);
            rightPanel.add(tempBox2);
            if(tempChan)
               rightPanel.add(tempBox4);
            tempPanel.add(leftPanel, BorderLayout.CENTER);
            tempPanel.add(rightPanel, BorderLayout.EAST);

            tempGrid.add(tempPanel);
         }

         add(tempGrid, BorderLayout.WEST);
      }
   }
   
   /**
    */

   protected class SetDevicePasswordTask extends ViewerTask
   {
      DSPortAdapter task_adapter = null;
      OneWireContainer53 task_container = null;
      byte[] task_readPassword = null;
      int task_readOffset;
      byte[] task_writePassword = null;
      int task_writeOffset;

      public SetDevicePasswordTask(DSPortAdapter adapter,
                                   OneWireContainer53 container,
                                   byte[] readPassword, int readOffset,
                                   byte[] writePassword, int writeOffset
                                   )
      {
         task_adapter = adapter;
         task_container = container;
         task_readPassword = readPassword;
         task_readOffset = readOffset;
         task_writePassword = writePassword;
         task_writeOffset = writeOffset;
      }

      public void executeTask()
      {
         if(task_adapter==null || task_container==null)
            return;
         try
         {
            task_adapter.beginExclusive(true);
            if(task_container.isMissionRunning())
            {
               setStatus(ERRMSG, "Cannot change password while mission is in progress");
            }
            else
            {
               if(task_writePassword!=null)
               {
                  setStatus(VERBOSE, "Setting Read/Write Password");
                  task_container.setDeviceReadWritePassword(task_writePassword, task_writeOffset);
               }
               if(task_readPassword!=null)
               {
                  setStatus(VERBOSE, "Setting Read Password");
                  task_container.setDeviceReadOnlyPassword(task_readPassword, task_readOffset);
               }
               setStatus(VERBOSE, "Done Setting Passwords!");
            }
         }
         catch(Exception e)
         {
            setStatus(ERRMSG, "Set Password error: " + e.toString());
            e.printStackTrace();
         }
         finally
         {
            task_adapter.endExclusive();
         }
         for(int btn=0; btn<TOTAL_BUTTONS; btn++)
            cmdButton[btn].setEnabled(true);
      }
   }
//*/
   /**
    */

   protected class SetPasswordEnableTask extends ViewerTask
   {
      DSPortAdapter task_adapter = null;
      OneWireContainer53 task_container = null;
      boolean task_passwordsEnabled;
      byte[] task_readPassword = null;
      int task_readOffset;
      byte[] task_writePassword = null;
      int task_writeOffset;

      public SetPasswordEnableTask(DSPortAdapter adapter,
                                   OneWireContainer53 container,
                                   boolean passwordsEnabled)
      {
         task_adapter = adapter;
         task_container = container;
         task_passwordsEnabled = passwordsEnabled;
      }

      public void executeTask()
      {
         if(task_adapter==null || task_container==null)
            return;
         try
         {
            setStatus(VERBOSE, "Setting Password Enable to " + task_passwordsEnabled);
            task_adapter.beginExclusive(true);
            if(task_container.isMissionRunning())
            {
               setStatus(ERRMSG, "Cannot change password while mission is in progress");
            }
            else
            {
               task_container.setDevicePasswordEnableAll(task_passwordsEnabled);
               setStatus(VERBOSE, "Done! Password Enable set to " + task_passwordsEnabled);
            }
         }
         catch(Exception e)
         {
            setStatus(ERRMSG, "Set Password Enable error: " + e.toString());
            e.printStackTrace();
         }
         finally
         {
            task_adapter.endExclusive();
         }
         for(int btn=0; btn<TOTAL_BUTTONS; btn++)
            cmdButton[btn].setEnabled(true);
      }
   }
//*/
   /**
    */
   public void getMissionStatusString()
   {
   }

}