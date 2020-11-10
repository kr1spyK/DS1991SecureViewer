' //
' // www.pudn.com <http://www.pudn.com/> > ds199x.rar
' // <http://www.pudn.com/Download/item/id/266998.html> > DS1991DATA.frm,
' // change:2002-03-20,size:19215b
' // 

VERSION 5.00 
Begin VB.Form DS1991DATA  
   Caption         =   "Form2" 
   ClientHeight    =   9420 
   ClientLeft      =   60 
   ClientTop       =   345 
   ClientWidth     =   10200 
   LinkTopic       =   "Form2" 
   ScaleHeight     =   9420 
   ScaleWidth      =   10200 
   StartUpPosition =   3  'Windows Default 
   Begin VB.TextBox Op_message  
      Height          =   1935 
      Left            =   8040 
      TabIndex        =   27 
      Text            =   "Text2" 
      Top             =   6600 
      Width           =   1575 
   End 
   Begin VB.TextBox Dev_info  
      Height          =   1815 
      Left            =   8040 
      TabIndex        =   25 
      Text            =   "Text2" 
      Top             =   3720 
      Width           =   1575 
   End 
   Begin VB.TextBox Sys_info  
      Height          =   1695 
      Left            =   8040 
      TabIndex        =   23 
      Text            =   "Text2" 
      Top             =   1320 
      Width           =   1695 
   End 
   Begin VB.CommandButton Rd_Family  
      Caption         =   "RD_Family" 
      Height          =   615 
      Left            =   5280 
      TabIndex        =   22 
      Top             =   1320 
      Width           =   975 
   End 
   Begin VB.TextBox Family  
      Height          =   495 
      Left            =   1800 
      TabIndex        =   20 
      Text            =   "Text2" 
      Top             =   1320 
      Width           =   2775 
   End 
   Begin VB.TextBox SEC3  
      Height          =   375 
      Left            =   5280 
      TabIndex        =   16 
      Text            =   "Text2" 
      Top             =   7560 
      Width           =   1575 
   End 
   Begin VB.TextBox ID3  
      Height          =   375 
      Left            =   2520 
      TabIndex        =   15 
      Text            =   "Text2" 
      Top             =   7560 
      Width           =   1455 
   End 
   Begin VB.TextBox SEC2  
      Height          =   375 
      Left            =   5280 
      TabIndex        =   14 
      Text            =   "Text2" 
      Top             =   5760 
      Width           =   1575 
   End 
   Begin VB.TextBox ID2  
      Height          =   375 
      Left            =   2520 
      TabIndex        =   13 
      Text            =   "Text2" 
      Top             =   5760 
      Width           =   1335 
   End 
   Begin VB.TextBox SEC1  
      Height          =   375 
      Left            =   5160 
      TabIndex        =   12 
      Text            =   "Text2" 
      Top             =   3480 
      Width           =   1695 
   End 
   Begin VB.TextBox ID1  
      Height          =   375 
      Left            =   2400 
      TabIndex        =   11 
      Text            =   "Text2" 
      Top             =   3480 
      Width           =   1575 
   End 
   Begin VB.TextBox Text1  
      Height          =   735 
      Left            =   1800 
      TabIndex        =   7 
      Text            =   "Text1" 
      Top             =   8280 
      Width           =   5175 
   End 
   Begin VB.TextBox DATA2  
      Height          =   615 
      Left            =   1800 
      TabIndex        =   5 
      Text            =   "Text1" 
      Top             =   6600 
      Width           =   5055 
   End 
   Begin VB.TextBox DATA1  
      Height          =   615 
      Left            =   1800 
      TabIndex        =   3 
      Text            =   "Text1" 
      Top             =   4800 
      Width           =   5535 
   End 
   Begin VB.TextBox Scratch  
      Height          =   615 
      Left            =   1800 
      TabIndex        =   1 
      Top             =   2400 
      Width           =   5055 
   End 
   Begin VB.Label Label13  
      Caption         =   "Op_message" 
      Height          =   375 
      Left            =   8040 
      TabIndex        =   28 
      Top             =   6120 
      Width           =   1575 
   End 
   Begin VB.Label Label12  
      Caption         =   "Dev_version" 
      Height          =   495 
      Left            =   8040 
      TabIndex        =   26 
      Top             =   3240 
      Width           =   1335 
   End 
   Begin VB.Label Label11  
      Caption         =   "SoftVersion" 
      Height          =   375 
      Left            =   7920 
      TabIndex        =   24 
      Top             =   840 
      Width           =   1575 
   End 
   Begin VB.Label Label10  
      Caption         =   "Family:" 
      Height          =   495 
      Left            =   600 
      TabIndex        =   21 
      Top             =   1320 
      Width           =   735 
   End 
   Begin VB.Label Label9  
      Caption         =   "KEY3:" 
      Height          =   495 
      Left            =   4440 
      TabIndex        =   19 
      Top             =   7560 
      Width           =   615 
   End 
   Begin VB.Label Label8  
      Caption         =   "KEY2:" 
      Height          =   375 
      Left            =   4440 
      TabIndex        =   18 
      Top             =   5760 
      Width           =   615 
   End 
   Begin VB.Label Label7  
      Caption         =   "KEY1:" 
      Height          =   375 
      Left            =   4440 
      TabIndex        =   17 
      Top             =   3480 
      Width           =   615 
   End 
   Begin VB.Label Label6  
      Caption         =   "DI2:" 
      Height          =   495 
      Left            =   1920 
      TabIndex        =   10 
      Top             =   7560 
      Width           =   375 
   End 
   Begin VB.Label Label5  
      Caption         =   "DI2:" 
      Height          =   375 
      Left            =   1920 
      TabIndex        =   9 
      Top             =   5760 
      Width           =   495 
   End 
   Begin VB.Label Label4  
      Caption         =   "ID1:" 
      Height          =   495 
      Left            =   1680 
      TabIndex        =   8 
      Top             =   3480 
      Width           =   495 
   End 
   Begin VB.Label Label3  
      Caption         =   "DATA3:" 
      Height          =   615 
      Left            =   480 
      TabIndex        =   6 
      Top             =   8400 
      Width           =   735 
   End 
   Begin VB.Label Label2  
      Caption         =   "DATA2:" 
      Height          =   615 
      Left            =   480 
      TabIndex        =   4 
      Top             =   6600 
      Width           =   855 
   End 
   Begin VB.Label Data1_TXT  
      Caption         =   "DATA1:" 
      Height          =   495 
      Left            =   480 
      TabIndex        =   2 
      Top             =   4680 
      Width           =   855 
   End 
   Begin VB.Label Label1  
      Caption         =   "SCRATCH:" 
      Height          =   375 
      Left            =   480 
      TabIndex        =   0 
      Top             =   2640 
      Width           =   975 
   End 
