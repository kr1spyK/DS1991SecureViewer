' //
' // www.pudn.com <http://www.pudn.com/> > ds199x.rar
' // <http://www.pudn.com/Download/item/id/266998.html> > ds1991_com.bas,
' // change:2002-03-19,size:21327b
' //

Attribute VB_Name = "ds1991_com" 
'//-------------------------------------------------------------------------- 
'// Copyright (C) 1992,1993,1994,1995,1996 Dallas Semiconductor Corporation. 
'// All rights Reserved. Printed in U.S.A. 
'// This software is protected by copyright laws of 
'// the United States and of foreign countries. 
'// This material may also be protected by patent laws of the United States 
'// and of foreign countries. 
'// This software is furnished under a license agreement and/or a 
'// nondisclosure agreement and may only be used or copied in accordance 
'// with the terms of those agreements. 
'// The mere transfer of this software does not imply any licenses 
'// of trade secrets, proprietary technology, copyrights, patents, 
'// trademarks, maskwork rights, or any other form of intellectual 
'// property whatsoever. Dallas Semiconductor retains all ownership rights. 
'//-------------------------------------------------------------------------- 
' Version 3.00 
' 
 
Option Explicit 
 
'// Global Variable Declaration 
 
'// session handle 
Global SHandle As Long 
'// data state space for DLL 
Global state_buffer(15360) As Byte 
'// port selection 
Global PortNum As Integer 
Global PortType As Integer 
'// debounce array 
Global Debounce(100) As Integer 
'// save selected values 
Global SelectROM(8) As Integer 
Global SelectFile(6) As Byte 
'// directory of the current opened device 
Global DirBuf(256) As Byte 
'// data to save 
Global SaveEditData As String 
'// flag to indicate if TMSetup has been run 
Global SetupDone As Integer 
'// max debounce value 
Global MaxDbnc As Integer 
 
'// declare the functions to read the registry 
Declare Function RegOpenKeyExA Lib "Advapi32.DLL" (ByVal hKey As Long, ByVal lpszSubKey As String, ByVal dwReserved As Long, ByVal samDesired As Long, phkResult As Long) As Long 
Declare Function RegQueryValueExA Lib "Advapi32.DLL" (ByVal hKey As Long, ByVal lpszValueName As String, ByVal lpdwReserved As Any, lpdwType As Long, lpbData As Byte, lpcbData As Long) As Long 
Declare Function RegCloseKey Lib "Advapi32.DLL" (ByVal hKey As Long) As Long 
Public Const KEY_READ As Long = &H120019 
Public Const HKEY_LOCAL_MACHINE As Long = &H80000002 
Public Const ERROR_SUCCESS As Long = &H0 
Public Const REG_SZ As Long = &H1 
 
 
 
'ByVal ID_buf$ 
' Change the directory from the current working directory to the 
' sub-directory name given in RS.  RS can be a back reference '..'. 
' 
Sub ChangeDirectory(RS As String) 
    Dim flag As Integer 
    Dim Dmmy As Integer 
    Dim TDir(6) As Byte 
    Dim I As Integer 
     
    '// start a session 
    SHandle = TMExtendedStartSession(PortNum, PortType, vbNullString) 
    If (SHandle > 0) Then 
        '// set the selected rom 
        flag = TMRom(SHandle, state_buffer(0), SelectROM(0)) 
        '// first change the directory to the correct current directory 
        '// need to do this because background task of listing roms has 
        '// reset the current directory 
        flag = TMChangeDirectory(SHandle, state_buffer(0), 0, DirBuf(0)) 
        '// change the directory to the selected sub directory 
        TDir(0) = 1 
        TDir(1) = Asc(".") 
        For I = 1 To 4 
          TDir(I + 1) = Asc(Mid$(RS, I, 1)) 
        Next I 
        flag = TMChangeDirectory(SHandle, state_buffer(0), 0, TDir(0)) 
        Dmmy = TMEndSession(SHandle) 
        If (flag > 0) Then 
            '// attempt to get the file list of the choosen device 
            '// recreate the select string 
            RS = "" 
            For flag = 7 To 0 Step -1 
                If (SelectROM(flag) < 16) Then 
                   RS = RS + "0" 
                End If 
                RS = RS + Hex$(SelectROM(flag)) 
            Next flag 
            GetFileList RS 
        End If 
    End If 
