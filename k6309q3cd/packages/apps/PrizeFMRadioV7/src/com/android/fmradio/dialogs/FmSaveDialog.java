/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.fmradio.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import com.android.fmradio.FmRecorder;
import com.android.fmradio.FmService;
import com.android.fmradio.FmUtils;
import com.android.fmradio.R;

/**
 * The dialog fragment for save recording file
 */
public class FmSaveDialog extends DialogFragment {
    private static final String TAG = "FmRx/RecordDlg";

    // save recording file button
    private Button mButtonSave = null;
    // discard recording file button
    private Button mButtonDiscard = null;
    // rename recording file edit text
    private EditText mRecordingNameEditText = null;
    // recording file default name
    private String mDefaultRecordingName = null;
    // recording file to save name
    private String mRecordingNameToSave = null;
    // text view which show storage warning
    private TextView mStorageWarningTextView = null;
    private OnRecordingDialogClickListener mListener = null;

    // The default filename need't to check whether exist
    private boolean mIsNeedCheckFilenameExist = false;
    // record sd card path when start recording
    private String mRecordingSdcard = null;
    private InputMethodManager imm;

    /**
     * FM record dialog fragment, because fragment manager need empty
     * constructor to instantiated this dialog fragment when configuration
     * change
     */
    public FmSaveDialog() {
    }

