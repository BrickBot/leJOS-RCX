#! /bin/sh

# Extract the first word of "gcc", so it can be a program name with args.
set dummy gcc; ac_word=$2
if test -n "$CC"; then
  ac_cv_prog_CC="$CC" # Let the user override the test.
else
  IFS="${IFS= 	}"; ac_save_ifs="$IFS"; IFS="${IFS}:"
  for ac_dir in $PATH; do
    test -z "$ac_dir" && ac_dir=.
    if test -f $ac_dir/$ac_word; then
      ac_cv_prog_CC="gcc"
      break
    fi
  done
  IFS="$ac_save_ifs"
fi
CC="$ac_cv_prog_CC"
if test -n "$CC"; then
  echo "$CC"
fi

if test -z "$CC"; then
  # Extract the first word of "cc", so it can be a program name with args.
set dummy cc; ac_word=$2

if test -n "$CC"; then
  ac_cv_prog_CC="$CC" # Let the user override the test.
else
  ac_prog_rejected=no
  IFS="${IFS= 	}"; ac_save_ifs="$IFS"; IFS="${IFS}:"
  for ac_dir in $PATH; do
    test -z "$ac_dir" && ac_dir=.
    if test -f $ac_dir/$ac_word; then
      if test "$ac_dir/$ac_word" = "/usr/ucb/cc"; then
        ac_prog_rejected=yes
	continue
      fi
      ac_cv_prog_CC="cc"
      break
    fi
  done
  IFS="$ac_save_ifs"
if test $ac_prog_rejected = yes; then
  # We found a bogon in the path, so make sure we never use it.
  set dummy $ac_cv_prog_CC
  shift
  if test $# -gt 0; then
    # We chose a different compiler from the bogus one.
    # However, it has the same basename, so the bogon will be chosen
    # first if we set CC to just the basename; use the full file name.
    shift
    set dummy "$ac_dir/$ac_word" "$@"
    shift
    ac_cv_prog_CC="$@"
  fi
fi
fi

CC="$ac_cv_prog_CC"
if test -n "$CC"; then
  echo "$CC"
fi

  test -z "$CC" && { echo "configure: error: no acceptable cc found in \$PATH" 1>&2; exit 1; }
fi
