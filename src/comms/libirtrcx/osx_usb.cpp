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
 *  13/2/2005 Made interface generic C to avoid all the IOKit and Mach types impacting
 *  all the other code. Also removed most of the code that was aware of the RCX 
 *  communication protocol, since that is now in cross-platform code. Andy Belk
 *
 */

//#define OSX_DEBUG 1

#include "osx_usb.h"
#include <AvailabilityMacros.h>
#include <CoreFoundation/CoreFoundation.h>
#include <unistd.h>
#include <sys/time.h>

#include <mach/mach.h>         // For mach_port_deallocate()
#include <IOKit/IOKitLib.h>
#include <IOKit/IOCFPlugIn.h>
#include <IOKit/usb/IOUSBLib.h>
#include <IOKit/usb/USBSpec.h>

#if MAC_OS_X_VERSION_MAX_ALLOWED > MAC_OS_X_VERSION_10_0
// On 10.1 and later use a more complete USB interface,
#define IOUSBDeviceInterface IOUSBDeviceInterface182
#define IOUSBInterfaceInterface IOUSBInterfaceInterface182
#endif

#define LegoUSBRelease 256

#define LEGO_SEND_PIPE 2
#define LEGO_RECV_PIPE 1

#ifndef RCX_COMM_H_INCLUDED
#include "rcx_comm.h"
#endif


static io_iterator_t			gRawAddedIter;

#define OSX_USB_BUFFERSIZE 4096

/* The read buffer */
#define OSX_USB_READ_BUF_SIZE 16
static unsigned char			gInBuffer[OSX_USB_READ_BUF_SIZE];
static unsigned char*			gReadBufferPtr;
static unsigned char*			gInBufferStart;
static unsigned char* 			gInBufferEnd;
static int 				gReadRemain;
static int 				gReadDone;

static short 				LegoUSBVendorID = 1684;
static short 				LegoUSBProductID = 1;

extern int				__comm_debug;

static mach_port_t			gMasterPort;

static IOUSBDeviceInterface		**dev = NULL;


/* Callback for async read */
void osx_usb_readComplete(void *refCon, IOReturn result, void *arg0);

/* Hexdump routine */

#define LINE_SIZE   16
#define GROUP_SIZE  4
#define UNPRINTABLE '.'

void osx_hexdump(char *prefix, const void *buf, int len) {
    unsigned char *b = (unsigned char *)buf;
    int i, j, w;

    for (i = 0; i < len; i += w) {
        w = len - i;
        if (w > LINE_SIZE)
            w = LINE_SIZE;
        if (prefix)
            printf("%s ", prefix);
        printf("%04x: ", i);
        for (j = 0; j < w; j++, b++) {
            printf("%02x ", *b);
            if ((j + 1) % GROUP_SIZE == 0)
                putchar(' ');
        }
        putchar('\n');
    }
}

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
    printf("Number of Configurations: %d\n", numConfig);
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
    res = (*plugInInterface)->QueryInterface(plugInInterface, CFUUIDGetUUIDBytes(kIOUSBInterfaceInterfaceID), (LPVOID*) &intf);
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

/*
 * Locates matching device. Returns resulting device count (zero, 1 or > 1). Only 1 is useful right now.
 */
unsigned int FindDevice(void *refCon, io_iterator_t iterator) {
    kern_return_t		kr;
    io_service_t		usbDevice;
    IOCFPlugInInterface		**plugInInterface = NULL;
    HRESULT			result;
    SInt32			score;
    UInt16			vendor;
    UInt16			product;
    UInt16			release;
    unsigned int 		count = 0;
    
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
                                                    (LPVOID *)&dev);
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
        if (kr == kIOReturnSuccess) {
            count++;
        } else {
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

    return count;
}

#if 0
void osx_usb_rcx_wakeup (IOUSBInterfaceInterface** intf)
{
    char wakeup[] = {0xfe, 0x10, 0xfe, 0x10};
    osx_usb_write(intf, &wakeup, 4);
    usleep(20000);
}
#endif

