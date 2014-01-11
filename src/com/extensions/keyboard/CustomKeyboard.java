package com.extensions.keyboard;

import java.util.ArrayList;

import org.mariotaku.twidere.R;
import com.pahans.sinhala.droid.project.SinhalaDroid;
import org.mariotaku.twidere.view.StatusComposeEditText;
import android.app.Activity;


import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;



public class CustomKeyboard {

    /** A link to the KeyboardView that is used to render this CustomKeyboard. */
    private KeyboardView mKeyboardView;
    /** A link to the activity that hosts the {@link #mKeyboardView}. */
    private Activity     mHostActivity;
    /**A link to the suggestions view  */
    private CandidateView mCandidateView;
    /**Suggestions getter */
    private SuggestionGenerator gen;
    
    /** The key (code) handler. */
    private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {
    	//defining of special codes for the keylistener
        public final static int CodeDelete   = -5; // Keyboard.KEYCODE_DELETE
        public final static int CodeCancel   = -3; // Keyboard.KEYCODE_CANCEL
        public final static int CodeAllLeft  = 55001;
        public final static int CodeLeft     = 55002;
        public final static int CodeRight    = 55003;
        public final static int CodeAllRight = 55004;
        public final static int CodeClear    = 55006;
        public final static int CodeSymbols  = 600000;
        public final static int CodeEnglish  = 700000;
        public final static int CodeShift    = -1;
        public final static int CodeAlt    = -2;
   
        private boolean sym = false;
        private boolean sym_shift = false;
        private boolean sym_smile = false;
        private boolean sinhala = true;
        private boolean sinhalaShifted = false;
        private boolean english = false;
        private boolean englishShifted = false;
        
        private AsyncTask<CandidateView,Void,ArrayList<String>> candidateTaskHolder = null;
        
        @Override 
        public void onKey(int primaryCode, int[] keyCodes) {
            // NOTE We can say '<Key android:codes="49,50" ... >' in the xml file; all codes come in keyCodes, the first in this list in primaryCode
            // Get the EditText and its Editable
        	View focusCurrent = mHostActivity.getWindow().getCurrentFocus();
            if( focusCurrent==null || (focusCurrent.getClass()!=EditText.class && focusCurrent.getClass()!=StatusComposeEditText.class) ) return;
            EditText edittext = (EditText) focusCurrent;
            Editable editable = edittext.getText();
            int start = edittext.getSelectionStart();
            // Apply the key to the edittext
            if( primaryCode==CodeCancel ){
                hideCustomKeyboard();
            } else if( primaryCode==CodeDelete ) {
                if( editable!=null && start>0 ){ 
                	editable.delete(start - 1, start);
                	candidateTaskHolder = setSuggestions(editable.toString(),start-1,candidateTaskHolder);
                }
            } else if( primaryCode==CodeClear ) {
                if( editable!=null ) editable.clear();
            } else if( primaryCode==CodeLeft ) {
                if( start>0 ) edittext.setSelection(start - 1);
            } else if( primaryCode==CodeRight ) {
                if (start < edittext.length()) edittext.setSelection(start + 1);
            } else if( primaryCode==CodeAllLeft ) {
                edittext.setSelection(0);
            } else if( primaryCode==CodeAllRight ) {
                edittext.setSelection(edittext.length());
            }else if( primaryCode == CodeSymbols ) {
            	//logic to handle the shift key operation
            	if(!(sym||sym_shift||sym_smile)){
            	mKeyboardView.setKeyboard(new Keyboard(mHostActivity, R.xml.symbols));
            	sym = true;
        		sym_shift = false;
        		sinhala = false;
        		sinhalaShifted = false;
        		sym_smile = false;
            	}else{
            		mKeyboardView.setKeyboard(new Keyboard(mHostActivity, R.xml.kbd));
            		sym = false;
            		sym_shift = false;
            		sinhala = true;
            		sinhalaShifted = false;
            		sym_smile = false;
            	}
                mKeyboardView.setPreviewEnabled(false); // NOTE Do not show the preview balloons
                mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
                mKeyboardView.refreshDrawableState();
            }else if(primaryCode==CodeEnglish){
            	if(sinhala||sinhalaShifted){
            		mKeyboardView.setKeyboard(new Keyboard(mHostActivity, R.xml.qwerty));
            		sinhala = false;
            		english = true;
            	}else if(english||englishShifted){
            		mKeyboardView.setKeyboard(new Keyboard(mHostActivity, R.xml.kbd));
            		sinhala = true;
            		english = false;
            	}
        		mKeyboardView.setPreviewEnabled(false); // NOTE Do not show the preview balloons
                mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
            } 
            else if( primaryCode==CodeShift ) {
            	if(sym){
            		mKeyboardView.setKeyboard(new Keyboard(mHostActivity, R.xml.symbols_shift));
            		sym = false;
            		sym_smile = false;
            		sym_shift = true;
            		sinhala = false;
            		sinhalaShifted = false;
            		english = false;
            		englishShifted = false;
            	}
            	else if(sym_shift){
            		mKeyboardView.setKeyboard(new Keyboard(mHostActivity, R.xml.symbols_smileys));
            		sym = false;
            		sym_shift = false;
            		sinhala = false;
            		sym_smile = true;
            		sinhalaShifted = false;
            		english = false;
            		englishShifted = false;
            	}else if(sym_smile){
            		mKeyboardView.setKeyboard(new Keyboard(mHostActivity, R.xml.symbols));
            		sym = true;
            		sym_shift = false;
            		sinhala = false;
            		sym_smile = false;
            		sinhalaShifted = false;
            		english = false;
            		englishShifted = false;
            	}else if(sinhala){
            		mKeyboardView.setKeyboard(new Keyboard(mHostActivity, R.xml.kbd_shift));
            		sym = false;
            		sym_shift = false;
            		sinhala = false;
            		sym_smile = false;
            		sinhalaShifted = true;
            		english = false;
            		englishShifted = false;
            	}else if(sinhalaShifted){
            		mKeyboardView.setKeyboard(new Keyboard(mHostActivity, R.xml.kbd));
            		sym = false;
            		sym_shift = false;
            		sinhala = true;
            		sym_smile = false;
            		sinhalaShifted = false;
            		english = false;
            		englishShifted = false;
            	}else if(english){
            		mKeyboardView.setKeyboard(new Keyboard(mHostActivity, R.xml.qwerty_shifted));
            		sym = false;
            		sym_shift = false;
            		sinhala = false;
            		sym_smile = false;
            		sinhalaShifted = false;
            		english = false;
            		englishShifted = true;
            	}else if(englishShifted){
            		mKeyboardView.setKeyboard(new Keyboard(mHostActivity, R.xml.qwerty));
            		sym = false;
            		sym_shift = false;
            		sinhala = false;
            		sym_smile = false;
            		sinhalaShifted = false;
            		english = true;
            		englishShifted = false;
            	}
                mKeyboardView.setPreviewEnabled(false); // NOTE Do not show the preview balloons
                mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
            } else { // insert character
                insertCharacterToView(editable,start,edittext,Character.toString((char) primaryCode));
            }
        }
        
        private void insertCharacterToView(Editable editable, int start, EditText edittext, CharSequence inString) {
        	editable.insert(start, inString);
            int now = edittext.getSelectionStart();
            String temp = SinhalaDroid.convert(editable.toString());
            editable.clear();
            editable.insert(0, temp);
            if(now<editable.length()){
            	edittext.setSelection(now);
            }
            else{
            	edittext.setSelection(editable.length());
            	now = editable.length();
            }
            candidateTaskHolder = setSuggestions(temp,now,candidateTaskHolder);
        }

		private AsyncTask<CandidateView,Void,ArrayList<String>> setSuggestions(String temp,int now,AsyncTask<CandidateView,Void,ArrayList<String>> holder) {
        	if(holder!=null)
        		holder.cancel(true);
        	if(temp!=null && temp.length()>0 && !temp.endsWith(" ")){
            	String last = "";
            	char[] text = temp.toCharArray();
            	int iter = now-1;
            	while(iter>=0 && text[iter]!=' ')
            		iter--;
            	iter++;
            	while(text.length>iter && (text[iter]!=' ')){
            		last += text[iter];
            		iter++;
            	}
            	//TODO add logic to update database with new words and frequency
            	return gen.getSuggestions(last,mCandidateView);
            }else{
            	mCandidateView.setSuggestions(null, false, false);
            	return null;
            }
		}

		@Override public void onPress(int arg0) {
        }

        @Override public void onRelease(int primaryCode) {
        }

        @Override public void onText(CharSequence text) {
        	View focusCurrent = mHostActivity.getWindow().getCurrentFocus();
        	if( focusCurrent==null || (focusCurrent.getClass()!=EditText.class && focusCurrent.getClass()!=StatusComposeEditText.class) ) return;
            EditText edittext = (EditText) focusCurrent;
            Editable editable = edittext.getText();
            int start = edittext.getSelectionStart();
            insertCharacterToView(editable,start,edittext,text);
        }

        @Override public void swipeDown() {
        }

        @Override public void swipeLeft() {
        }

        @Override public void swipeRight() {
        }

        @Override public void swipeUp() {
        }
    };

