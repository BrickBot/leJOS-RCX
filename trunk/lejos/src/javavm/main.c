
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

extern char _bss_start;
extern char _end;
extern char _text_end;
extern char _data_end;
extern char _text_begin;
extern char _data_begin;

extern char _extraMemory;

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

#define HC_NONE            0
#define HC_SHUTDOWN_POWER  1
//#define HC_DELETE_FIRMWARE 2

#define RS_NO_PROGRAM      0
#define RS_STOPPED         1
#define RS_RUNNING         2

#ifdef VERIFY

void assert (boolean aCond, int aCode)
{
  // TBD
}

#endif

char timerdata1[6];
async_t timerdata0;
char buffer[BUFSIZE];
char numread;   
char valid, state0, state1;
char hookCommand;
char *nextByte;
char *mmStart;
short status;
TWOBYTES seqNumber;
TWOBYTES currentIndex;

//char runStatus = RS_NO_PROGRAM;

static inline void
set_data_pointer (void *ptr)
{
  play_sound_or_set_data_pointer(0x1771, (short)ptr, 0);
}

#if DEBUG_RCX

void debug (short s, short n1, short n2)
{
  trace (s, n1, n2);
}

#endif

void wait_for_power_release()
{
  do {
    get_power_status (POWER_KEY, &status);
  } while (status == 0);
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
  wait_for_power_release();
}

#if 0

void update_run_status()
{
  if (runStatus == RS_RUNNING)
  {
    clear_lcd_segment (LCD_STANDING);
    set_lcd_segment (LCD_WALKING);    
  }
  else if (runStatus == RS_STOPPED)
  {
    clear_lcd_segment (LCD_WALKING);
    set_lcd_segment (LCD_STANDING);    
  }
  else if (runStatus == RS_NO_PROGRAM)
  {
    clear_lcd_segment (LCD_WALKING);
    clear_lcd_segment (LCD_STANDING);        
  }
  refresh_display();
}

void set_run_status (char aRunStatus)
{
  runStatus = aRunStatus;
  update_run_status();
}

#endif

/**
 * This function is invoked by switch_thread
 * and the download loop.
 */
void switch_thread_hook()
{
  get_power_status (POWER_KEY, &status);
  if (status == 0)
  {
    // Power button pressed - wait for release
    wait_for_power_release();
    // Make interpreter exit
    gMustExit = true;

#if 0
    // Check to see if this is a delete-firmware request
    read_buttons (BUTTONS_READ, &status);
    if ((status & 0x05) != 0)
      hookCommand = HC_DELETE_FIRMWARE;
    else
      hookCommand = HC_SHUTDOWN_POWER;
#endif

    hookCommand = HC_SHUTDOWN_POWER;
  }

#if 0

  else
  {
    read_buttons (BUTTONS_READ, &status);
    if ((status & 0x01) != 0)
    {
      // Run button pressed.
      if (runStatus == RS_RUNNING)
      {
        gMustExit = 1;
      }
      else if (runStatus == RS_STOPPED)
      {
        runStatus = RS_RUNNING;
      }
    }
  }

#endif

}

void reset_rcx()
{
  init_timer (&timerdata0, &timerdata1[0]);
  init_power();
  state0 = 0;
  state1 = 0;
  init_serial (&state0, &state1, 1, 1);
}

