
#include <rom.h>

#include "types.h"
#include "constants.h"
#include "classes.h"
#include "threads.h"
#include "specialclasses.h"
#include "specialsignatures.h"
#include "language.h"
#include "memory.h"
#include "interpreter.h"
#include "exceptions.h"
#include "configure.h"
#include "trace.h"
#include "magic.h"
#include "systime.h"

extern char _bss_start;
extern char _end;
extern char _text_end;
extern char _data_end;
extern char _text_begin;
extern char _data_begin;

extern char _extraMemory;

/**
 * bootThread is a special global.
 */
Thread *bootThread;

#if !EMULATE && VERIFY
#error Should not VERIFY in RCX
#endif

#define MEM_START      (&_end)
#define RAM_START_ADDR 0x8000
#define RAM_SIZE       0x6F00

#define MEM_END_ADDR   (RAM_START_ADDR + RAM_SIZE)
#define MEM_END        ((char *) MEM_END_ADDR)
#define BUFSIZE        9
#define TDATASIZE      100
#define MAXNEXTBYTEPTR (MEM_END - TDATASIZE)

// Hook Commands
#define HC_NONE            0	// No hook command
#define HC_SHUTDOWN_POWER  1	// Shut down power
#define HC_EXIT_PROGRAM    2	// Exit program

// Program Status
#define HP_NO_PROGRAM      0	// No program at all
#define HP_INTERRUPTED     1	// Not used
#define HP_NOT_STARTED     2	// Not started

#define BUTTON_VIEW 0x02
#define BUTTON_PRGM 0x04
#define BUTTON_RUN 0x01

#define POWER_OFF_TIMEOUT  (8L * 60L * 1000L)
 
#ifdef VERIFY

void assert (boolean aCond, int aCode)
{
  // TBD
}

#endif

char versionArray[] = { 0xE2, 0x00, 0x03, 0x00, 0x01, 
                        0x00, 0x00, 0x00, 0x00 };

char timerdata1[6];
async_t timerdata0;
char buffer[BUFSIZE];
char numread;   
char valid;
char hookCommand;
char *nextByte;
char *mmStart;
short status;
byte hasProgram;
boolean isReadyToTransfer;
//TWOBYTES noTransferCount;
TWOBYTES seqNumber;
TWOBYTES currentIndex;
FOURBYTES powerOffTime;

//char runStatus = RS_NO_PROGRAM;

static inline void
set_data_pointer (void *ptr)
{
  play_sound_or_set_data_pointer(0x1771, (short)ptr, 0);
}

void delay (unsigned long count)
{
  unsigned long i;
	
  for (i = 0; i  < count; i++) { }
}

void wait_for_power_release (unsigned short count)
{
  TWOBYTES debouncer = 0;

  do
  {
    delay (20);
    get_power_status (POWER_KEY, &status);
    if (status == 0)
      debouncer = 0;
    else
      debouncer++;
  } while (debouncer <= count);
}

void trace (short s, short n1, short n2)
{
  if (s != -1)
    play_system_sound (SOUND_QUEUED, s);
  set_lcd_number (LCD_UNSIGNED, n1, 3002);
  set_lcd_number (LCD_PROGRAM, n2, 0);
  set_lcd_segment (LCD_SENSOR_1_VIEW);
  refresh_display();
  // Wait for power press
  do {
    get_power_status (POWER_KEY, &status);
  } while (status != 0);
  // Wait for power release
  wait_for_power_release (30);
}

void handle_uncaught_exception (Object *exception,
                                       const Thread *thread,
				       const MethodRecord *methodRecord,
				       const MethodRecord *rootMethod,
				       byte *pc)
{
  trace (4, methodRecord->signatureId, get_class_index (exception) % 10);
}

void wait_for_release (short code)
{
  short st;
  short debouncer = 0;
  
  do 
  {
    read_buttons (0x3000, &st);
    if (st & code)
      debouncer = 0;
    else
      debouncer++;
  } while (debouncer < 100);
}

/**
 * This function is invoked by switch_thread
 * and the download loop.
 */
void switch_thread_hook()
{
  get_power_status (POWER_KEY, &status);
  if (status == 0)
  {
    short st;
    
    read_buttons (0x3000, &st);
    // Power button pressed - wait for release
    wait_for_power_release (150);
    schedule_request (REQUEST_EXIT);
    
    // If power button is released whilst run button is down
    // exit the program (once run button is released)
    if (st & BUTTON_RUN)
    {
      wait_for_release (BUTTON_RUN);
      hookCommand = HC_EXIT_PROGRAM;
    }
    else
    {
      // Just shut down the power
      hookCommand = HC_SHUTDOWN_POWER;      
      play_system_sound (SOUND_QUEUED, 0);
      delay (40000);
    }
  }
}

