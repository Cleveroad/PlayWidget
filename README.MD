#PlayWidget [![Awesome](https://cdn.rawgit.com/sindresorhus/awesome/d7305f38d29fed78fa85652e3a63e154dd8e8829/media/badge.svg)](https://github.com/sindresorhus/awesome) <img src="https://www.cleveroad.com/public/comercial/label-android.svg" height="20"> <a href="https://www.cleveroad.com/?utm_source=github&utm_medium=label&utm_campaign=contacts"><img src="https://www.cleveroad.com/public/comercial/label-cleveroad.svg" height="20"></a>
![Header image](/images/header.png)

## Brand new Play Widget for Android by Cleveroad

Are you creating a music app? Or maybe you want to add some creative detail to an existing one? Then you’ve come to the right place! Play Widget for Android created by Cleveroad can be easily integrated with your music app to spruce the user experience up. Break the monotony and make your music player unique!

![Demo image](/images/demo.gif)
####Take a look at the animation on <strong><a target="_blank" href="https://youtu.be/-8Yt8EZUdZE?list=PLi-FH7__aeiydOwY_1q5I8P2EUSseqUCj">Play Widget for Android on YouTube</a></strong> in HD quality.

New distinct things encourage users to launch your app again. Add the Play Widget component to your app and enjoy the benefits  — it’s easy as one-two-three.


[![Awesome](/images/logo-footer.png)](https://www.cleveroad.com/?utm_source=github&utm_medium=label&utm_campaign=contacts)



## Setup and usage
To include this library to your project add dependency in **build.gradle** file:
```groovy
    dependencies {
        compile 'com.cleveroad:play-widget:1.0.0'
    }
```

Use the PlayLayout in layout file
```XML
<com.cleveroad.play_widget.PlayLayout
   android:id="@+id/playLayout"
   android:layout_width="wrap_content"
   android:layout_height="wrap_content"

   app:pw_progress_line_enabled="true"
   app:pw_buttons_size="48dp"
   app:pw_image_src="@drawable/pa_image"

   app:pw_progress_ball_color="#01999933"
   app:pw_progress_complete_line_color="#77FFFFFF"
   app:pw_progress_line_color="#34FF0000"

   app:pw_progress_ball_radius="4dp"
   app:pw_progress_complete_line_stroke_width="5dp"
   app:pw_progress_line_stroke_width="3dp"

   app:pw_progress_line_padding="10dp"
   app:pw_diffusers_padding="15dp"

   app:pw_big_diffuser_shadow_width="30dp"
   app:pw_medium_diffuser_shadow_width="25dp"
   app:pw_small_diffuser_shadow_width="20dp"

   app:pw_big_diffuser_color="#009933"
   app:pw_medium_diffuser_color="#003399"
   app:pw_play_button_background_tint="#123456"
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
void setOnButtonsLongClickListener(new OnButtonsLongClickListener() {
    @Override
    public void onShuffleLongClicked() {}
    @Override
    public void onSkipPreviousLongClicked() {}
    @Override
    public void onSkipNextLongClicked() {}
    @Override
    public void onRepeatLongClicked() {}
    @Override
    public void onPlayButtonLongClicked() {}
});
```
For providing progress changed listener use:
```JAVA
void setOnProgressChangedListener(new OnProgressChangedListener() {
    @Override
    void onPreSetProgress();
    @Override
    public void onProgressChanged(float progress) {}
});
```
Method `onPreSetProgress` will be called when user touch progress line to start changing progress.
For changing shadow params you can use method:
```JAVA
void shadowChanged(
    float bigDiffuserShadowPercentage,
    float mediumDiffuserShadowPercentage,
    float smallDiffuserShadowPercentage
);
```

You can use shadow provider **VisualizerShadowChanger**. **VisualizerShadowChanger** listen to audio from android system and translate it to diffusers shadow changing.
To implement you own data shadow provider, just extend your class from **PlayLayout.ShadowPercentageProvider** class and use changeShadow method to set new shadow percentages.
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

If you have any other questions regarding the use of this library, please contact us for support at info@cleveroad.com (email subject: "Android PlayWidget widget. Support request.")


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
