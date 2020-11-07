# DS1991SecureViewer
A little something to interface with DS1991 specific memory commands
This discontinued Multikey iButton networks similarily to others, but differs in its data transfer commands because of its three secure memory subkeys in lieu of memory pages. 

Like other iButtons, commands and data are to be sent starting at least significant byte and least significant bit. Therfore, responses from the button are in the same format (little-endian).

Subkey consists of 8 byte ID, 8 byte write-only password, and 48 byte secured memory. Recommended to write on the 64 byte scratchpad then verify before copy to subkey.