intptr_t osx_usb_rcx_init (int is_fast)
{
    CFMutableDictionaryRef	matchingDict;
    kern_return_t		result;
    IOUSBInterfaceInterface	**intf = NULL;
    unsigned int 		device_count = 0;
        
    // create master handler
    result = IOMasterPort(MACH_PORT_NULL, &gMasterPort);
    if (result || !gMasterPort) {
        printf("ERR: Couldn't create master I/O Kit port(%08x)\n", result);
        return (intptr_t)NULL;
    }
#ifdef OSX_DEBUG
    printf("Created Master Port.\n");
#endif
    // Set up the matching dictionary for class IOUSBDevice and its subclasses

    matchingDict = IOServiceMatching(kIOUSBDeviceClassName);
    if (!matchingDict) {
        printf("Couldn't create a USB matching dictionary\n");
        mach_port_deallocate(mach_task_self(), gMasterPort);
        return (intptr_t)NULL;
    }
    CFDictionarySetValue(matchingDict, CFSTR(kUSBVendorID),
                         CFNumberCreate(kCFAllocatorDefault, kCFNumberShortType, &LegoUSBVendorID));
    CFDictionarySetValue(matchingDict, CFSTR(kUSBProductID),
                         CFNumberCreate(kCFAllocatorDefault, kCFNumberShortType, &LegoUSBProductID));
    
    result = IOServiceGetMatchingServices(gMasterPort, matchingDict, &gRawAddedIter);
    matchingDict = 0;			// this was consumed by the above call

    // Iterate over matching devices to access already present devices
    device_count = FindDevice(NULL, gRawAddedIter);
    if (device_count == 1) {
        result = FindInterfaces(dev, &intf);
        if (kIOReturnSuccess != result) {
            printf("unable to find interfaces on device: %08x\n", result);
            (*dev)->USBDeviceClose(dev);
            (*dev)->Release(dev);
            return (intptr_t)NULL;
        }
        // Used to do a wakeup here. Not clear it's needed since Linux does not do it.
        //osx_usb_rcx_wakeup(intf);
        return (intptr_t)intf;
    } else if (device_count > 1) {
        printf("too many matching devices (%d) !\n", device_count);
    } else {
   	printf("no matching devices found\n");
    }
    return (intptr_t)NULL;
}

void osx_usb_rcx_close (intptr_t usbii)
{
    IOUSBInterfaceInterface **intf = (IOUSBInterfaceInterface**)usbii;
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
#ifdef OSX_DEBUG
    printf("osx_usb_startRead called.\n");
#endif
    // clear the input buffer
    gInBufferEnd = gInBuffer;
    gInBufferStart = gInBuffer;
    res = (*intf)->ReadPipeAsync(intf, LEGO_RECV_PIPE, gInBuffer, OSX_USB_READ_BUF_SIZE, osx_usb_readComplete, (void *) intf);
    if (kIOReturnSuccess != res) {
        printf("Error while reading async!");
        gReadDone = true;
    }
    return;
}