End 
Attribute VB_Name = "DS1991DATA" 
Attribute VB_GlobalNameSpace = False 
Attribute VB_Creatable = False 
Attribute VB_PredeclaredId = True 
Attribute VB_Exposed = False 
Dim Data_Buff(64) As Byte 
Dim scratch_buff(64) As Byte 
Dim Call_statu As Boolean 
 
Private Sub DATA1_Change() 
 
End Sub 
 
Private Sub Form_Load() 
'The version string of the main driver can be read using the ��Get_Version�� API 
'function. The format for the main identification (ID) string is 
'"xx_DLLNAME_Vz.zz_month/day/year (FULLNAME.DLL)" where: 
'_ - represent spaces 
'xx hardware type code 00 to 99 that the DLL uses. 
'00 general DLL requiring a hardware specific type interrupt 
'01 COM port DLL 
'02 LPT port DLL 
'03 contain type 00,01, and 02 
'FF indicates a version 3.10 main driver that has a selectable and 
'configurable hardware drivers. 
'Declare Function Get_Version Lib "IBFS32.DLL" (ByVal ID_buf$) As Integer 
'Declare Function TMGetTypeVersion Lib "IBFS32.DLL" (ByVal HSType As Integer, ByVal ID_buf$) As Integer 
Dim ID_buff As String 
Dim DStype As Integer 
   rr = Get_Version(ID_buff) 
   If rr <> 0 Then 
       Sys_info.Text = ID_buff 
   End If 
'------------------- 
'��TMGetTypeVersion��. The format for the hardware specific ID string is 
'"tttt_HARDWARENAME_Vz.zz_month/day/year (FULLNAME.DLL)" where: 
'tttt - is the one to four letter short version of the hardware type. This short 
'name can be used by applications as an indicator of the port type such as' 
'��COM�� for the DS9097E COM port driver. 
'HARDWARENAME - is the name of the hardware such as the DS9097E or 
'DS1410E 1-wire adapters. 
'The rest of the fields in the ID string represent the same as in the main ID 
'string. 
'------------------- 
'short far pascal TMGetTypeVersion(short HSType, char far *ID_buf); 
'The function returns: 
'1 => ID string is in ID_buf. 
'0 => error could not get the ID 
'<0 => HARDWARE_SPECIFIC error 
   rr = TMGetTypeVersion(DStype, ID_buff) 
   If rr = 1 Then 
     Dev_info.Text = ID_buff 
   End If 
    
