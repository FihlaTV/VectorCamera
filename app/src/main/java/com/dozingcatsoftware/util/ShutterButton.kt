/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dozingcatsoftware.util

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

/**
 * A button designed to be used for the on-screen shutter button.
 * It's currently an `ImageView` that can call a delegate when the
 * pressed state changes.
 */
class ShutterButton(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {

    private var mOldPressed: Boolean = false
    var onShutterButtonFocus: ((Boolean) -> Unit)? = null
    var onShutterButtonClick: (() -> Unit)? = null

    /**
     * Hook into the drawable state changing to get changes to isPressed -- the
     * onPressed listener doesn't always get called when the pressed state
     * changes.
     */
    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val pressed = isPressed
        if (pressed != mOldPressed) {
            if (!pressed) {
                // When pressing the physical camera button the sequence of
                // events is:
                //    focus pressed, optional camera pressed, focus released.
                // We want to emulate this sequence of events with the shutter
                // button. When clicking using a trackball button, the view
                // system changes the the drawable state before posting click
                // notification, so the sequence of events is:
                //    pressed(true), optional click, pressed(false)
                // When clicking using touch events, the view system changes the
                // drawable state after posting click notification, so the
                // sequence of events is:
                //    pressed(true), pressed(false), optional click
                // Since we're emulating the physical camera button, we want to
                // have the same order of events. So we want the optional click
                // callback to be delivered before the pressed(false) callback.
                //
                // To do this, we delay the posting of the pressed(false) event
                // slightly by pushing it on the event queue. This moves it
                // after the optional click notification, so our client always
                // sees events in this sequence:
                //     pressed(true), optional click, pressed(false)
                post { onShutterButtonFocus?.invoke(pressed) }
            } else {
                onShutterButtonFocus?.invoke(pressed)
            }
            mOldPressed = pressed
        }
    }

    override fun performClick(): Boolean {
        val result = super.performClick()
        onShutterButtonClick?.invoke()
        return result
    }
}