End Sub 
 
Sub EditFileData(RS As String) 
    Dim I, J, hndl, ln, flag, rslt As Integer 
    Dim Buf(7140) As Byte 
    Dim tstr As String 
     
    '// set result flag 
    rslt = False 
 
    '// record the selected file 
    For I = 0 To 3 
       SelectFile(I) = Asc(Mid$(RS, I + 1, 1)) 
    Next I 
    SelectFile(4) = Val(Right$(RS, 3)) 
 
    '// start a session 
    SHandle = TMExtendedStartSession(PortNum, PortType, vbNullString) 
 
    '// if SHandle valid then try to read the file to edit 
    If (SHandle > 0) Then 
        '// set the selected rom 
        flag = TMRom(SHandle, state_buffer(0), SelectROM(0)) 
        '// first change the directory to the correct current directory 
        '// need to do this because background task of listing roms has 
        '// reset the current directory 
        flag = TMChangeDirectory(SHandle, state_buffer(0), 0, DirBuf(0)) 
        '// try to open the file 
        hndl = TMOpenFile(SHandle, state_buffer(0), SelectFile(0)) 
        If (hndl >= 0) Then 
            '// file is open, so read 
            ln = TMReadFile(SHandle, state_buffer(0), hndl, Buf(0), 7140) 
            flag = TMCloseFile(SHandle, state_buffer(0), hndl) 
            If (ln >= 0) Then 
                '// valid read so display and edit 
                tstr = "" 
                For I = 0 To ln - 1 
                    tstr = tstr + Chr$(Buf(I)) 
                Next I 
                FileData.EditData.Text = tstr 
                SaveEditData = FileData.EditData.Text 
                '// construct a title for the form 
                I = 1 
                While (Mid$(RS, I, 1) <> " ") 
                    I = I + 1 
                Wend 
                FileData.Caption = Left$(RS, I - 1) + "." + Right$(RS, 3) + "    " + DirList.Caption 
                FileData.Left = DirList.Left + 1000 
                FileData.Top = DirList.Top + 1000 
                FileData.Show 
                rslt = True 
            End If 
        End If 
        '// close the opened session 
        flag = TMEndSession(SHandle) 
    Else 
        MsgBox "Could not get permission to use the 1-Wire", 16, "RAYVB" 
    End If 
 
    '// check result flag 
    If (Not rslt) Then 
        MsgBox "Could not read data file", 16, "RAYVB" 
    End If 
End Sub 
 
