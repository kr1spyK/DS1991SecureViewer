'/*
' * www.pudn.com <http://www.pudn.com/> > ds199x.rar
' * <http://www.pudn.com/Download/item/id/266998.html> > DS1919TEMP.frm,
' * change:2002-03-19,size:13471b
' */

VERSION 5.00 
Begin VB.Form Form1  
   Caption         =   "Form1" 
   ClientHeight    =   3195 
   ClientLeft      =   60 
   ClientTop       =   345 
   ClientWidth     =   4680 
   LinkTopic       =   "Form1" 
   ScaleHeight     =   3195 
   ScaleWidth      =   4680 
   StartUpPosition =   3  'Windows Default 
End 
Attribute VB_Name = "Form1" 
Attribute VB_GlobalNameSpace = False 
Attribute VB_Creatable = False 
Attribute VB_PredeclaredId = True 
Attribute VB_Exposed = False 
Private Sub Form_Load() 
'/*-------------------------------------------------------------------------- 
' * Copyright (C) 1992,1993,1994,1995,1996 Dallas Semiconductor Corporation. 
' * All rights Reserved. Printed in U.S.A. 
' * This software is protected by copyright laws of 
' * the United States and of foreign countries. 
' * This material may also be protected by patent laws of the United States 
' * and of foreign countries. 
' * This software is furnished under a license agreement and/or a 
' * nondisclosure agreement and may only be used or copied in accordance 
' * with the terms of those agreements. 
' * The mere transfer of this software does not imply any licenses 
' * of trade secrets, proprietary technology, copyrights, patents, 
' * trademarks, maskwork rights, or any other form of intellectual 
' * property whatsoever. Dallas Semiconductor retains all ownership rights. 
' *-------------------------------------------------------------------------- 
' * 
' * TDS1991.C - Demonstration program to find and dump the contents of a 
' *             DS1991 (DS1425). 
' * 
' * Compiler:  Microsoft Visual C 5.0 
' * Externals: iBTMEX.h 
' * Version: 3.10Alpha 
' * 
' */ 
' 
'#define TMEXUTIL 
' 
'#include "iBTMEXCW.H" 
' 
'/* Local Function Prototypes */ 
'short FindFirstFamily(short, long); 
'void Exit_Prog(char *, short); 
'short ReadDS1991Scratchpad(uchar *); 
'short WriteDS1991Scratchpad(uchar *); 
'short ReadDS1991Subkey(uchar *, short); 
'short DumpDS1991(void); 
' 
'/* Global Variables to hold list information */ 
'short PortType, PortNum; 
'uchar state_buffer[5120]; 
'long session_handle; 
 
'/*---------------------------------------------------------------------- 
' *  This is the Main routine for TDS1991. 
' */ 
'void main(short argc, char **argv) 
'{ 
'   short State=0,printmsg=1,listnum=0,currentrom=0; 
'   char tstr[80]; 
'   HKEY GlobKey; 
'   DWORD dwType,size; 
'   char ReturnString[256];' 
'' 
' 
'   /* check arguments to see if request instruction with '?' or too many */ 
'   if ((argc > 2) && (argv[1][0] == '?' || argv[1][1] == '?')) 
'   { 
'       printf("\nusage: TDS1991 <PortNum> \n" 
'              "  - view contents of DS1991\n" 
'              "  - <optional> argument 1 specifies the port number\n" 
'              "  - version 3.10Alpha\n"); 
'       Exit_Prog("",BAD_ARGUMENT); 
'   } 
' 
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
'   // loop to get a session handle and to a temperature conversion */ 
'   for (;;) 
'   { 
'      /* attempt to get a session */ 
'      session_handle = TMExtendedStartSession(PortNum,PortType,NULL); 
'      if (session_handle > 0) 
'      { 
'         /* setup the MicroLan */ 
'         if (TMSetup(session_handle) == 1) 
'         { 
'            printf("Searching for a DS1991...\n\n"); 
 ' 
