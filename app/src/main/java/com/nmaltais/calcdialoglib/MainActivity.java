/*
 * Copyright (c) 2018 Nicolas Maltais
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.nmaltais.calcdialoglib;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.nmaltais.calcdialog.CalcDialog;
import com.nmaltais.calcdialog.CalcNumpadLayout;

import java.math.BigDecimal;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity implements CalcDialog.CalcDialogCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int DIALOG_REQUEST_CODE = 0;

    private TextView valueTxv;
    private CheckBox signChk;

    private @Nullable BigDecimal value;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        if (state != null) {
            String valueStr = state.getString("value");
            if (valueStr != null) {
                value = new BigDecimal(valueStr);
            }
        }

        final CalcDialog calcDialog = CalcDialog.newInstance(DIALOG_REQUEST_CODE);

        signChk = findViewById(R.id.chk_change_sign);
        if (value == null) signChk.setEnabled(false);

        final CheckBox showAnswerChk = findViewById(R.id.chk_answer_btn);
        final CheckBox showSignChk = findViewById(R.id.chk_show_sign);
        final CheckBox clearOnOpChk = findViewById(R.id.chk_clear_operation);
        final CheckBox showZeroChk = findViewById(R.id.chk_show_zero);

        // Max value
        final CheckBox maxValChk = findViewById(R.id.chk_max_value);
        final EditText maxValEdt = findViewById(R.id.edt_max_value);
        maxValChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                maxValEdt.setEnabled(isChecked);
            }
        });
        maxValEdt.setEnabled(maxValChk.isChecked());
        maxValEdt.setText(String.valueOf(10000000000L));

        // Max integer digits
        final CheckBox maxIntChk = findViewById(R.id.chk_max_int);
        final EditText maxIntEdt = findViewById(R.id.edt_max_int);
        maxIntChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                maxIntEdt.setEnabled(isChecked);
            }
        });
        maxIntEdt.setEnabled(maxIntChk.isChecked());
        maxIntEdt.setText(String.valueOf(10));

        // Max fractional digits
        final CheckBox maxFracChk = findViewById(R.id.chk_max_frac);
        final EditText maxFracEdt = findViewById(R.id.edt_max_frac);
        maxIntChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                maxFracEdt.setEnabled(isChecked);
            }
        });
        maxFracEdt.setEnabled(maxFracChk.isChecked());
        maxFracEdt.setText(String.valueOf(8));

        // Numpad layout
        final RadioGroup numpadLayoutGroup = findViewById(R.id.radiogroup_numpad);

        // Value display
        valueTxv = findViewById(R.id.txv_result);
        valueTxv.setText(value == null ? getString(R.string.result_value_none) : value.toPlainString());

        // Open dialog button
        Button openBtn = findViewById(R.id.btn_open);
        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean signCanBeChanged = !signChk.isEnabled() || signChk.isChecked();

                String maxValueStr = maxValEdt.getText().toString();
                BigDecimal maxValue = maxValChk.isChecked() && !maxValueStr.isEmpty() ?
                        new BigDecimal(maxValueStr) : null;

                String maxIntStr = maxIntEdt.getText().toString();
                int maxInt = maxIntChk.isChecked() && !maxIntStr.isEmpty() ?
                        Integer.valueOf(maxIntStr) : CalcDialog.MAX_DIGITS_UNLIMITED;

                String maxFracStr = maxFracEdt.getText().toString();
                int maxFrac = maxFracChk.isChecked() && !maxFracStr.isEmpty() ?
                        Integer.valueOf(maxFracStr) : CalcDialog.MAX_DIGITS_UNLIMITED;

                CalcNumpadLayout layout;
                if (numpadLayoutGroup.getCheckedRadioButtonId() == R.id.radio_numpad_calc) {
                    layout = CalcNumpadLayout.CALCULATOR;
                } else {
                    layout = CalcNumpadLayout.PHONE;
                }

                // Set settings and value
                calcDialog.setValue(value)
                        .setShowSignButton(showSignChk.isChecked())
                        .setShowAnswerButton(showAnswerChk.isChecked())
                        .setSignCanBeChanged(signCanBeChanged, signCanBeChanged ? 0 : value.signum())
                        .setClearDisplayOnOperation(clearOnOpChk.isChecked())
                        .setShowZeroWhenNoValue(showZeroChk.isChecked())
                        .setMaxValue(maxValue)
                        .setMaxDigits(maxInt, maxFrac)
                        .setNumpadLayout(layout);

                FragmentManager fm = getSupportFragmentManager();
                if (fm.findFragmentByTag("calc_dialog") == null) {
                    calcDialog.show(fm, "calc_dialog");
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        if (value != null) {
            state.putString("value", value.toString());
        }
    }

    @Override
    public void onValueEntered(int requestCode, @Nullable BigDecimal value) {
        // if (requestCode == DIALOG_REQUEST_CODE) {}  <-- If there are many dialogs

        this.value = value;

        if (value == null) {
            valueTxv.setText(R.string.result_value_none);
            signChk.setEnabled(false);
        } else {
            valueTxv.setText(value.toPlainString());
            signChk.setEnabled(value.compareTo(BigDecimal.ZERO) != 0);
        }
    }
}