Sub GetFileList(RS As String) 
    Dim I, J, flag, rslt As Integer 
    Dim FileStr(50) As Byte 
    Dim TmpDir As String 
    Dim tstr As String 
 
    '// set result flag 
    rslt = False 
 
    '// record the selected file rom 
    For I = 0 To 7 
        SelectROM(I) = Val("&H" + Mid$(RS, (7 - I) * 2 + 1, 2)) 
    Next I 
 
    '// clear the Directory list 
    Do While (DirList.FileList.ListCount > 0) 
        DirList.FileList.RemoveItem 0 
    Loop 
 
    '// start a session 
    SHandle = TMExtendedStartSession(PortNum, PortType, vbNullString) 
 
    '// if SHandle valid then try to read directory 
    If (SHandle > 0) Then 
        '// set the selected rom 
        flag = TMRom(SHandle, state_buffer(0), SelectROM(0)) 
 
        '// read the directory to see if need to add back references 
        flag = TMChangeDirectory(SHandle, state_buffer(0), 1, DirBuf(0)) 
 
        '// set the title of the filelist to include rom number and current directory 
        TmpDir = Str$(PortNum) + ":\" 
        For I = 1 To DirBuf(0) 
            For J = 1 To 4 
                If (DirBuf(J + 1 + (I - 1) * 4) <> &H20) Then 
                    TmpDir = TmpDir + Chr$(DirBuf(J + 1 + (I - 1) * 4)) 
                Else 
                    Exit For 
                End If 
            Next J 
            TmpDir = TmpDir + "\" 
        Next I 
        DirList.Caption = RS + "  " + TmpDir 
 
        '// get first file 
        rslt = TMFirstFile(SHandle, state_buffer(0), FileStr(0)) 
        Do While (rslt >= 1) 
            '// check to see if file or subdirectory 
            If (FileStr(4) = &H7F) Then 
                '// check to see if directory is hidden 
                If (FileStr(7) <> 2) Then 
                    tstr = "" 
                    For J = 0 To 3 
                        tstr = tstr + Chr$(FileStr(J)) 
                    Next J 
                    '// decide where to place the new directory so that all directories at top 
                    DirList.FileList.AddItem (tstr + "  <DIR>") 
                End If 
            Else 
                tstr = "" 
                For J = 0 To 3 
                    tstr = tstr + Chr$(FileStr(J)) 
                Next J 
             
                DirList.FileList.AddItem (tstr + "   " + Format$(FileStr(4), "000")) 
            End If 
            rslt = TMNextFile(SHandle, state_buffer(0), FileStr(0)) 
        Loop 
     
        '// end the open session 
        flag = TMEndSession(SHandle) 
    End If 
 
    '// hide the file edit window 
    FileData.Hide 
     
    '// size and display the directory list box if rslt ok 
    If (rslt >= 0) Then 
        DirList.FileList.ListIndex = DirList.FileList.ListCount - 1 
        DirList.Left = RegNum.Left + 1000 
        DirList.Top = RegNum.Top + 1000 
        DirList.FileList.Height = 2895 
        DirList.Show 
    Else 
        DirList.Hide 
        MsgBox "Could not read file directory", 16, "RAYVB" 
    End If 
End Sub 
 
Sub ShowDS2409State(RS As String) 
    Dim I, J, flag, rslt As Integer 
    Dim FileStr(50) As Byte 
    Dim TmpDir As String 
    Dim tstr As String 
 
    '// set result flag 
    rslt = False 
 
    '// record the selected file rom 
    For I = 0 To 7 
        SelectROM(I) = Val("&H" + Mid$(RS, (7 - I) * 2 + 1, 2)) 
    Next I 
     
    '// start a session 
    SHandle = TMExtendedStartSession(PortNum, PortType, vbNullString) 
 
    '// if SHandle valid then try to read the file to edit 
    If (SHandle > 0) Then 
        '// set the selected rom 
        flag = TMRom(SHandle, state_buffer(0), SelectROM(0)) 
     
        '// Select the device 
        flag = TMStrongAccess(SHandle, state_buffer(0)) 
        If (flag = 1) Then 
            flag = TMTouchByte(SHandle, &H5A) 
            flag = TMTouchByte(SHandle, &HFF) 
            rslt = TMTouchByte(SHandle, &HFF) 
        Else 
            rslt = -1 
        End If 
             
        '// end the open session 
        flag = TMEndSession(SHandle) 
    End If 
 
    '// size and display the directory list box if rslt ok 
    If (rslt >= 0) Then 
        '// set the status label 
        DS2409Form.StatusLabel.Caption = "Raw Status = " + Hex$(rslt) + " hex " 
        '// set the button labels 
        If ((rslt And &H1) = &H1) Then 
            DS2409Form.MainButton.Caption = "OFF" 
        Else 
            DS2409Form.MainButton.Caption = "ON" 
        End If 
        If ((rslt And &H4) = &H4) Then 
            DS2409Form.AuxButton.Caption = "OFF" 
        Else 
            DS2409Form.AuxButton.Caption = "ON" 
        End If 
     
        '// show the window 
        DS2409Form.Caption = RS 
        DS2409Form.Left = RegNum.Left + 1000 
        DS2409Form.Top = RegNum.Top + 1000 
        DS2409Form.Show 
    Else 
        DS2409Form.Hide 
        MsgBox "Could not read DS2409 State", 16, "RAYVB" 
    End If 