End Sub 
 
Private Sub Rd_Family_Click() 
'      /* attempt to get a session */ 
      session_handle = TMExtendedStartSession(PortNum, PortType, Null) 
      If (session_handle > 0) Then 
'         /* setup the MicroLan */ 
         If (TMSetup(session_handle) = 1) Then 
'            /* look for the first DS1991 (family type 0x02) */ 
             If (FindFirstFamily(&H2, session_handle)) Then 
'               /* read the contents of the DS1991 iButton found  */ 
               Call DumpDs1991 
'               /* failed to read device */ 
             Else 
                Op_message.Text = "Device read failed!" 
             End If 
'            /*  iButton not on MicroLan */ 
          Else 
               Op_message.Text = "DS1991 not found on MicroLan!" 
          End 
'         /* MicroLan port not valid */ 
       Else 
             Op_message.Text = "MicroLan setup failed!" 
       End If 
'   /* end the session if one is open */ 
    TMEndSession (session_handle) 
'   /* close the 1-Wire and exit the program (1.10) */ 
'   Exit_Prog("",NORMAL_EXIT); 
End Sub 
 
Sub DumpDs1991() 
Dim i, Sub_data As Integer 
Dim buff(64) As Byte 
Dim bf2, subi 
Dim mess As String 
' *  Dump the contents of the DS1991 found 
' */ 
'Short DumpDS1991(void) 
'{ 
'   uchar buff[64]; 
'   short i,sub; 
' 
'   // read the scratchpad 
   Call ReadDS1991Scratchpad 
   If Call_statu = True Then 
'   { 
    Buf2 = CStr(scratch_buff(0)) + " " 
    For i = 1 To 63 
        Buf2 = Buf2 + CStr(scratch_buff(1)) + " " 
    Next i 
    Scratch.Text = Buf2 
    Call_statu = True 
   Else 
    Call_statu = False 
'      return FALSE;' 
   End If 
' 
'   // read each subkey 
'   for (sub = 0; sub < 3; sub++) 
'   For subi = 0 To 2 
'   { 
'      // set the password to all U's 
'     for (i = 0; i < 8; i++) 
      For i = 0 To 7 
         buff(8 + i) = "U" 
      Next i 
         Call ReadDS1991Subkey(buff, 0) 
         If Call_statu = True Then 
         'mess = "Subkey " 
         'mess = mess + "ID: " 
         mess = CStr(buff(0)) + " " + CStr(buff(1)) + " " + CStr(buff(2)) + " " + CStr(buff(3)) + " " + CStr(buff(4)) + " " + CStr(buff(5)) + " " + CStr(buff(6)) + " " + CStr(buff(7)) + " " 
         ID1.Text = mess 
'         printf("\n   Password used: "); 
'         for (i = 8; i < 16; i++) 
'            printf("%02X",buff[i]); 
         mess = CStr(buff(8)) + " " + CStr(buff(9)) + " " + CStr(buff(10)) + " " + CStr(buff(11)) + " " + CStr(buff(12)) + " " + CStr(buff(13)) + " " + CStr(buff(14)) + " " + CStr(buff(15)) 
         SEC1.Text = mess 
         mess = CStr(buff(16)) + " " + CStr(buff(17)) + " " + CStr(buff(18)) + " " + CStr(buff(19)) + " " + CStr(buff(20)) + " " + CStr(buff(21)) + " " + CStr(buff(22)) + " " + CStr(buff(23)) 
         mess = mess + CStr(buff(24)) + " " + CStr(buff(25)) + " " + CStr(buff(26)) + " " + CStr(buff(27)) + " " + CStr(buff(28)) + " " + CStr(buff(29)) + " " + CStr(buff(30)) + " " + CStr(buff(31)) 
         mess = mess + CStr(buff(32)) + " " + CStr(buff(33)) + " " + CStr(buff(34)) + " " + CStr(buff(35)) + " " + CStr(buff(36)) + " " + CStr(buff(37)) + " " + CStr(buff(38)) + " " + CStr(buff(39)) 
         mess = mess + CStr(buff(40)) + " " + CStr(buff(41)) + " " + CStr(buff(42)) + " " + CStr(buff(43)) + " " + CStr(buff(44)) + " " + CStr(buff(45)) + " " + CStr(buff(46)) + " " + CStr(buff(47)) 
         mess = mess + CStr(buff(48)) + " " + CStr(buff(49)) + " " + CStr(buff(50)) + " " + CStr(buff(51)) + " " + CStr(buff(52)) + " " + CStr(buff(53)) + " " + CStr(buff(54)) + " " + CStr(buff(55)) 
         mess = mess + CStr(buff(56)) + " " + CStr(buff(57)) + " " + CStr(buff(58)) + " " + CStr(buff(59)) + " " + CStr(buff(60)) + " " + CStr(buff(61)) + " " + CStr(buff(62)) + " " + CStr(buff(63)) 
         DATA1.Text = mess 
      For i = 0 To 7 
         buff(8 + i) = "U" 
      Next i 
         Call ReadDS1991Subkey(buff, 1) 
         If Call_statu = True Then 
         'mess = "Subkey " 
         'mess = mess + "ID: " 
         mess = CStr(buff(0)) + " " + CStr(buff(1)) + " " + CStr(buff(2)) + " " + CStr(buff(3)) + " " + CStr(buff(4)) + " " + CStr(buff(5)) + " " + CStr(buff(6)) + " " + CStr(buff(7)) + " " 
         ID2.Text = mess 