    /**
     * Create a custom keyboard, that uses the KeyboardView (with resource id <var>viewid</var>) of the <var>host</var> activity,
     * and load the keyboard layout from xml file <var>layoutid</var> (see {@link Keyboard} for description).
     * Note that the <var>host</var> activity must have a <var>KeyboardView</var> in its layout (typically aligned with the bottom of the activity).
     * Note that the keyboard layout xml file may include key codes for navigation; see the constants in this class for their values.
     * Note that to enable EditText's to use this custom keyboard, call the {@link #registerEditText(int)}.
     *
     * @param host The hosting activity.
     * @param viewid The id of the KeyboardView.
     * @param layoutid The id of the xml file containing the keyboard layout.
     */
    public CustomKeyboard(Activity host, int viewid, int layoutid) {
    	gen = new SuggestionGenerator(host);
    	mHostActivity= host;
        mKeyboardView= (KeyboardView)mHostActivity.findViewById(viewid);
        mKeyboardView.setKeyboard(new Keyboard(mHostActivity, layoutid));
        mKeyboardView.setPreviewEnabled(false); // NOTE Do not show the preview balloons
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
        mCandidateView = new CandidateView(this.mKeyboardView.getContext(),host);
        LinearLayout suggestions = (LinearLayout) mHostActivity.findViewById(R.id.suggestions);
        mCandidateView = new CandidateView(mHostActivity,mHostActivity);
        suggestions.addView(mCandidateView);
        // Hide the standard keyboard initially
        mHostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /** Returns whether the CustomKeyboard is visible. */
    public boolean isCustomKeyboardVisible() {
        return mKeyboardView.getVisibility() == View.VISIBLE;
    }

    /** Make the CustomKeyboard visible, and hide the system keyboard for view v. */
    public void showCustomKeyboard( View v ) {
    	mCandidateView.setVisibility(View.VISIBLE);
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
        mCandidateView.setEnabled(true);
        if( v!=null ) ((InputMethodManager)mHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /** Make the CustomKeyboard invisible. */
    public void hideCustomKeyboard() {
        mKeyboardView.setVisibility(View.GONE);
        mCandidateView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
    }

    /**
     * Register <var>EditText<var> with resource id <var>resid</var> (on the hosting activity) for using this custom keyboard.
     *
     * @param resid The resource id of the EditText that registers to the custom keyboard.
     */
    public void registerEditText(int resid) {
        // Find the EditText 'resid'
        EditText edittext= (EditText)mHostActivity.findViewById(resid);
        // Make the custom keyboard appear
        edittext.setOnFocusChangeListener(new OnFocusChangeListener() {
            // NOTE By setting the on focus listener, we can show the custom keyboard when the edit box gets focus, but also hide it when the edit box loses focus
            @Override public void onFocusChange(View v, boolean hasFocus) {
            	if( hasFocus ) showCustomKeyboard(v); else hideCustomKeyboard();
            }
        });
        edittext.setOnClickListener(new OnClickListener() {
            // NOTE By setting the on click listener, we can show the custom keyboard again, by tapping on an edit box that already had focus (but that had the keyboard hidden).
            @Override public void onClick(View v) {
                showCustomKeyboard(v);
            }
        });
        // Disable standard keyboard hard way
        // NOTE There is also an easy way: 'edittext.setInputType(InputType.TYPE_NULL)' (but you will not have a cursor, and no 'edittext.setCursorVisible(true)' doesn't work )
        edittext.setOnTouchListener(new OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
            	EditText edittext = (EditText) v;
                int inType = edittext.getInputType();       // Backup the input type
                edittext.onTouchEvent(event);               // Call native handler
                int now = edittext.getSelectionStart();
                edittext.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
                edittext.setInputType(inType);              // Restore input type
                edittext.setSelection(now);
                return true; 								// Consume touch event
            }
        });
        // Disable spell check (hex strings look like words to Android)
        edittext.setInputType(edittext.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

}