'            /* look for the first DS1991 (family type 0x02) */ 
'            if (FindFirstFamily(0x02,session_handle)) 
'            { 
'               /* read the contents of the DS1991 iButton found  */ 
'               if (DumpDS1991() == 1) 
'                  printf("DS1991 display complete\n"); 
'               /* failed to read device */ 
'               Else 
'                  printf("Device read failed!"); 
' 
'               MessageBeep(MB_ICONEXCLAMATION); 
'               break; 
'            } 
'            /*  iButton not on MicroLan */ 
'            Else 
'            { 
'               printf("DS1991 not found on MicroLan!"); 
'               break; 
'            } 
'         } 
'         /* MicroLan port not valid */ 
'         Else 
'         { 
'            printf("MicroLan setup failed!"); 
'            break; 
'         } 
'      } 
'      else if (session_handle == 0) 
'      { 
'         /* check if need to print the waiting message */ 
'         if (printmsg) 
'         { 
'            printmsg = FALSE; 
'            printf("\nWaiting to get access to the MicroLan\n"); 
'         } 
'      } 
'      else if (session_handle < 0) 
'      { 
'         MessageBeep(MB_ICONEXCLAMATION); 
'         printf("No Hardware Driver for this type found!"); 
'         break; 
'      } 
'   } 
' 
'   /* end the session if one is open */ 
'   TMEndSession(session_handle);' 
' 
'   /* close the 1-Wire and exit the program (1.10) */ 
'   Exit_Prog("",NORMAL_EXIT); 
'}' 
' 
'/*-------------------------------------------------------------------------- 
' *  Dump the contents of the DS1991 found 
' */ 
'Short DumpDS1991(void) 
'{ 
'   uchar buff[64]; 
'   short i,sub; 
' 
'   // read the scratchpad 
'   if (ReadDS1991Scratchpad(buff)) 
'   { 
'      printf("ScratchPad:\n  "); 
'      for (i = 0; i < 64; i++) 
'      { 
'         printf("%02X",buff[i]); 
'         if (((i + 1) % 32) == 0) 
'            printf("\n  "); 
'      } 
'      printf("\n"); 
'   } 
'   Else 
'      return FALSE;' 
' 
'   // read each subkey 
'   for (sub = 0; sub < 3; sub++) 
'   { 
'      // set the password to all U's 
 '     for (i = 0; i < 8; i++) 
'         buff[8+i] = 'U';' 
' 
'      if (ReadDS1991Subkey(buff, sub)) 
'      { 
'         printf("Subkey %d:\n   ",sub); 
'         printf("ID: "); 
'         for (i = 0; i < 8; i++) 
'            printf("%02X",buff[i]); 
'         printf("\n   Password used: "); 
'         for (i = 8; i < 16; i++) 
'            printf("%02X",buff[i]); 
'         printf("\n   Data read: "); 
'         for (i = 16; i < 64; i++) 
'         { 
'            printf("%02X",buff[i]); 
'            if (((i - 15) % 16) == 0) 
'               printf("\n              "); 
'         } 
'         printf("\n"); 
'      } 
'      Else 
'         return FALSE; 
'   }' 
' 
'   return TRUE; 
'}' 
 
'/*-------------------------------------------------------------------------- 
' *  Prints a message, beeps and exits the program. 
' */ 
'void Exit_Prog(char *msg, short errcd) 
'{ 
'   short i;' 
' 
'   /*  check to see if any characters to read */ 
'   for (i = 0; i < 2; i++) 
'      if (kbhit()) getch();' 
' 
'   /* print the message left justified */ 
'   printf("\r%s",msg);' 
' 
'   /*  error beep if return code is not 0 */ 
'   if (errcd != NORMAL_EXIT) 
'     Beep(300,600); 
' 
' '  /*  return error code passed */ 
'   exit(errcd); 
'} 
'' 
' 
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
 '  short tran_len=0,result,i;' 
 
 '   /* access the current device */ 
 '   result = TMAccess(session_handle, state_buffer);' 
' 
'    if (result == 1) 
 '   { 
 '     /* send read scratchpad command to part */ 
 '     tran_buf[tran_len++] = 0x69; 
 '     /* indicate reading scratchpad starting at address 0 */ 
 '     tran_buf[tran_len++] = 0xC0; 
 '     /* confirm starting address/scratchpad read */ 
 '     tran_buf[tran_len++] = 0x3F; 
 '     /* read scratchpad data */ 
 '     for (i = 0; i < 64; i++) 
 '        tran_buf[tran_len++] = 0xFF; 
' 
'      /* transfer the block */ 
'      result = TMBlockStream(session_handle, tran_buf, tran_len); 
' 
'      if (result == tran_len) 
'      { 
'         /* copy the result to the scratch buffer */ 
'         for (i = 0; i < 64; i++) 
'            scratch_buff[i] = tran_buf[tran_len - 64 + i];'' 
 