    /**
     * FM record dialog fragment according name, should pass value recording
     * file name
     *
     * @param defaultName The default file name in FileSystem
     * @param recordingName The name in the dialog for show and save
     */
    public FmSaveDialog(String sdcard, String defaultName, String recordingName) {
        mRecordingSdcard = sdcard;
        mDefaultRecordingName = defaultName;
        mRecordingNameToSave = recordingName;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnRecordingDialogClickListener) activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    /**
     * When onCreate Set dialog style
     *
     * @param savedInstanceState The save instance state
     */
    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, ">>onCreate() savedInstanceState:" + savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.common_custom_dialog);
        Log.d(TAG, "<<onCreate()");
    }*/

    /**
     * Inflate view and get the operation button
     *
     * @param inflater The layout inflater
     * @param container The view group container
     * @param savedInstanceState The save instance state
     *
     * @return The inflater view
     */

    /**
     * Set the dialog edit text and other attribute
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, ">>onResume()");

        // check if storage is OK, if not enough storage, make view visible
        if (!FmUtils.hasEnoughSpace(mRecordingSdcard)) {
            mStorageWarningTextView.setVisibility(View.VISIBLE);
        }

        // have define in fm_recorder_dialog.xml length at most
        // 250(maxFileLength - suffixLength)
//        mRecordingNameEditText.setSingleLine(true);

        if (mDefaultRecordingName != null) {
            if (null != mRecordingNameToSave) {
                // this case just for,fragment recreate
				Log.d(TAG, "mRecordingNameToSave:"+mRecordingNameToSave);
                mRecordingNameEditText.setText(mRecordingNameToSave);
                if ("".equals(mRecordingNameToSave)) {
                	mButtonSave.setTextColor(getActivity().getResources().getColor(R.color.favorite_bottom_divider));
                    mButtonSave.setEnabled(false);
                }
            } else {
                mRecordingNameEditText.setText(mDefaultRecordingName);
            }

            mRecordingNameEditText.selectAll();
        }else{
			mButtonSave.setTextColor(getActivity().getResources().getColor(R.color.favorite_bottom_divider));
            mButtonSave.setEnabled(false);
		}
        mRecordingNameEditText.setHint(getActivity().getResources().getString(
                R.string.edit_recording_name_hint));
        mRecordingNameEditText.requestFocus();
        setTextChangedCallback();
        getDialog().setCanceledOnTouchOutside(false);
       // getDialog().getWindow().setSoftInputMode(
              //  WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                     //   | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Log.d(TAG, "<<onResume()");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, ">>onSaveInstanceState()");
        outState.putString("record_file_name", mRecordingNameToSave);
        outState.putString("record_default_name", mDefaultRecordingName);
        super.onSaveInstanceState(outState);
    }
    
	@Override
    public void onDestroy() {
        Log.d(TAG, ">>onDestroy()");
        mDefaultRecordingName = null;
        mRecordingNameToSave = null;
        mListener = null;
        Log.d(TAG, "<<onDestroy()");
        super.onDestroy();
    }
    

   // prize modified by longzhongping, FMRadio, for bug 73943, 2019.04.02-start
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, ">>onPause() , mRecordingNameEditText="+mRecordingNameEditText);
        if(null != mRecordingNameEditText) {
            imm.hideSoftInputFromWindow(mRecordingNameEditText.getWindowToken(),0);
        }
    }
    // prize modified by longzhongping, FMRadio, for bug 73943, 2019.04.02-end  
	@Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null) {
            mListener.onRecordingDialogClick(mRecordingNameToSave);
            mListener = null;
        }
    }
	
	@Override
    public void onStop() {
        super.onPause();
        Log.d(TAG, ">>onPause() , mRecordingNameEditText="+mRecordingNameEditText);
        if(null != mRecordingNameEditText) {
            imm.hideSoftInputFromWindow(mRecordingNameEditText.getWindowToken(),0);
        }
    }
    /**
     * This method register callback and set filter to Edit, in order to make
     * sure that user input is legal. The input can't be illegal filename and
     * can't be too long.
     */
    private void setTextChangedCallback() {
        mRecordingNameEditText.addTextChangedListener(new TextWatcher() {
            // not use, so don't need to implement it
            @Override
            public void afterTextChanged(Editable arg0) {
            }

            // not use, so don't need to implement it
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            /**
             * check user input whether include invalid character
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged() s:" + s + ", start:" + start +
                        ", before:" + before + ", count:" + count);
                // Filename changed, so we should check the new filename is
                // whether exist.
                mIsNeedCheckFilenameExist = true;
                String recordName = s.toString().trim();
                // Characters not allowed by file system
                if (recordName.length() <= 0
                        || recordName.startsWith(".")
                        || recordName.matches(".*[/\\\\:*?\"<>|\t].*")) {
                    mButtonSave.setEnabled(false);
                    mButtonSave.setTextColor(getActivity().getResources().getColor(R.color.favorite_bottom_divider));
                } else {
                    mButtonSave.setEnabled(true);
                    mButtonSave.setTextColor(getActivity().getResources().getColor(R.color.rename_dialog_button_color));
                }

                mRecordingNameToSave = mRecordingNameEditText.getText().toString().trim();
                Log.d(TAG, "onTextChanged mRecordingNameToSave:" + mRecordingNameToSave);
            }
        });
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
    	if (savedInstanceState != null) {
            mRecordingNameToSave = savedInstanceState.getString("record_file_name");
            mDefaultRecordingName = savedInstanceState.getString("record_default_name");
            mRecordingSdcard = FmService.getRecordingSdcard();
        }
        Dialog dialog = new Dialog(getActivity(), R.style.common_custom_dialog);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fm_recorder_dialog_prize, null);
        mButtonSave = (Button) view.findViewById(R.id.fm_recording_btn_save);
        mButtonSave.setOnClickListener(mButtonOnClickListener);

        mButtonDiscard = (Button) view.findViewById(R.id.fm_recording_btn_discard);
        mButtonDiscard.setOnClickListener(mButtonOnClickListener);

        mStorageWarningTextView = (TextView) view
                .findViewById(R.id.save_recording_storage_warning);

        // Set the recording edit text
        mRecordingNameEditText = (EditText) view.findViewById(R.id.fm_recording_text);
        imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mRecordingNameEditText, 0); 
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);  
        dialog.setContentView(view);
		WindowManager.LayoutParams params = 
				dialog.getWindow().getAttributes();
				params.width = (int) getActivity().getResources().getDimension(R.dimen.custom_dialog_width);
				params.height = (int) getActivity().getResources().getDimension(R.dimen.custom_dialog_height);
				dialog.getWindow().setAttributes(params);
		return dialog;
	}

	private OnClickListener mButtonOnClickListener = new OnClickListener() {
        /**
         * Define the button operation
         */
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.fm_recording_btn_save:
                String msg = null;
                // Check the recording name whether exist
                mRecordingNameToSave = mRecordingNameEditText.getText().toString().trim();
                File recordingFolderPath = new File(mRecordingSdcard, "FM Recording");
                File recordingFileToSave =
                        new File(recordingFolderPath, mRecordingNameToSave
                                + FmRecorder.RECORDING_FILE_EXTENSION);

                // If the new name is same as default name ,need't to check!
                if (mDefaultRecordingName == null) {
                    Log.e(TAG, "Error:recording file is not exist!");
                    return;
                }
                if (mDefaultRecordingName.equals(mRecordingNameToSave)) {
                    mIsNeedCheckFilenameExist = false;
                } else {
                    mIsNeedCheckFilenameExist = true;
                }

                Log.d(TAG, "save:" + mDefaultRecordingName + "->" + mRecordingNameToSave +
                        ", " + mIsNeedCheckFilenameExist);
                if (recordingFileToSave.exists() && mIsNeedCheckFilenameExist) {
                    // show a toast notification if can't renaming a file/folder
                    // to the same name
                    msg = mRecordingNameEditText.getText().toString() + " "
                            + getActivity().getResources().getString(R.string.already_exists);
                    Log.d(TAG, "file " + mRecordingNameToSave + ".ogg is already exists!");
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                } else {
                    if (null != mListener) {
                        mListener.onRecordingDialogClick(mRecordingNameToSave);
                    }
                    dismissDialog();
                }
                break;

                case R.id.fm_recording_btn_discard:
                    if (null != mListener) {
                        mListener.onRecordingDialogClick(null);
                    }
                    dismissDialog();
                    Log.d(TAG, "Discard FM recording file. ");
                    break;

                default:
                    break;
            }
        }
    };
    
    private void dismissDialog() {
        imm.hideSoftInputFromWindow(mRecordingNameEditText.getWindowToken(), 0);
        dismissAllowingStateLoss();
    }
    
    /**
     * The listener for click Save or Discard
     */
    public interface OnRecordingDialogClickListener {
        /**
         * Record dialog click callback
         *
         * @param recordingName The user input recording name
         */
        void onRecordingDialogClick(String recordingName);
    }

    /**
     * Get the latest modified recording name
     *
     * @return The latest modified recording name
     */
    public String getRecordingNameToSave() {
        return mRecordingNameToSave;
    }
}
