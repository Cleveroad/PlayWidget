## HEADER
***
# Setup and usage
Use the PlayLayout in layout file
```XML
<com.cleveroad.play_widget.PlayLayout
   android:id="@+id/revealView"
   android:layout_width="wrap_content"
   android:layout_height="wrap_content"

   app:pa_progress_line_enabled="true"
   app:pa_buttons_size="48dp"
   app:pa_image_src="@drawable/pa_image"

   app:pa_progress_ball_color="#01999933"
   app:pa_progress_complete_line_color="#77FFFFFF"
   app:pa_progress_line_color="#34FF0000"

   app:pa_progress_ball_radius="4dp"
   app:pa_progress_complete_line_stroke_width="5dp"
   app:pa_progress_line_stroke_width="3dp"

   app:pa_progress_line_padding="10dp"
   app:pa_diffusers_padding="15dp"

   app:pa_big_diffuser_shadow_width="30dp"
   app:pa_medium_diffuser_shadow_width="25dp"
   app:pa_small_diffuser_shadow_width="20dp"

   app:pa_big_diffuser_color="#009933"
   app:pa_medium_diffuser_color="#003399"
   app:pa_play_button_background_tint="#123456"
/>
```
or programmatically
```JAVA
mPlayLayout = new PlayLayout.Builder(context)
                .setLayoutParams(params)
                .setBigDiffuserColorResource(R.color.test_blue)
                .setMediumDiffuserColorResource(R.color.test_green)
                .setBigDiffuserShadowWidthResource(R.dimen.test_dimen_10)
                .setMediumDiffuserShadowWidthResource(R.dimen.test_dimen_10)
                .setSmallDiffuserShadowWidthResource(R.dimen.test_dimen_10)
                .setProgressLinePaddingResource(R.dimen.test_dimen_10)
                .setProgressBallRadiusResource(R.dimen.test_dimen_20)
                .setProgressCompleteLineStrokeWidthResource(R.dimen.test_dimen_10)
                .setProgressLineStrokeWidthResource(R.dimen.test_dimen_20)
                .setProgressLineColorResource(R.color.test_blue)
                .setProgressCompleteColorResource(R.color.test_green)
                .setProgressBallColorResource(R.color.test_red)
                .setDiffusersPaddingResource(R.dimen.test_dimen_30)
                .setButtonsSizeResource(R.dimen.test_dimen_30)
                .setPlayButtonBackgroundTintList(colorStateList)
                .setImageResource(R.drawable.imgpsh_fullsize_hd)
                .build();
        container.addView(playLayout);
```

methods for changing any settings you can set via attribute in xml:
```JAVA
void setProgressEnabled(boolean enabled)
void setButtonsSize(int buttonsSize)

void setImageBitmap(Bitmap bm)
void setImageURI(@Nullable Uri uri)
void setImageDrawable(@Nullable Drawable drawable)
void setImageResource(@DrawableRes int resId)

void setProgressBallColor(int progressBallColor)
void setProgressCompleteColor(int progressCompleteColor)
void setProgressLineColor(int progressLineColor)

void setProgressBallRadius(float radius)
void setProgressCompleteLineStrokeWidth(float progressCompleteLineStrokeWidth)
void setProgressLineStrokeWidth(float progressLineStrokeWidth)

void setProgressLinePadding(int padding)
void setDiffusersPadding(int padding)

void setBigDiffuserShadowWidth(int bigDiffuserShadowWidth)
void setMediumDiffuserShadowWidth(int mediumDiffuserShadowWidth)
void setSmallDiffuserShadowWidth(int smallDiffuserShadowWidth)

void setBigDiffuserColor(int color)
void setMediumDiffuserColor(int color)
void setPlayButtonBackgroundTintList(@Nullable ColorStateList tint)
```
To check if diffusers are open use method:
```JAVA
boolean isOpen();
```
To set progress use method (0.0f - 1.0f):
```JAVA
void setProgress(float progress)
```
For providing buttons listener use:
```JAVA
void setOnButtonsClickListener(new OnButtonsClickListener() {
   @Override
   public void onShuffleClicked() { }
   @Override
   public void onSkipPreviousClicked() { }
   @Override
   public void onSkipNextClicked() { }
   @Override
   public void onRepeatClicked() { }
   @Override
   public void onPlayButtonClicked() { }
});
```
For changing shadow params you can use method:
```JAVA
void shadowChanged(
    float bigDiffuserShadowPercentage,
    float mediumDiffuserShadowPercentage,
    float smallDiffuserShadowPercentage
);
```

You can use shadow provider **VisualizerShadowChanger**. **VisualizerShadowChanger** listen to audio from android system and translate it to diffusers shadow changing.
To implement you own data shadow provider, just extend your class from **RevealView.ShadowPercentageProvider** class and use changeShadow method to set new shadow percentages.
```JAVA
protected void changeShadow(
    float bigDiffuserShadowPercentage,
    float mediumDiffuserShadowPercentage,
    float smallDiffuserShadowPercentage
    )
```

To set shadow provider use:
```JAVA
mPlayLayout.setShadowProvider( VisualizerShadowChanger.newInstance(yourAudioSessionId) );
```
where `yourAudioSessionId` is audioSession from MediaPlayer or AudioTrack or 0.
For using **VisualizerShadowChanger** need to provide permissions:
```XML
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>
```



## Support

If you have any other questions regarding the use of this library, please contact us for support at info@cleveroad.com (email subject: "Android PlayAnimation widget. Support request.")


## License
* * *
    The MIT License (MIT)

    Copyright (c) 2016 Cleveroad Inc.

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.