'         return TRUE; 
'      } 
'   } 
' 
'   return FALSE; 
'}' 
' 
'/*-------------------------------------------------------------------------- 
' * This procedure writes the 64 byte scratchpad of the DS1991 currently 
' * selected.  Write not verified, use ReadDS1991Scratchpad 
' * 
' * Note: port must be owned with a TMExtendedStartSession before 
' *       calling this function. 
' */ 
'Short WriteDS1991Scratchpad(uchar * scratch_buff) 
'{ 
'    unsigned char tran_buf[100]; 
'   short tran_len=0,result,i;' 
' 
'    /* access the current device */ 
'    result = TMAccess(session_handle, state_buffer);' 
' 
'    if (result == 1) 
'    { 
'      /* write scratchpad command byte */ 
'      tran_buf[tran_len++] = 0x96; 
'      /* indicate write scratchpad starting at address 0 */ 
'      tran_buf[tran_len++] = 0xC0; 
 '     /* confirmation of previous byte sent */ 
'      tran_buf[tran_len++] = 0x3F; 
'      /* write scratchpad data */ 
'      for (i = 0; i < 64; i++) 
'         tran_buf[tran_len++] = scratch_buff[i];' 
' 
'      /* transfer the block */ 
'      result = TMBlockStream(session_handle, tran_buf, tran_len); 
 ' 
 '     if (result == tran_len) 
 '        return TRUE; 
 '  } 
' 
'   return FALSE; 
'}' 
' 
'/*-------------------------------------------------------------------------- 
' * This procedure reads a 64 byte subkey of the DS1991 currently 
' * selected. 
' * 
' * Note: port must be owned with a TMExtendedStartSession before 
' *       calling this function. 
' */ 
'short ReadDS1991Subkey(uchar *subkey_buff, short keynum) 
'{ 
'    unsigned char tran_buf[100]; 
'   short tran_len=0,result,i;' 
' 
'    /* access the current device */ 
'    result = TMAccess(session_handle, state_buffer);'' 
 
'    if (result == 1) 
'    { 
'      /* read secure subkey command */ 
'      tran_buf[tran_len++] = 0x66; 
'      /* specify subkey number/starting addr */ 
'      tran_buf[tran_len++] = (keynum << 6) | 0x10; 
'      /* confirmation of last byte */ 
'      tran_buf[tran_len++] = ~((keynum << 6) | 0x10); 
'      /* read id field */ 
'      for (i = 0; i < 8; i++) 
 '        tran_buf[tran_len++] = 0xFF; 
'      /* send password */ 
'      for (i = 0; i < 8; i++) 
'         tran_buf[tran_len++] = subkey_buff[8 + i]; 
'      /* read sec. data */ 
'      for (i = 0; i < 48; i++) 
'         tran_buf[tran_len++] = 0xFF; 
' 
'      /* transfer the block */ 
'      result = TMBlockStream(session_handle, tran_buf, tran_len); 
' 
'      if (result == tran_len) 
'      { 
'         /* copy the result to the read subkey */ 
'         for (i = 0; i < 64; i++) 
'            subkey_buff[i] = tran_buf[tran_len - 64 + i];'' 
 
'         return TRUE; 
'      } 
'   } 
' 
'   return FALSE; 
'}' 
'' 
' 
'/*---------------------------------------------------------------------- 
' * Find the first device with a particular family 
' */ 
'short FindFirstFamily(short family, long session_handle) 
'{ 
'   short j; 
'   short ROM[9]; 
' 
'   /* set up to find the first device with family 'family */ 
'   if (TMFamilySearchSetup(session_handle,&state_buffer,family) == 1) 
'   { 
'      /* get first device in list with the specified family */ 
'      if (TMNext(session_handle,state_buffer) == 1) 
'      { 
'         /* read the rom number */ 
'         ROM[0] = 0; 
'         TMRom(session_handle,state_buffer,ROM); 
' 
'         /* check if correct type */ 
'         if ((family & 0x7F) == (ROM[0] & 0x7F)) 
'         { 
'            printf("Serial ROM ID: "); 
'            for (j = 7; j >= 0; j--) 
'               printf("%02X",ROM[j]); 
'            printf("\n\n"); 
'            return 1; 
'         } 
'      } 
'   } 
' 
'   /* failed to find device of that type */ 
'   return 0; 
'} 
 
 
End Sub 