End Sub 
 
Sub SendDS2409Command(cmd As Integer, num As Integer) 
    Dim I, J, flag, rslt As Integer 
    Dim FileStr(50) As Byte 
    Dim TmpDir As String 
    Dim tstr As String 
 
    '// set result flag 
    rslt = 0 
 
    '// record the selected file rom 
    For I = 0 To 7 
        SelectROM(I) = Val("&H" + Mid$(DS2409Form.Caption, (7 - I) * 2 + 1, 2)) 
    Next I 
     
    '// start a session 
    SHandle = TMExtendedStartSession(PortNum, PortType, vbNullString) 
 
    '// if SHandle valid then try to read the file to edit 
    If (SHandle > 0) Then 
        '// set the selected rom 
        flag = TMRom(SHandle, state_buffer(0), SelectROM(0)) 
     
        '// Select the device 
        flag = TMStrongAccess(SHandle, state_buffer(0)) 
        If (flag = 1) Then 
            '// send the command 
            flag = TMTouchByte(SHandle, cmd) 
            '// send num of dummy bytes 
            For I = 0 To num 
                flag = TMTouchByte(SHandle, &HFF) 
            Next I 
            '// get the confirmation byte 
            flag = TMTouchByte(SHandle, &HFF) 
            If (flag = cmd) Then 
                rslt = 1 
            End If 
        Else 
            rslt = -1 
        End If 
             
        '// end the open session 
        flag = TMEndSession(SHandle) 
    End If 
 
    '// size and display the directory list box if rslt ok 
    If (rslt < 0) Then 
        DS2409Form.Hide 
        MsgBox "Could not set DS2409 State", 16, "RAYVB" 
    End If 
End Sub 
 
'// Routine that is executed first 
' 
Sub Main() 
    Dim ps, ln As Integer 
    Dim Buf As String * 150 
    Dim NBuf As String 
    Dim cstring(150) As Byte 
    Dim I As Integer 
    Dim GlobKey As Long 
    Dim rtval As Integer 
    Dim RtType As Long 
    Dim RtSize As Long 
    Dim Dummy As Long 
     
    '// read the Registry to get the default PortNum and PortType 
    '// set defaults 
    MaxDbnc = 3 
    PortNum = 1 
    PortType = 1 
    '// read Registry for the PortNum 
    If (RegOpenKeyExA(HKEY_LOCAL_MACHINE, "Software\Dallas Semiconductor\iButton TMEX\3.00", 0, KEY_READ, GlobKey) = ERROR_SUCCESS) Then 
        RtSize = 150 
        If (RegQueryValueExA(GlobKey, "DefaultPortNum", vbNullString, RtType, cstring(0), RtSize) = ERROR_SUCCESS) Then 
            '// check to verify that the return type is string 
            If (RtType = REG_SZ) Then 
                NBuf = "" 
                I = 1 
                While (Asc(Mid(cstring, I, 1)) <> 0) 
                    NBuf = NBuf + Mid(cstring, I, 1) 
                    I = I + 1 
                Wend 
                PortNum = Val(NBuf) 
            End If 
        End If 
      '// release the key 
      Dummy = RegCloseKey(GlobKey) 
    End If 
    '// read Registry for the PortType 
    If (RegOpenKeyExA(HKEY_LOCAL_MACHINE, "Software\Dallas Semiconductor\iButton TMEX\3.00", 0, KEY_READ, GlobKey) = ERROR_SUCCESS) Then 
        RtSize = 150 
        If (RegQueryValueExA(GlobKey, "DefaultPortType", vbNullString, RtType, cstring(0), RtSize) = ERROR_SUCCESS) Then 
            '// check to verify that the return type is string 
            If (RtType = REG_SZ) Then 
                NBuf = "" 
                I = 1 
                While (Asc(Mid(cstring, I, 1)) <> 0) 
                    NBuf = NBuf + Mid(cstring, I, 1) 
                    I = I + 1 
                Wend 
                PortType = Val(NBuf) 
            End If 
        End If 
      '// release the key 
      Dummy = RegCloseKey(GlobKey) 
    End If 
     
     
    '// check the command line arguments 
    ln = Len(Command$) 
    ps = InStr(1, Command$, " ") 
    If (ln > 0) Then 
        If (ln = 1) Then 
            PortNum = Val(Command$) 
        ElseIf ((ln > 3) And (ps = 2)) Then 
            PortNum = Val(Left$(Command$, ps)) 
            MaxDbnc = Val(Right$(Command$, ln - ps)) 
        Else 
            MsgBox "Error in command line argument!", 16, "RAYVB" 
        End If 
    End If 
 
    '// get the main and hardware driver version 
    I = Get_Version(Buf) 
    NBuf = Left$(Buf, InStr(1, Buf, Chr$(0), 1) - 1) 
    RegNum.DriverLab.Caption = NBuf 
    If (TMGetTypeVersion(PortType, Buf) > 0) Then 
        NBuf = Left$(Buf, InStr(1, Buf, Chr$(0), 1) - 1) 
        RegNum.DriverLab.Caption = RegNum.DriverLab.Caption + Chr$(&HD) + NBuf 
    End If 
         
    RegNum.Caption = "Registration Numbers on Port " + Str$(PortNum) + " Type " + Str$(PortType) 
     
    '// size and display the registration number box 
    RegNum.Left = 1000 
    RegNum.Top = 1000 
    RegNum.RegList.Height = 2895 
    RegNum.Show 
