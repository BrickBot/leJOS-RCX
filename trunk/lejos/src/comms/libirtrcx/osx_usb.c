/*
 *  $Id$
 *  RCX communication routines for USB on OS X.
 *  By Markus Strickler <markus@braindump.ms>
 *
 *  The contents of this file are subject to the Mozilla Public License
 *  Version 1.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS"
 *  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 *  License for the specific language governing rights and limitations
 *  under the License.
 *
 *
 *  This code builds heavily on the Firmdl Code, released October 3, 1998.
 *
 *  The Initial Developer of the Original Code is Kekoa Proudfoot.
 *  Portions created by Kekoa Proudfoot are Copyright (C) 1998, 1999
 *  Kekoa Proudfoot. All Rights Reserved.
 *
 *  OS X specific code taken in parts from Dave Baum's OS X implementation of NCQ.
 *
 */

#include "osx_usb.h"

/* The read buffer */
unsigned char			gInBuffer[8];
unsigned char*			gReadBufferPtr;
unsigned char*			gInBufferStart;
unsigned char* 			gInBufferEnd;
int 				gReadRemain;
int 				gReadDone;
short 				LegoUSBVendorID = 1684;
short 				LegoUSBProductID = 1;

extern int 			__comm_debug;

mach_port_t			gMasterPort;
IOUSBDeviceInterface		**dev = NULL;

int osx_usb_nbread (IOUSBInterfaceInterface **intf, void *buf, int maxlen, int timeout);

/* Callback for async read */
void osx_usb_readComplete(void *refCon, IOReturn result, void *arg0);

IOReturn ConfigureDevice(IOUSBDeviceInterface **dev) {
    UInt8				numConfig;
    IOReturn				result;
    IOUSBConfigurationDescriptorPtr	configDesc;

    //Get the number of configurations
    result = (*dev)->GetNumberOfConfigurations(dev, &numConfig);
    if (!numConfig) {
        return -1;
    }

    // Get the configuration descriptor
    result = (*dev)->GetConfigurationDescriptorPtr(dev, 0, &configDesc);
    if (result) {
        printf("Couldn't get configuration descriptior for index %d (err=%08x)\n", 0, result);
        return -1;
    }

#ifdef OSX_DEBUG
    printf("Numer of Configurations: %d\n", numConfig);
#endif
    
    // Configure the device
    result = (*dev)->SetConfiguration(dev, configDesc->bConfigurationValue);
    if (result)
    {
        printf("Unable to set configuration to value %d (err=%08x)\n", 0, result);
        return -1;
    }

    return kIOReturnSuccess;
}

IOReturn FindInterfaces(IOUSBDeviceInterface **dev, IOUSBInterfaceInterface ***itf)
{
    IOReturn			kr;
    IOUSBFindInterfaceRequest	request;
    io_iterator_t		iterator;
    io_service_t		usbInterface;
    IOUSBInterfaceInterface 	**intf = NULL;
    IOCFPlugInInterface 	**plugInInterface = NULL; 
    HRESULT 			res;
    SInt32 			score;
    UInt8			intfClass;
    UInt8			intfSubClass;
    UInt8			intfNumEndpoints;
    int				pipeRef;
    CFRunLoopSourceRef		runLoopSource;

    request.bInterfaceClass = kIOUSBFindInterfaceDontCare;
    request.bInterfaceSubClass = kIOUSBFindInterfaceDontCare;
    request.bInterfaceProtocol = kIOUSBFindInterfaceDontCare;
    request.bAlternateSetting = kIOUSBFindInterfaceDontCare;

    kr = (*dev)->CreateInterfaceIterator(dev, &request, &iterator);

    usbInterface = IOIteratorNext(iterator);
    IOObjectRelease(iterator);
#ifdef OSX_DEBUG
    printf("Interface found.\n");
#endif
    kr = IOCreatePlugInInterfaceForService(usbInterface, kIOUSBInterfaceUserClientTypeID, kIOCFPlugInInterfaceID, &plugInInterface, &score);
    kr = IOObjectRelease(usbInterface); // done with the usbInterface object now that I have the plugin
    if ((kIOReturnSuccess != kr) || !plugInInterface)
    {
        printf("unable to create a plugin (%08x)\n", kr);
        return -1;
    }

    // I have the interface plugin. I need the interface interface
    res = (*plugInInterface)->QueryInterface(plugInInterface, CFUUIDGetUUIDBytes(kIOUSBInterfaceInterfaceID), (LPVOID) &intf);
    (*plugInInterface)->Release(plugInInterface);			// done with this
    if (res || !intf)
    {
        printf("couldn't create an IOUSBInterfaceInterface (%08x)\n", (int) res);
        return -1;
    }

    // Now open the interface. This will cause the pipes to be instantiated that are
    // associated with the endpoints defined in the interface descriptor.
    kr = (*intf)->USBInterfaceOpen(intf);
    if (kIOReturnSuccess != kr)
    {
        printf("unable to open interface (%08x)\n", kr);
        (void) (*intf)->Release(intf);
        return -1;
    }

    kr = (*intf)->CreateInterfaceAsyncEventSource(intf, &runLoopSource);
    if (kIOReturnSuccess != kr)
    {
        printf("unable to create async event source (%08x)\n", kr);
        (void) (*intf)->USBInterfaceClose(intf);
        (void) (*intf)->Release(intf);
        return -1;
    }
    CFRunLoopAddSource(CFRunLoopGetCurrent(), runLoopSource, kCFRunLoopDefaultMode);
    

    if (!intf) {
        printf("Interface is NULL!\n");
    } else {
        *itf = intf;
    }
    return kr;
}

