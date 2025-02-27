/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.google.android.material.textfield;

import com.google.android.material.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.textfield.TextInputLayout.OnEditTextAttachedListener;
import com.google.android.material.textfield.TextInputLayout.OnEndIconChangedListener;

/** Default initialization of the clear text end icon {@link TextInputLayout.EndIconMode}. */
class ClearTextEndIconDelegate extends EndIconDelegate {

  private static final int ANIMATION_FADE_DURATION = 100;
  private static final int ANIMATION_SCALE_DURATION = 150;
  private static final float ANIMATION_SCALE_FROM_VALUE = 0.8f;

  private final TextWatcher clearTextEndIconTextWatcher =
      new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(@NonNull Editable s) {
          if (endLayout.getSuffixText() != null) {
            return;
          }
          animateIcon(shouldBeVisible());
        }
      };
  private final OnFocusChangeListener onFocusChangeListener =
      new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
          animateIcon(shouldBeVisible());
        }
      };
  private final OnEditTextAttachedListener clearTextOnEditTextAttachedListener =
      new OnEditTextAttachedListener() {
        @Override
        public void onEditTextAttached(@NonNull TextInputLayout textInputLayout) {
          EditText editText = textInputLayout.getEditText();
          textInputLayout.setEndIconVisible(shouldBeVisible());
          editText.setOnFocusChangeListener(onFocusChangeListener);
          endIconView.setOnFocusChangeListener(onFocusChangeListener);
          // Make sure there's always only one clear text text watcher added
          editText.removeTextChangedListener(clearTextEndIconTextWatcher);
          editText.addTextChangedListener(clearTextEndIconTextWatcher);
        }
      };
  private final OnEndIconChangedListener endIconChangedListener =
      new OnEndIconChangedListener() {
        @Override
        public void onEndIconChanged(@NonNull TextInputLayout textInputLayout, int previousIcon) {
          final EditText editText = textInputLayout.getEditText();
          if (editText != null && previousIcon == TextInputLayout.END_ICON_CLEAR_TEXT) {
            // Remove any listeners set on the edit text.
            editText.post(
                new Runnable() {
                  @Override
                  public void run() {
                    editText.removeTextChangedListener(clearTextEndIconTextWatcher);
                    // Make sure icon view is visible.
                    animateIcon(/* show= */ true);
                  }
                });
            if (editText.getOnFocusChangeListener() == onFocusChangeListener) {
              editText.setOnFocusChangeListener(null);
            }
            if (endIconView.getOnFocusChangeListener() == onFocusChangeListener) {
              endIconView.setOnFocusChangeListener(null);
            }
          }
        }
      };

  private AnimatorSet iconInAnim;
  private ValueAnimator iconOutAnim;

  ClearTextEndIconDelegate(@NonNull EndCompoundLayout endLayout, @DrawableRes int customEndIcon) {
    super(endLayout, customEndIcon);
  }

  @Override
  void initialize() {
    endLayout.setEndIconDrawable(
        customEndIcon == 0 ? R.drawable.mtrl_ic_cancel : customEndIcon);
    endLayout.setEndIconContentDescription(
        endLayout.getResources().getText(R.string.clear_text_end_icon_content_description));
    endLayout.setEndIconCheckable(false);
    endLayout.setEndIconOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            Editable text = textInputLayout.getEditText().getText();
            if (text != null) {
              text.clear();
            }

            endLayout.refreshEndIconDrawableState();
          }
        });
    textInputLayout.addOnEditTextAttachedListener(clearTextOnEditTextAttachedListener);
    endLayout.addOnEndIconChangedListener(endIconChangedListener);
    initAnimators();
  }

  @Override
  void onSuffixVisibilityChanged(boolean visible) {
    if (endLayout.getSuffixText() == null) {
      return;
    }
    animateIcon(visible);
  }

  private void animateIcon(boolean show) {
    boolean shouldSkipAnimation = endLayout.isEndIconVisible() == show;
    if (show && !iconInAnim.isRunning()) {
      iconOutAnim.cancel();
      iconInAnim.start();
      if (shouldSkipAnimation) {
        iconInAnim.end();
      }
    } else if (!show) {
      iconInAnim.cancel();
      iconOutAnim.start();
      if (shouldSkipAnimation) {
        iconOutAnim.end();
      }
    }
  }

  private void initAnimators() {
    ValueAnimator scaleAnimator = getScaleAnimator();
    ValueAnimator fadeAnimator = getAlphaAnimator(0, 1);
    iconInAnim = new AnimatorSet();
    iconInAnim.playTogether(scaleAnimator, fadeAnimator);
    iconInAnim.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            endLayout.setEndIconVisible(true);
          }
        });
    iconOutAnim = getAlphaAnimator(1, 0);
    iconOutAnim.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            endLayout.setEndIconVisible(false);
          }
        });
  }

  private ValueAnimator getAlphaAnimator(float... values) {
    ValueAnimator animator = ValueAnimator.ofFloat(values);
    animator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    animator.setDuration(ANIMATION_FADE_DURATION);
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator animation) {
            float alpha = (float) animation.getAnimatedValue();
            endIconView.setAlpha(alpha);
          }
        });

    return animator;
  }

  private ValueAnimator getScaleAnimator() {
    ValueAnimator animator = ValueAnimator.ofFloat(ANIMATION_SCALE_FROM_VALUE, 1);
    animator.setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
    animator.setDuration(ANIMATION_SCALE_DURATION);
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator animation) {
            float scale = (float) animation.getAnimatedValue();
            endIconView.setScaleX(scale);
            endIconView.setScaleY(scale);
          }
        });
    return animator;
  }

  private boolean shouldBeVisible() {
    EditText editText = textInputLayout.getEditText();
    return editText != null
        && (editText.hasFocus() || endIconView.hasFocus())
        && editText.getText().length() > 0;
  }
}