End Sub 
 
'// Replace the file 'SaveFile' with the data in 
'// 'FileData.EditData.Txt' field. 
' 
Sub ReplaceFile() 
    Dim flag, I, J, hndl, ln, rslt  As Integer 
    Dim mln As Integer 
    Dim Epr As Integer 
    Dim ans As Integer 
    Dim Buf(7140) As Byte 
     
    '// result flag 
    rslt = False 
    Epr = False 
 
    '// start a session 
    SHandle = TMExtendedStartSession(PortNum, PortType, vbNullString) 
 
    '// if SHandle valid then try to read the file to edit 
    If (SHandle > 0) Then 
        '// set the selected rom 
        flag = TMRom(SHandle, state_buffer(0), SelectROM(0)) 
        '// first change the directory to the correct current directory 
        '// need to do this because background task of listing roms has 
        '// reset the current directory 
        flag = TMChangeDirectory(SHandle, state_buffer(0), 0, DirBuf(0)) 
 
        '// check for an program job 
        flag = TMCreateProgramJob(SHandle, state_buffer(0)) 
        If (flag = 1) Then Epr = True 
 
        '// delete the existing file 
        flag = TMDeleteFile(SHandle, state_buffer(0), SelectFile(0)) 
        If ((flag = 1) Or (flag = -6)) Then 
            '// success or delete of file already deleted 
            '// create the file to write 
            hndl = TMCreateFile(SHandle, state_buffer(0), mln, SelectFile(0)) 
            If (hndl >= 0) Then 
                '// success in create 
                ln = Len(FileData.EditData.Text) 
                For I = 1 To ln 
                    Buf(I - 1) = Asc(Mid$(FileData.EditData.Text, I, 1)) 
                Next I 
                flag = TMWriteFile(SHandle, state_buffer(0), hndl, Buf(0), ln) 
                If (flag = ln) Then 
                    '// success with write 
                    FileData.Hide 
                    rslt = True 
                End If 
            End If 
        End If 
 
        '// check to see if need to finish program job 
        If (Epr = True And rslt = True) Then 
            '// loop trying to write the device 
            '// until done or a user abort 
            Do 
                flag = TMDoProgramJob(SHandle, state_buffer(0)) 
                If (flag < 0) Then 
                    If (flag = -22) Then 
                        ans = MsgBox("Device is written such that updating is impossible.", 53, "RAYVB") 
                    ElseIf (flag = -23) Then 
                        ans = MsgBox("Program device can not be written with non-program devices on the 1-Wire.", 53, "RAYVB") 
                    ElseIf (flag = -1) Then 
                        ans = MsgBox("Device not found, replace and press retry!", 53, "RAYVB") 
                    ElseIf (flag = -200) Then 
                        SHandle = TMExtendedStartSession(PortNum, PortType, vbNullString) 
                    ElseIf (flag = -13) Then 
                        ans = MsgBox("Can not be program with this hardware or software configuration!", 53, "RAYVB") 
                    Else 
                        ans = MsgBox("Unknown error! " + Str$(flag), 53, "RAYVB") 
                    End If 
         
                    '// check result of message box 
                    If (ans = 2) Then  '// Cancel 
                        rslt = False 
                        '// reset the program job 
                        flag = TMCreateProgramJob(SHandle, state_buffer(0)) 
                        Exit Do 
                    End If 
                End If 
            Loop Until (flag = 1) 
        End If 
 
        '// end the session 
        flag = TMEndSession(SHandle) 
    End If 
 
    '// check result flag 
    If (Not rslt) Then 
        MsgBox "Could not write data file", 16, "RAYVB" 
    End If 