void reset_rcx_serial()
{
  //init_serial (&timerdata1[4], &timerdata0, 1, 1);
  init_serial (0, 0, 1, 1);
}

int main (void)
{
// Main entry point for VM
LABEL_FIRMWARE_ONE_TIME_INIT:
  // If just downloaded VM, we have no program.
  hasProgram = HP_NO_PROGRAM;
  
  // Default program number
  set_program_number (0);
  
// Power up initializations
LABEL_POWERUP:  
  // The following call always needs to be the first one.
  init_timer (&timerdata0, &timerdata1[0]);

  // Set sleep mode to 'standby' plus other stuff needed for init_serial()  
  init_power();
  
  // Initialize timer handler.
  sys_time = 0l;
  systime_init();

  // Not sure why this is done.  
  init_sensors();
  
  // If power key pressed, wait until it's released.
  wait_for_power_release (600);
  
// Entry point for program exit (HC_EXIT_PROGRAM)
LABEL_NEW_PROGRAM:
  // No hook command to execute
  hookCommand = HC_NONE;
  
// Go into download mode
LABEL_RESET_DOWNLOAD:
  // Initialize serial I/O
  reset_rcx_serial();
  
// Initialize download
LABEL_DOWNLOAD_INIT:
  isReadyToTransfer = false;
  play_system_sound (SOUND_QUEUED, 1);
  clear_display();
  get_power_status (POWER_BATTERY, &status);
  status = (status * 100L) / 355L;
  set_lcd_number (LCD_SIGNED, status, LCD_DECIMAL_1);
  
// Show program number
LABEL_SHOW_PROGRAM_NUMBER:
  set_lcd_number (LCD_PROGRAM, get_program_number(), 0);

// Entry point for download start
LABEL_START_TRANSFER:
  powerOffTime = sys_time + POWER_OFF_TIMEOUT;
  clear_lcd_segment (LCD_WALKING);
  if (hasProgram != HP_NO_PROGRAM)
  {
    set_lcd_segment (LCD_STANDING);
  }
  else
  {
    clear_lcd_segment (LCD_STANDING);
  }
  refresh_display();
  currentIndex = 1;

// Loop downloading stuff
LABEL_COMM_LOOP:
  for (;;)
  {
    check_for_data (&valid, &nextByte);
    if (valid)
    {
      //if (runStatus != RS_NO_PROGRAM)
      //  set_run_status (RS_NO_PROGRAM);
      numread = 0;
      receive_data (buffer, BUFSIZE, &numread);
      switch (buffer[0] & 0xF7)
      {
        case 0x10:
          // Alive?
          buffer[0] = ~buffer[0];
          send_data (SERIAL_NO_POINTER, 0, buffer, 1);
          goto LABEL_COMM_LOOP;
        case 0x15:
          // Get Versions...
          versionArray[0] = ~buffer[0];
          send_data (SERIAL_NO_POINTER, 0, versionArray, 9);
          goto LABEL_COMM_LOOP;
        case 0x65:
          // Delete firmware
          buffer[0] = ~buffer[0];
          send_data (SERIAL_NO_POINTER, 0,  buffer, 1);
          goto LABEL_EXIT;
        case 0x45:
          // Transfer data
          seqNumber = ((TWOBYTES) buffer[2] << 8) |  buffer[1]; 
	  buffer[0] = ~buffer[0];
	  buffer[1] = (byte) !isReadyToTransfer;
	  send_data (SERIAL_NO_POINTER, 0, buffer, 2);
	  if (!isReadyToTransfer)
	  {
  	    goto LABEL_RESET_DOWNLOAD;
	  }
          break;
	case 0x12:
          // Get value -- used by TinyVM to initiate download
	  if (isReadyToTransfer)
            goto LABEL_RESET_DOWNLOAD;
	  buffer[0] = ~buffer[0];
	  buffer[1] = (byte) (MAGIC >> 8);
	  buffer[2] = (byte) (MAGIC & 0xFF);
	  send_data (SERIAL_NO_POINTER, 0, buffer, 3);
          set_data_pointer (MEM_START);
          isReadyToTransfer = true;
	  hasProgram = HP_NO_PROGRAM;
	  goto LABEL_START_TRANSFER;
        default:
          // Other??
          #if 0
          trace (2, (short) buffer[0], 7);
          #endif
          goto LABEL_RESET_DOWNLOAD;
      }
      // Only gets here if we are transferring data
      set_lcd_number (LCD_UNSIGNED, (short) currentIndex, 3002);
      set_lcd_number (LCD_PROGRAM, (short) 0, 0);
      refresh_display();
      
      // Do we have all the data?
      if (currentIndex != 1 && seqNumber == 0)
      {
        // Reinitialize serial communications
        reset_rcx_serial();
	// Set pointer to start of binary image
        install_binary (MEM_START);
        // Check magic number
        if (get_magic_number() != MAGIC)
        { 
          trace (1, MAGIC, 9);
          goto LABEL_RESET_DOWNLOAD;
        }
	// Indicate that the RCX has a new program in it.
	hasProgram = HP_NOT_STARTED;
        // Make sure memory allocation starts at an even address.
        mmStart = (((TWOBYTES) nextByte) & 0x0001) ? nextByte + 1 : nextByte;
	// Initialize program number shown on LCD.
	set_program_number (0);
        goto LABEL_DOWNLOAD_INIT;
      }
      if (nextByte >= MAXNEXTBYTEPTR || seqNumber != currentIndex)
      {
        #if 0
        trace (4, (TWOBYTES) seqNumber, currentIndex);
        trace (4, (TWOBYTES) nextByte / 10, 5);
        #endif
        goto LABEL_RESET_DOWNLOAD;
      }
      currentIndex++;
    }
    else	// No data to be received
    {
      // Should we power off?
      if (sys_time >= powerOffTime)
      {
	play_system_sound (SOUND_QUEUED, 0);
	delay (30000);
	hookCommand = HC_SHUTDOWN_POWER;
	goto LABEL_SHUTDOWN_POWER;
      }
      
      // Check for button presses
      switch_thread_hook();
      
      // Power off?
      if (hookCommand == HC_SHUTDOWN_POWER)
        goto LABEL_SHUTDOWN_POWER;
        
      // Stop program? But then we aren't running it!
      if (hookCommand != HC_NONE)
	goto LABEL_START_TRANSFER;
	
      // If we have a program to run...
      if (hasProgram != HP_NO_PROGRAM)
      {
      	// Check on a few more buttons
        read_buttons (0x3000, &status);
        
        // Run the program
	if (status & BUTTON_RUN)
	{
          wait_for_release (BUTTON_RUN);
	  goto LABEL_PROGRAM_STARTUP;
	}
	
	// Run a different program
	else if (status & BUTTON_PRGM)
	{
	  play_system_sound (SOUND_QUEUED, 0);
	  wait_for_release (BUTTON_PRGM);
	  inc_program_number();
	  goto LABEL_SHOW_PROGRAM_NUMBER;
	}
      }
    } // End if(valid)else
  } // End for

// Run the program.
LABEL_PROGRAM_STARTUP:

  // Reinitialize binary
  initialize_binary();
  
  // Initialize memory for object allocation.
  // This can only be done after initialize_binary().
  init_memory (mmStart, ((TWOBYTES) MEM_END - (TWOBYTES) mmStart) / 2);

  // Initialize special exceptions
  init_exceptions();

  // Create the boot thread (bootThread is a special global).
  bootThread = (Thread *) new_object_for_class (JAVA_LANG_THREAD);

  #if DEBUG_RCX_MEMORY
  {
    TWOBYTES numNodes, biggest, freeMem;	
    scan_memory (&numNodes, &biggest, &freeMem);
    trace (3, numNodes, 3);
    trace (3, biggest, 4);
    trace (3, freeMem, 5);
  }
  #endif
  
  // Jump to this point to start executing main().
  
  // Initialize the threading module.
  init_threads();

  // Start/prepare boot thread. Sets thread state to STARTED,
  // which in the case of bootThread, means main will be scheduled.
  if (!init_thread (bootThread))
  {
    // There isn't enough memory to even start the boot thread!!
    trace (1, start_4_5V, JAVA_LANG_OUTOFMEMORYERROR % 10);
    // The program is useless now
    goto LABEL_FIRMWARE_ONE_TIME_INIT;
  }

  // Show walking man.
  clear_lcd_segment (LCD_STANDING);
  set_lcd_segment (LCD_WALKING);
  refresh_display();
  
  // Execute the bytecode interpreter.
  engine();

// Go in to standby mode  
LABEL_SHUTDOWN_POWER:

  // Program terminated.
  clear_display();
  refresh_display();

  // Program terminated voluntarily
  if (hookCommand != HC_SHUTDOWN_POWER)
  {
    // We have a program but it isn't running.
    hasProgram = HP_NOT_STARTED;
    
    // Stop motors.
    control_motor(MOTOR_0,MOTOR_STOP,0);
    control_motor(MOTOR_1,MOTOR_STOP,0);
    control_motor(MOTOR_2,MOTOR_STOP,0);
    goto LABEL_NEW_PROGRAM;
  }

  // Power off has been pressed. Pause
  // for some time to allow motors to spin down.
  delay (20000);

  shutdown_sensors();
  shutdown_buttons();
  shutdown_timer();
  shutdown_power();	// Presumably doesn't return again until power is pressed
  goto LABEL_POWERUP;

// Erase VM  
LABEL_EXIT:
  // Seems to be a good idea to shutdown timer before going back to ROM.
  shutdown_timer();
  return 0;
}