int main (void)
{
 LABEL_POWERUP:
  // The following call always needs to be the first one.
  init_timer (&timerdata0, &timerdata1[0]);
 LABEL_DOWNLOAD:
  reset_rcx();
  init_sensors();
  // If power key pressed, wait until it's released.
  wait_for_power_release();
  play_system_sound (SOUND_QUEUED, 1);
  hookCommand = HC_NONE;
  clear_display();
  set_lcd_number (LCD_UNSIGNED, (short) 0, 3002);
  set_lcd_number (LCD_PROGRAM, (short) 0, 0);
  //update_run_status();
  refresh_display();
 LABEL_RESET:
  set_data_pointer (MEM_START);
  currentIndex = 1;
  while (1)
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
          goto LABEL_RESET;
        case 0x15:
          // Get Versions...
          buffer[0] = ~buffer[0];
          #if 0
          buffer[1] = 0x00;
          buffer[2] = 0x03;
          buffer[3] = 0x00;
          buffer[4] = 0x01;
          buffer[5] = 0x00;
          buffer[6] = 0x00;
          buffer[7] = 0x00;
          buffer[8] = 0x00;
          #endif
          send_data (SERIAL_NO_POINTER, 0, buffer, 9);
          goto LABEL_RESET;
        case 0x65:
          // Delete firmware
          buffer[0] = ~buffer[0];
          send_data (SERIAL_NO_POINTER, 0,  buffer, 1);
          goto LABEL_EXIT;
        case 0x45:
          // Transfer data
          break;
        default:
          // Other??
          #if 0
          trace (4, (short) buffer[0], 9);
          #endif
          goto LABEL_DOWNLOAD;
      }
      seqNumber = ((TWOBYTES) buffer[2] << 8) |  buffer[1]; 
      set_lcd_number (LCD_UNSIGNED, (short) currentIndex, 3002);
      set_lcd_number (LCD_PROGRAM, (short) 0, 0);
      refresh_display();
      if (currentIndex != 1 && seqNumber == 0)
      {
        install_binary (MEM_START);
        // Initialize heap location and size
        #if DEBUG_RCX
        debug (-1, ((TWOBYTES) nextByte) / 10, 1);
        #endif
        // Make sure memory allocation starts at an even address.
        mmStart = (((TWOBYTES) nextByte) & 0x0001) ? nextByte + 1 : nextByte;
        //runStatus = RS_STOPPED;
        goto LABEL_PROGRAM_STARTUP;
      }
      if (nextByte >= MAXNEXTBYTEPTR || seqNumber != currentIndex)
      {
        #if DEBUG_RCX
        debug (4, (TWOBYTES) nextByte / 10, seqNumber - currentIndex);
        #endif
        goto LABEL_DOWNLOAD;
      }
      currentIndex++;
    }
    else
    {
      switch_thread_hook();

      //if (hookCommand == HC_DELETE_FIRMWARE)
      //  goto LABEL_EXIT;

      if (hookCommand == HC_SHUTDOWN_POWER)
        goto LABEL_SHUTDOWN_POWER;

      //if (runStatus == RS_RUNNING)
      //  goto LABEL_PROGRAM_STARTUP;
    }
  }

 LABEL_PROGRAM_STARTUP:
  // Gotta shutdown serial communications
  shutdown_serial();
  // Initialize memory for object allocation
  init_memory (mmStart, ((TWOBYTES) MEM_END - (TWOBYTES) mmStart) / 2);
  // Initialize special exceptions
  init_exceptions();
  // Create the boot thread (bootThread is a special global)
  bootThread = (Thread *) new_object_for_class (JAVA_LANG_THREAD);
  #if DEBUG_RCX
  debug (-1, ((TWOBYTES) bootThread) / 10, 3);
  #endif
  // Start/prepare thread
  init_thread (bootThread);
  // Execute the bytecode interpreter
  #if 0
  debug (-1, (short) get_master_record()->magicNumber, 4);
  #endif
  engine();
  //runStatus = RS_STOPPED;
  // Engine returns when all threads are done or
  // when power has been shut down.
  if (hookCommand == HC_SHUTDOWN_POWER)
    goto LABEL_SHUTDOWN_POWER;        
  goto LABEL_DOWNLOAD;
 LABEL_SHUTDOWN_POWER:
  shutdown_sensors();
  shutdown_buttons();
  shutdown_power();
  goto LABEL_POWERUP;
 LABEL_EXIT:
  //shutdown_buttons();
  //shutdown_timer();
  //shutdown_power();
  return 0;
}