End Sub 
 
 
'----------------------------------------------------------------------------------------- 
'   /* read the default PortNum and PortType from the registry */ 
'   if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, 
'         "Software\\Dallas Semiconductor\\iButton TMEX\\3.00",0, 
'         KEY_READ,&GlobKey) == ERROR_SUCCESS) 
'   { 
'      /* attempt to read the PortNum */ 
'      size = 255; 
'      PortNum = 1; 
'      if (RegQueryValueEx(GlobKey,"DefaultPortNum",NULL,&dwType, 
'          &ReturnString[0],&size) == ERROR_SUCCESS) 
'      { 
'         if ((dwType == REG_SZ) || (size > 0)) 
'            PortNum = atoi(ReturnString); 
'      } 
'      /* attempt to read the PortType */ 
'      size = 255; 
'      PortType = 1; 
'      if (RegQueryValueEx(GlobKey,"DefaultPortType",NULL,&dwType, 
'          &ReturnString[0],&size) == ERROR_SUCCESS) 
'      { 
'         if ((dwType == REG_SZ) || (size > 0)) 
'            PortType = atoi(ReturnString); 
'      }' 
' 
'      // close the key 
'      RegCloseKey(GlobKey); 
'   } 
'   Else 
'     Exit_Prog("ERROR, Could not read the default PortNum and PortType from registry!\n", 
'               NO_DRIVERS);' 
' 
'   /* check argument to see if provided port number */ 
'   if (argc > 1) 
'       PortNum = atoi(argv[1]);' 
' 
'   /* check to see that indicated PortNum is  <= 15 */ 
'   if (PortNum > 15) 
'     Exit_Prog("ERROR, Indicated PortNum is not valid!\n", 
'               -XONE_WIRE_PORT_ERROR); 
' 
'   /* print a header */ 
'   printf("\nTDS1991 Version 3.10Alpha\n" 
'         "Dallas Semiconductor Corporation\n" 
'         "Copyright (C) 1992-1998\n\n"); 
'   printf("Port number: %d    Port type: %d\n",PortNum,PortType); 
'   Get_Version(tstr); 
'   printf("Main Driver: %s\n",tstr); 
'   printf("Type%d:",PortType); 
'   if (TMGetTypeVersion(PortType,tstr) < 0) 
'      Exit_Prog("No Hardware Driver for this type found!",NO_DRIVERS); 
'   printf(" %s\n\n\n",tstr);' 
' 
 