void FindDevice(void *refCon, io_iterator_t iterator) {
    kern_return_t		kr;
    io_service_t		usbDevice;
    IOCFPlugInInterface		**plugInInterface = NULL;
    HRESULT			result;
    SInt32			score;
    UInt16			vendor;
    UInt16			product;
    UInt16			release;

#ifdef OSX_DEBUG
    printf("Searching Device....\n");
#endif
    while (usbDevice = IOIteratorNext(iterator)) {
        // create intermediate plug-in
#ifdef OSX_DEBUG
        printf("Found a device!\n");
#endif
        kr = IOCreatePlugInInterfaceForService(usbDevice,
                                               kIOUSBDeviceUserClientTypeID,
                                               kIOCFPlugInInterfaceID,
                                               &plugInInterface, &score);
        kr = IOObjectRelease(usbDevice);
        if ((kIOReturnSuccess != kr) || !plugInInterface) {
            printf("Unable to create a plug-in (%08x)\n", kr);
            continue;
        }
        // Now create the device interface
        result = (*plugInInterface)->QueryInterface(plugInInterface,
                                                CFUUIDGetUUIDBytes(kIOUSBDeviceInterfaceID),
                                                    (LPVOID)&dev);
        // Don't need intermediate Plug-In Interface
        (*plugInInterface)->Release(plugInInterface);

        if (result || !dev) {
            printf("Couldn't create a device interface (%08x)\n",
                   (int)result);
            continue;
        }

        // check these values for confirmation
        kr = (*dev)->GetDeviceVendor(dev, &vendor);
        kr = (*dev)->GetDeviceProduct(dev, &product);
        kr = (*dev)->GetDeviceReleaseNumber(dev, &release);
        if ((vendor != LegoUSBVendorID) || (product != LegoUSBProductID) ||
            (release != LegoUSBRelease)) {
            printf("Found unwanted device (vendor = %d != %d, product = %d != %d, release = %d)\n",
                   vendor, kUSBVendorID, product, LegoUSBProductID, release);
            (void) (*dev)->Release(dev);
            continue;
        }

        // Open the device to change its state
        kr = (*dev)->USBDeviceOpen(dev);
        if (kr != kIOReturnSuccess) {
            printf("Unable to open device: %08x\n", kr);
            (void) (*dev)->Release(dev);
            continue;
        }
        // Configure device
        kr = ConfigureDevice(dev);
        if (kr != kIOReturnSuccess) {
            printf("Unable to configure device: %08x\n", kr);
            (void) (*dev)->USBDeviceClose(dev);
            (void) (*dev)->Release(dev);
            continue;
        }
        break;
    }
}

IOUSBInterfaceInterface** osx_usb_rcx_init (int is_fast)
{
    CFMutableDictionaryRef	matchingDict;
    kern_return_t		result;
    IOUSBInterfaceInterface	**intf = NULL;
    
    // create master handler
    result = IOMasterPort(MACH_PORT_NULL, &gMasterPort);
    if (result || !gMasterPort) {
        printf("ERR: Couldn't create master I/O Kit port(%08x)\n", result);
        return NULL;
    }
#ifdef OSX_DEBUG
    printf("Created Master Port.\n");
#endif
    // Set up the matching dictionary for class IOUSBDevice and its subclasses

    matchingDict = IOServiceMatching(kIOUSBDeviceClassName);
    if (!matchingDict) {
        printf("Couldn't create a USB matching dictionary\n");
        mach_port_deallocate(mach_task_self(), gMasterPort);
        return NULL;
    }
    CFDictionarySetValue(matchingDict, CFSTR(kUSBVendorID),
                         CFNumberCreate(kCFAllocatorDefault, kCFNumberShortType, &LegoUSBVendorID));
    CFDictionarySetValue(matchingDict, CFSTR(kUSBProductID),
                         CFNumberCreate(kCFAllocatorDefault, kCFNumberShortType, &LegoUSBProductID));
    
    result = IOServiceGetMatchingServices(gMasterPort, matchingDict, &gRawAddedIter);
    matchingDict = 0;			// this was consumed by the above call

    // Iterate over matching devices to access already persent devices
    FindDevice(NULL, gRawAddedIter);

    result = FindInterfaces(dev, &intf);
    if (kIOReturnSuccess != result)
    {
        printf("unable to find interfaces on device: %08x\n", result);
        (*dev)->USBDeviceClose(dev);
        (*dev)->Release(dev);
        return NULL;
    }
    return intf;
}