'         printf("\n   Password used: "); 
'         for (i = 8; i < 16; i++) 
'            printf("%02X",buff[i]); 
         mess = CStr(buff(8)) + " " + CStr(buff(9)) + " " + CStr(buff(10)) + " " + CStr(buff(11)) + " " + CStr(buff(12)) + " " + CStr(buff(13)) + " " + CStr(buff(14)) + " " + CStr(buff(15)) 
         SEC2.Text = mess 
         mess = CStr(buff(16)) + " " + CStr(buff(17)) + " " + CStr(buff(18)) + " " + CStr(buff(19)) + " " + CStr(buff(20)) + " " + CStr(buff(21)) + " " + CStr(buff(22)) + " " + CStr(buff(23)) 
         mess = mess + CStr(buff(24)) + " " + CStr(buff(25)) + " " + CStr(buff(26)) + " " + CStr(buff(27)) + " " + CStr(buff(28)) + " " + CStr(buff(29)) + " " + CStr(buff(30)) + " " + CStr(buff(31)) 
         mess = mess + CStr(buff(32)) + " " + CStr(buff(33)) + " " + CStr(buff(34)) + " " + CStr(buff(35)) + " " + CStr(buff(36)) + " " + CStr(buff(37)) + " " + CStr(buff(38)) + " " + CStr(buff(39)) 
         mess = mess + CStr(buff(40)) + " " + CStr(buff(41)) + " " + CStr(buff(42)) + " " + CStr(buff(43)) + " " + CStr(buff(44)) + " " + CStr(buff(45)) + " " + CStr(buff(46)) + " " + CStr(buff(47)) 
         mess = mess + CStr(buff(48)) + " " + CStr(buff(49)) + " " + CStr(buff(50)) + " " + CStr(buff(51)) + " " + CStr(buff(52)) + " " + CStr(buff(53)) + " " + CStr(buff(54)) + " " + CStr(buff(55)) 
         mess = mess + CStr(buff(56)) + " " + CStr(buff(57)) + " " + CStr(buff(58)) + " " + CStr(buff(59)) + " " + CStr(buff(60)) + " " + CStr(buff(61)) + " " + CStr(buff(62)) + " " + CStr(buff(63)) 
         DATA1.Text = mess 
       ' 
       For i = 0 To 7 
         buff(8 + i) = "U" 
       Next i 
         Call ReadDS1991Subkey(buff, 2) 
         If Call_statu = True Then 
         'mess = "Subkey " 
         'mess = mess + "ID: " 
         mess = CStr(buff(0)) + " " + CStr(buff(1)) + " " + CStr(buff(2)) + " " + CStr(buff(3)) + " " + CStr(buff(4)) + " " + CStr(buff(5)) + " " + CStr(buff(6)) + " " + CStr(buff(7)) + " " 
         ID3.Text = mess 
