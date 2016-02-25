## Startup ##

On Windows, Mac OS X, and most desktop Linux systems, simply double-click the spectroedit-x.y.jar file. If that doesn't work, try `java -jar spectroedit-x.y.jar` instead.

In all cases, ensure you have a fully-compliant Java 5 runtime environment on your system. To check your JRE version on the command line, execute the command `java -version`. See http://java.com/getjava if you need assistance installing or upgrading your JRE.

## Choosing a File ##

Once spectro-edit is started, you will see a file dialog. Choose any 16-bit mono WAV or AIFF file. Support for opening stereo recordings and MP3 or Ogg files will be available in a future version of Spectro-Edit.

Also, this early version of Spectro-Edit does a bad job sizing its window when you open a long audio clip. You'll have more success at this point if you limit yourself to clips of 15 seconds or less. Again, future versions of the user interface will do a better job with long recordings.

## Looking ##

What you see is a display of your audio clip with time progressing across the screen from left to right (the same as a traditional waveform editor such as Audacity or CoolEdit), but up and down, you see the amount of signal energy at the various frequencies--low at the bottom to high at the top.

## Listening ##

Press the **Play** button to begin playback. Presently, there is no visual playback position indicator. You'll have to use your ears for now.

When playing, the play button becomes the **Pause** button. Playback will pause after you press this, and you can resume it later from the same point by pressing play again.

_Known bug: When playback is over, the button doesn't yet revert to Play, but if you hit it twice, playback will restart from the beginning._

At any time, you can reset the playback position to the beginning by pressing **Rewind**.

## Modifying ##

You can erase any part of the signal by pressing and/or dragging the mouse over the visual display. You can even make modifications during playback, and you will hear them right away.

Currently, there is no way to undo your changes, but this is planned for a future version.

## Saving Your Work ##

When you're happy with the way things sound, you can save it out as a 16-bit WAV file. Simply press the **Save...** button and choose the save location in the dialog that pops up. Please be careful: Spectro-Edit does not currently prompt you before overwriting an existing file.