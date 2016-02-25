Here are some before and after examples to give you an idea of what's
easy to do. If you come up with new ideas, I'd be interested to hear
from you!

## Example 1 ##
> Me whistling with a TV show on in the background

| ![http://spectro-edit.googlecode.com/svn/trunk/doc/examples/whistling_before_annotated.jpg](http://spectro-edit.googlecode.com/svn/trunk/doc/examples/whistling_before_annotated.jpg) | ![http://spectro-edit.googlecode.com/svn/trunk/doc/examples/whistling_after.jpg](http://spectro-edit.googlecode.com/svn/trunk/doc/examples/whistling_after.jpg) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Before ([Listen](http://spectro-edit.googlecode.com/svn/trunk/doc/examples/whistling_before.wav))                                                                                     | After ([Listen](http://spectro-edit.googlecode.com/svn/trunk/doc/examples/whistling_after.wav))                                                                 |

I recorded this sample with my laptop's built-in microphone while I
was working on the initial release of Spectro-Edit. I whistled a bit
to make a simple waveform that changes pitch over time, so I could see
if the program was working.

### Looking ###
Even before making changes, it turns out there is a fair bit we can learn even from a throwaway recording like this just by looking at it in Spectro-Edit:

  * my whistle sometimes has a single faint overtone.
  * there was a high-pitch whine in the room when I made the recording. At first I thought it was noise inside my laptop being picked up by the internal mic, but recordings I made in other rooms didn't have this. It was probably the FM stereo carrier tone from the TV broadcast, unfortunately not being filtered out by our TV tuner.
  * "S" sounds (in this case, the x in "exactly") are made of relatively high-frequency noise compared with vowels.

### Changing ###
But now the fun part: I blacked out the fundamental and overtone of my
whistle, and now the background noise comes through clearly. You can
hear what's going on with the TV, and you can't even tell I was
whistling while making this recording!

Note that I didn't have to be very careful when "painting out" the
whistle: erasing a bit of nearby background noise doesn't have a huge
effect. Likewise, missing a bit of the bleedover doesn't make the
whistling sound too apparent.