'         printf("\n   Password used: "); 
'         for (i = 8; i < 16; i++) 
'            printf("%02X",buff[i]); 
         mess = CStr(buff(8)) + " " + CStr(buff(9)) + " " + CStr(buff(10)) + " " + CStr(buff(11)) + " " + CStr(buff(12)) + " " + CStr(buff(13)) + " " + CStr(buff(14)) + " " + CStr(buff(15)) 
         SEC3.Text = mess 
         mess = CStr(buff(16)) + " " + CStr(buff(17)) + " " + CStr(buff(18)) + " " + CStr(buff(19)) + " " + CStr(buff(20)) + " " + CStr(buff(21)) + " " + CStr(buff(22)) + " " + CStr(buff(23)) 
         mess = mess + CStr(buff(24)) + " " + CStr(buff(25)) + " " + CStr(buff(26)) + " " + CStr(buff(27)) + " " + CStr(buff(28)) + " " + CStr(buff(29)) + " " + CStr(buff(30)) + " " + CStr(buff(31)) 
         mess = mess + CStr(buff(32)) + " " + CStr(buff(33)) + " " + CStr(buff(34)) + " " + CStr(buff(35)) + " " + CStr(buff(36)) + " " + CStr(buff(37)) + " " + CStr(buff(38)) + " " + CStr(buff(39)) 
         mess = mess + CStr(buff(40)) + " " + CStr(buff(41)) + " " + CStr(buff(42)) + " " + CStr(buff(43)) + " " + CStr(buff(44)) + " " + CStr(buff(45)) + " " + CStr(buff(46)) + " " + CStr(buff(47)) 
         mess = mess + CStr(buff(48)) + " " + CStr(buff(49)) + " " + CStr(buff(50)) + " " + CStr(buff(51)) + " " + CStr(buff(52)) + " " + CStr(buff(53)) + " " + CStr(buff(54)) + " " + CStr(buff(55)) 
         mess = mess + CStr(buff(56)) + " " + CStr(buff(57)) + " " + CStr(buff(58)) + " " + CStr(buff(59)) + " " + CStr(buff(60)) + " " + CStr(buff(61)) + " " + CStr(buff(62)) + " " + CStr(buff(63)) 
         DATA1.Text = mess 
End Sub 
 
Sub ReadDS1991Scratchpad() 
'/*-------------------------------------------------------------------------- 
' * This procedure reads the 64 byte scratchpad of the DS1991 currently 
'' * selected. 
' * 
' * Note: port must be owned with a TMExtendedStartSession before 
' *       calling this function. 
' */ 
'Short ReadDS1991Scratchpad(uchar * scratch_buff) 
'{ 
 '   unsigned char tran_buf[100]; 
 Dim tran_buf(100) As Byte 
 '  short tran_len=0,result,i;' 
 Dim tran_len, result, i As Integer 
 tran_len = 0 
 '   /* access the current device */ 
 result = TMAccess(session_handle, state_buffer) 
' 
    If (result = 1) Then 
 '   { 
 '     /* send read scratchpad command to part */ 
      tran_buf(tran_len) = &H69   'comman for reading scratchpad 
      tran_len = tran_len + 1 
 '     /* indicate reading scratchpad starting at address 0 */ 
      tran_buf(tran_len) = &HC0 
      tran_len = tran_len + 1 
 '     /* confirm starting address/scratchpad read */ 
      tran_buf(tran_len) = &H3F 
      tran_len = tran_len + 1 
 '     /* read scratchpad data */ 
 '     for (i = 0; i < 64; i++) 
 For i = 3 To 63 
         tran_buf(tran_len) = &HFF 
 Next i 
'      /* transfer the block */ 
'TMBlockStream 
'This API call is a general purpose block transfer function This function simply does a 
'stream of TMTouchByte (s) of all of the 'num_tran' bytes in the 'tran_buffer' data buffer. 
'The values returned from the TMTouchByte(s) are placed back into the 'tran_buffer' 
'data buffer. This call returns a byte length greater than or equal to 0 for success or one 
'of the HARDWARE SPECIFIC error values described for a failure. This API call is 
'similar to TMBlockIO but without the TMTouchReset at the beginning of 
'communication. The maximum number of 'num_tran' is 1023 bytes. 
'Microsoft Windows TMEX DLL function prototype: 
'short far pascal TMBlockStream(long session_handle, unsigned char far 
'*tran_buffer, short num_tran); 
'The function returns: 
'>=0 => length of data sent and received from MicroLan 
'<0 => a TRANSPORT error has occurred 
 
      result = TMBlockStream(session_handle, tran_buf, tran_len) 
      If (result = tran_len) Then 
'      { 
'         /* copy the result to the scratch buffer */ 
'         for (i = 0; i < 64; i++) 
          For i = 0 To 63 
            scratch_buff(i) = tran_buf(i) 
          Next i 
          Call_statu = True 
       Else 
          Call_statu = False 
       End If 
'         return TRUE; 
'      } 
'   } 
' 
'   return FALSE; 
'}' 
' 
End Sub 
 