void osx_usb_rcx_close (IOUSBInterfaceInterface** intf)
{
    kern_return_t	kr;
    
    (void) (*intf)->USBInterfaceClose(intf);
    (void) (*intf)->Release(intf);
    // Close device and release the object
    kr = (*dev)->USBDeviceClose(dev);
    kr = (*dev)->Release(dev);
    mach_port_deallocate(mach_task_self(), gMasterPort);    
}

void osx_usb_consumeInBuffer() {
    while(gReadRemain && (gInBufferStart < gInBufferEnd))
    {
        *gReadBufferPtr++ = *gInBufferStart++;
        gReadRemain--;
    }

    if (gReadRemain==0) gReadDone = true;
}

void osx_usb_startRead(IOUSBInterfaceInterface **intf) {
    IOReturn res;

    // clear the input buffer
    gInBufferEnd = gInBuffer;
    gInBufferStart = gInBuffer;
    res = (*intf)->ReadPipeAsync(intf, LEGO_RECV_PIPE, gInBuffer, 8, osx_usb_readComplete, (void *) intf);
    if (kIOReturnSuccess != res) {
        printf("Error while reading async!");
        gReadDone = true;
    }
    return;
}

void osx_usb_readComplete(void *refCon, IOReturn result, void *arg0) {
    IOUSBInterfaceInterface	**intf = (IOUSBInterfaceInterface **) refCon;
    UInt32 			read = (UInt32) arg0;

    if (result == kIOReturnAborted) return;

    if (kIOReturnSuccess != result)
    {
        gReadDone = TRUE;
        printf("error from async read (%08x)\n", result);
        (void) (*intf)->USBInterfaceClose(intf);
        (void) (*intf)->Release(intf);
        return;
    }
#ifdef OSX_DEBUG
    printf("ReadComplete called, read = %ld\n", read);
#endif
    gInBufferEnd = gInBuffer + read;
    osx_usb_consumeInBuffer();

    if (!gReadDone)
    {
        osx_usb_startRead(intf);
    }
    
    return;
}

/* Timeout read routine */
int osx_usb_nbread (IOUSBInterfaceInterface **intf, void *buf, int maxlen, int timeout)
{
    int len = 0;
    SInt32 reason;

    gReadBufferPtr = (unsigned char *)buf;
    gReadDone = FALSE;
    gReadRemain = maxlen;

    osx_usb_consumeInBuffer();

    if (!gReadDone) {
        osx_usb_startRead(intf);
        do
        {
            reason = CFRunLoopRunInMode(kCFRunLoopDefaultMode, timeout / 1000.0, true);
            if (reason == kCFRunLoopRunTimedOut)
            {
                (*intf)->AbortPipe(intf, LEGO_RECV_PIPE);
#ifdef OSX_DEBUG
                printf("Timed out!\n");
#endif
                gReadDone = TRUE;
            }
        } while(!gReadDone);
    }

    len = gReadBufferPtr - (unsigned char*)buf;
    if (__comm_debug) {
        printf("Read %d Bytes\n", len);
        hexdump("Read: ", buf, len);
    }

    return len;
}

