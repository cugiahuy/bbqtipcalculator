package com.example.bbqtipcalculator;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 *  
 *
 * @author Ji jiwpark90
 */
public class MainActivity extends Activity {

	public static final int US_DOLLAR_IN_CENTS = 100;

	// maximum number of shifts that can exist before a shift
	private static final int MAX_PREV_SHIFTS = 5;

	// reference to the layout containing info for card tip(s)
	private LinearLayout mPrevCardTipLayout;

	// references to the current total amounts
	private EditText mCashTip;
	private EditText mCardTipTotal;
	private EditText mGratTotal;

	// collection of EditTexts for previous shifts containing previous
	// shifts' card tip
	private ArrayList<EditText> mPrevCardTip = new ArrayList<EditText>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// save the nested layout of the dynamic portion of the main layout,
		// namely the card tip section
		mPrevCardTipLayout = (LinearLayout) findViewById(R.id.layout_prev_card_tip);

		// save references to total amounts for the clear() method
		mCashTip = (EditText) findViewById(R.id.edittext_cash_tip);
		mCardTipTotal = (EditText) findViewById(R.id.edittext_card_tip_total);
		mGratTotal = (EditText) findViewById(R.id.edittext_gratuity_total);

		// set currency filters to existing EditTexts
		((EditText) findViewById(R.id.edittext_cash_tip)).setFilters(
				new InputFilter[] { new CurrencyInputFilter() });
		((EditText) findViewById(R.id.edittext_card_tip_total)).setFilters(
				new InputFilter[] { new CurrencyInputFilter() });
		((EditText) findViewById(R.id.edittext_gratuity_total)).setFilters(
				new InputFilter[] { new CurrencyInputFilter() });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Adds a new slot for a tip from a previous shift to the main layout.
	 * 
	 * @param view TODO
	 */
	public void addCardTipSlot(View view) {
		if (mPrevCardTip.size() < MAX_PREV_SHIFTS) {
			EditText newCardTipSlot = (EditText) getLayoutInflater().
					inflate(R.layout.edit_text_template, mPrevCardTipLayout, false);
			newCardTipSlot.setFilters(new InputFilter[] { new CurrencyInputFilter() });
			newCardTipSlot.setId(mPrevCardTip.size());
			mPrevCardTip.add(newCardTipSlot);
			mPrevCardTipLayout.addView(newCardTipSlot);
			Toast.makeText(MainActivity.this, Integer.toString(newCardTipSlot.getId()), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(MainActivity.this, R.string.alert_num_shift, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Calculates the current shift's tip based on the current user input.
	 * 
	 * @param view TODO
	 */
	public void calculateTip(View view) {

	}

	/**
	 * Clears all the input fields.
	 * 
	 * @param view TODO
	 */
	public void clear(View view) {
		AlertDialog.Builder builder =
				new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(R.string.alert_clear_title);
		builder.setMessage(R.string.alert_clear_message);
		builder.setNegativeButton(R.string.alert_cancel,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				// back out from delete
				dialog.cancel();
			}
		});

		builder.setPositiveButton(R.string.alert_ok,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				// remove all previous shift information
				mPrevCardTipLayout.removeAllViews();

				// reset all information regarding current shift
				mCashTip.setText("");
				mCardTipTotal.setText("");
				mGratTotal.setText("");
			}
		});
		// show the alert message
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
