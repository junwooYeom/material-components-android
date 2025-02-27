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

import androidx.annotation.NonNull;

/**
 * Removes anything related to any {@link TextInputLayout.EndIconMode}.
 */
class NoEndIconDelegate extends EndIconDelegate {
  NoEndIconDelegate(@NonNull EndCompoundLayout endLayout) {
    super(endLayout, 0);
  }

  @Override
  void initialize() {
    endLayout.setEndIconOnClickListener(null);
    endLayout.setEndIconDrawable(null);
    endLayout.setEndIconContentDescription(null);
  }
}