int osx_usb_rcx_recv (IOUSBInterfaceInterface **intf, void *buf, int maxlen, int timeout, int use_comp) {
    char *bufp = (char *)buf;
    unsigned char msg[OSX_USB_BUFFERSIZE];
    int msglen;
    int sum;
    int pos;
    int len;
    int i;
    /* Receive message */

    for (i = 0; i < 64; i++) {
        msg[i] = 0x00;
    }

    msglen = osx_usb_nbread(intf, msg, 64, timeout);

    if (__comm_debug == 1) {
        printf("recvlen = %d\n", msglen);
        hexdump("R", msg, msglen);
    }

    /* Check for message */

    if (!msglen)
        return RCX_NO_RESPONSE;

    /* Verify message */

    if (use_comp) {
        if (msglen < 5 || (msglen - 3) % 2 != 0)
            // 55 swallow workaround
            if (msglen < 4 || (msglen -2) % 2 != 0)
                return RCX_BAD_RESPONSE;

        if (msg[0] != 0x55 || msg[1] != 0xff || msg[2] != 0x00)
            if (msg[0] != 0xff || msg[1] != 0x00)
                return RCX_BAD_RESPONSE;
            else
                pos = 2;
        else
            pos = 3;

        for (sum = 0, len = 0; pos < msglen - 2; pos += 2) {
            if (msg[pos] != ((~msg[pos+1]) & 0xff))
                return RCX_BAD_RESPONSE;
            sum += msg[pos];
            if (len < maxlen)
                bufp[len++] = msg[pos];
        }

        if (msg[pos] != ((~msg[pos+1]) & 0xff))
            return RCX_BAD_RESPONSE;

        if (msg[pos] != (sum & 0xff))
            return RCX_BAD_RESPONSE;

        /* Success */
        return len;
    }
    else {
        if (msglen < 4)
            return RCX_BAD_RESPONSE;

        if (msg[0] != 0x55 || msg[1] != 0xff || msg[2] != 0x00)
            return RCX_BAD_RESPONSE;

        for (sum = 0, len = 0, pos = 3; pos < msglen - 1; pos++) {
            sum += msg[pos];
            if (len < maxlen)
                bufp[len++] = msg[pos];
        }

        /* Return success if checksum matches */
        if (msg[pos] == (sum & 0xff))
            return len;

        /* Failed.  Possibly a 0xff byte queued message? (legos unlock firmware) */
        for (sum = 0, len = 0, pos = 3; pos < msglen - 2; pos++) {
            sum += msg[pos];
            if (len < maxlen)
                bufp[len++] = msg[pos];
        }

        /* Return success if checksum matches */
        if (msg[pos] == (sum & 0xff))
            return len;

        /* Failed.  Possibly a long message? */
        /* Long message if opcode is complemented and checksum okay */
        /* If long message, checksum does not include opcode complement */
        for (sum = 0, len = 0, pos = 3; pos < msglen - 1; pos++) {
            if (pos == 4) {
                if (msg[3] != ((~msg[4]) & 0xff))
                    return RCX_BAD_RESPONSE;
            }
            else {
                sum += msg[pos];
                if (len < maxlen)
                    bufp[len++] = msg[pos];
            }
        }

        if (msg[pos] != (sum & 0xff))
            return RCX_BAD_RESPONSE;

        /* Success */
        return len;
    }
}

int osx_usb_rcx_send(IOUSBInterfaceInterface **intf, void *buf, int len, int use_comp)
{
    char *bufp = (char *)buf;
    char buflen = len;
    char msg[OSX_USB_BUFFERSIZE];
    int msglen;
    int sum;
    IOReturn			kr;


    /* drain buffer */
    while (osx_usb_nbread(intf, msg, 64, 100) > 0) 
        ;
        
    /* Encode message */

    msglen = 0;
    sum = 0;

    if (use_comp) {
        msg[msglen++] = 0x55;
        msg[msglen++] = 0xff;
        msg[msglen++] = 0x00;
        while (buflen--) {
            msg[msglen++] = *bufp;
            msg[msglen++] = (~*bufp) & 0xff;
            sum += *bufp++;
        }
        msg[msglen++] = sum;
        msg[msglen++] = ~sum;
    }
    else {
        msg[msglen++] = 0xff;
        while (buflen--) {
            msg[msglen++] = *bufp;
            sum += *bufp++;
        }
        msg[msglen++] = sum;
    }
    if (__comm_debug == 1)
        hexdump("Sending: ", &msg, msglen);

    kr = (*intf)->WritePipe(intf, LEGO_SEND_PIPE, &msg, msglen);
    if (kIOReturnSuccess != kr)
    {
        printf("unable to do osx_usb_rcx_send (%08x)\n", kr);
        (void) (*intf)->USBInterfaceClose(intf);
        (void) (*intf)->Release(intf);
        return -1;
    }
/* #ifdef OSX_DEBUG
    printf(".\n");
#endif */
    return len;
}

int osx_usb_rcx_sendrecv (IOUSBInterfaceInterface **intf, void *send, int slen, void *recv, int rlen, int timeout, int retries, int use_comp)
{
    int status = 0;

#ifdef OSX_DEBUG
    printf("sendrecv %d:\n", slen);
#endif
    while (retries--) {
        if ((status = osx_usb_rcx_send(intf, send, slen, use_comp)) < 0) {
            if (__comm_debug == 1)
                printf("status = %s\n", rcx_strerror(status));
            continue;
        }
        /* printf("\n"); */
        if ((status = osx_usb_rcx_recv(intf, recv, rlen, timeout, use_comp)) < 0) {
            if (__comm_debug == 1)
                printf("status = %s\n", rcx_strerror(status));
            continue;
        }
        break;
    }

    return status;
}

int osx_usb_rcx_is_alive (IOUSBInterfaceInterface **intf, int use_comp)
{
    unsigned char send[1] = { 0x10 };
    unsigned char recv[1];

    return (osx_usb_rcx_sendrecv(intf, send, 1, recv, 1, 100, 5, use_comp) == 1);
}