void osx_usb_readComplete(void *refCon, IOReturn result, void *arg0) {
    IOUSBInterfaceInterface	**intf = (IOUSBInterfaceInterface **) refCon;
    UInt32 			read = (UInt32) arg0;

    if (result == kIOReturnAborted) {
#ifdef OSX_DEBUG
        printf("osx_usb_readComplete: IOReturnAborted!\n");
#endif
        return;
    }
    
    if (kIOReturnSuccess != result) {
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

    if (!gReadDone) {
        osx_usb_startRead(intf);
    }
    
    return;
}


/* Timeout read routine */
int osx_usb_nbread (intptr_t usbii, void *buf, int maxlen, int timeout) {
    IOUSBInterfaceInterface **intf = (IOUSBInterfaceInterface **)usbii;
    IOReturn kr;
    timeout *= 5;   // For safety
#ifdef OSX_DEBUG
    printf("Called osx_usb_nbread. Maxlen=%d timeout = %d\n", maxlen, timeout);
#endif

    if (intf == NULL) {
        return -1;
    }
    SInt32 reason;
    gReadBufferPtr = (unsigned char *)buf;
    gReadDone = FALSE;
    gReadRemain = maxlen;
    int len = 0;
    CFRunLoopSourceRef runLoopSource;

    osx_usb_consumeInBuffer();

    if (!gReadDone) {
        osx_usb_startRead(intf);
        do {
            reason = CFRunLoopRunInMode(kCFRunLoopDefaultMode, timeout / 1000.0, true);
            if (reason == kCFRunLoopRunTimedOut) {
                (*intf)->AbortPipe(intf, LEGO_RECV_PIPE);
#ifdef OSX_DEBUG
                printf("Timed out!\n");
#endif
                gReadDone = TRUE;
            } else if (reason == kCFRunLoopRunFinished) {
                // oops, seems no event source is registered for this runloop
                kr = (*intf)->CreateInterfaceAsyncEventSource(intf, &runLoopSource);
                if (kIOReturnSuccess != kr) {
                    printf("unable to create async event source (%08x)\n", kr);
                    (void) (*intf)->USBInterfaceClose(intf);
                    (void) (*intf)->Release(intf);
                    return -1;
                }
                CFRunLoopAddSource(CFRunLoopGetCurrent(), runLoopSource, kCFRunLoopDefaultMode);
                
#ifdef OSX_DEBUG
                printf("Finished.\n");
#endif
                gReadDone = TRUE;
            }
        } while(!gReadDone);
    }

    len = gReadBufferPtr - (unsigned char*)buf;

    if (__comm_debug) {
        printf("Read %d Bytes\n", len);
        osx_hexdump("Read: ", buf, len); 
    }

    return len;
}

#if 0
// Note: We can't useReadPipeTO() USB as Lego Tower is an interrupt device
// This version blocks
int osx_usb_read(intptr_t usbii, void *buf, int len, int timeout) {
    IOUSBInterfaceInterface **intf = (IOUSBInterfaceInterface **)usbii;
    if (intf == NULL) {
        return -1;
    }
    IOReturn kr;
#ifdef OSX_DEBUG
    printf("Called osx_usb_read. Maxlen=%d timeout = %d\n", len, timeout);
#endif
    
    // Can't use ASYNC USB as Lego Tower is an interrupt device
    void *p = buf;
    while (len > 0) {
        UInt32 count = len;
        kr = (*intf)->ReadPipe(intf, LEGO_RECV_PIPE, p, &count);
        if (kIOReturnSuccess == kr) {
#ifdef OSX_DEBUG
            printf("USB read %d bytes\n", count);
#endif
            p += count;
            len -= count;
        } else if (kr == kIOReturnError) {
            (*intf)->AbortPipe(intf, LEGO_RECV_PIPE);
#ifdef OSX_DEBUG
            printf("USB read failed\n");
#endif
            len = 0;
        } else {
            (*intf)->AbortPipe(intf, LEGO_RECV_PIPE);
#ifdef OSX_DEBUG
            printf("USB read failed: 0x%x\n", kr);
#endif
            len = 0;
        }
    }
    
    if (__comm_debug) {
        printf("Read %d Bytes\n", len);
        osx_hexdump("Read: ", buf, len); 
    }
    
    return len;
}
#endif // 0

int osx_usb_write(intptr_t usbii, const void *buf, int len) {
    IOUSBInterfaceInterface **intf = (IOUSBInterfaceInterface **)usbii;
    if (intf == NULL) {
        return -1;
    }
    IOReturn kr;
    if (len <= 0) {
        fprintf(stderr, "osx_usb_write: len <= 0: %d\n", len);
        return -1;
    }
    if (len > OSX_USB_BUFFERSIZE) {
        fprintf(stderr, "osx_usb_write: buffer length (len) too big %d\n", len);
        return -1;
    }

#ifdef OSX_DEBUG
    printf("osx_usb_write: Enter: msglen = %d\n", len);
    osx_hexdump("Write: ", buf, len);
#endif
   
    kr = (*intf)->WritePipe(intf, LEGO_SEND_PIPE, (void *)buf, len);

    if (kIOReturnSuccess != kr) {
        printf("unable to do osx_usb_write (%08x)\n", kr);
        (void) (*intf)->USBInterfaceClose(intf);
        (void) (*intf)->Release(intf);
        return -1;
    }
#ifdef OSX_DEBUG
    kr = (*intf)->GetPipeStatus(intf, LEGO_SEND_PIPE);
    printf("SEND PIPE status: %08x\n", kr);
    printf("osx_usb_write: Exit.\n");
#endif
    return len;
}
