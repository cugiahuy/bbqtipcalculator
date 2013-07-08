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
import android.widget.RadioButton;
import android.widget.Toast;

/**
 *  
 *
 * @author Ji jiwpark90
 */
public class MainActivity extends Activity {

	public static final int US_DOLLAR_IN_CENTS = 100;
	public static final double CARD_FEE_MULTIPLIER = 0.95;
	public static final double KITCHEN_TIP_PERCENTAGE = 0.15;

	// maximum number of shifts that can exist before a shift
	private static final int MAX_PREV_SHIFTS = 5;

	// reference to the layout containing info for card tip(s)
	private LinearLayout mPrevCardTipLayout;

	// references to the current total amounts
	private EditText mCashTip;
	private EditText mCardTipTotal;
	private EditText mGratuity;
	
	// reference to the # of servers
	private int mNumServers;

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
		mGratuity = (EditText) findViewById(R.id.edittext_gratuity);
		mCardTipTotal = (EditText) findViewById(R.id.edittext_card_tip_total);

		// set currency filters to existing EditTexts
		((EditText) findViewById(R.id.edittext_cash_tip)).setFilters(
				new InputFilter[] { new CurrencyInputFilter() });
		((EditText) findViewById(R.id.edittext_card_tip_total)).setFilters(
				new InputFilter[] { new CurrencyInputFilter() });
		((EditText) findViewById(R.id.edittext_gratuity)).setFilters(
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
			// TODO
			Toast.makeText(MainActivity.this, Integer.toString(newCardTipSlot.getId()), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(MainActivity.this, R.string.alert_num_shift, Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Change the number of servers to divide the tip by.
	 * 
	 * @param view TODO
	 */
	public void onRadioButtonClicked(View view) {	    
	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.radio1:
	            mNumServers = 1;
	            break;
	        case R.id.radio2:
	            mNumServers = 2;
	            break;
	        case R.id.radio3:
	        	mNumServers = 3;
	        	break;
	    }
	}

	/**
	 * Calculates the current shift's tip based on the current user input.
	 * 
	 * @param view TODO
	 */
	public void calculateTip(View view) {
		int currentCardTip;
		int prevCardTips;
		int currentCardTipAfterFee;
		int totalTip;
		int kitchenTip;
		int serverTip;
		
		// add the cash tip to the total
		if (mCashTip.getText().toString().equals("")) {
			totalTip = 0;
		} else {
			totalTip = parseTip(mCashTip);
		}
		
		// add gratuity to the total
		if (!mGratuity.getText().toString().equals("")) {
			totalTip += parseTip(mGratuity);
		}
		
		// store actual amount * 100 cents and store it as an int to avoid 
		// dealing with doubles
		if (mCardTipTotal.getText().toString().equals("")) {
			currentCardTip = 0;	
		} else {
			currentCardTip = parseTip(mCardTipTotal);
		}
		
		// loop through the previous shifts' card tips and subtract them from
		// the total card tip reported to get the current shift's card tip
//		for (int i = 0; i < mPrevCardTip.size(); i++) {
//			EditText prevCardTip = (EditText) findViewById(i);
//			if (!prevCardTip.getText().toString().equals("")) {
//				int prevCardTipInt = parseTip(prevCardTip);
//				currentCardTip -= prevCardTipInt;
//				
//				// the sum of previous shifts' card tips shouldn't exceed the current
//				// reported total card tip
//				if (currentCardTip < 0) {
//					Toast.makeText(MainActivity.this, R.string.error_card_tip_amount, 
//							Toast.LENGTH_LONG).show();
//					return;
//				}
//			}
//		}
		
		prevCardTips = sumPrevCardTips();
		if (prevCardTips > currentCardTip) {
			Toast.makeText(MainActivity.this, R.string.error_card_tip_amount, 
			Toast.LENGTH_LONG).show();
			return;
		} else {
			currentCardTip -= prevCardTips;
		}
				
		// get the current card tip minus the 5% (in case of BBQ)
		currentCardTipAfterFee = (int) (Math.round(currentCardTip * 
				CARD_FEE_MULTIPLIER));
		
		// add the summed card tip to the total
		totalTip += currentCardTipAfterFee;
		
		// TODO to make sure the calculated total tip is accurate
		Toast.makeText(MainActivity.this, "Card tip after fee is: " + currentCardTipAfterFee, 
				Toast.LENGTH_LONG).show();
		
		// TODO TOTAL TOAST
		Toast.makeText(MainActivity.this, "Total is: " + totalTip, 
				Toast.LENGTH_LONG).show();
		
		// calculate the kitchen tip
		kitchenTip = (int) (Math.round(totalTip * KITCHEN_TIP_PERCENTAGE));
		
		// TODO KITCHEN TIP TOAST
		Toast.makeText(MainActivity.this, "Kitchen tip is: " + kitchenTip, 
				Toast.LENGTH_LONG).show();
		
		// get server tip
		serverTip = totalTip - kitchenTip;
		
		// TODO SERVER TIP TOAST
		Toast.makeText(MainActivity.this, "Server tip is: " + serverTip, 
				Toast.LENGTH_LONG).show();
		
		
	}
	
	/*
	 * Given a reference to an EditText of one of the tip fields, returns
	 * an int representation of tip.
	 * 
	 * @EditText tip Reference to the EditTex that contains the user input
	 * 			 of a tip (varies in types [kitchen, server, etc.].
	 */
	private int parseTip(EditText tip) {
		return (int) (Double.parseDouble(tip.getText().
				toString()) * US_DOLLAR_IN_CENTS);
	}
	
	/*
	 * If there were any card tips from the previous shifts, returns the sum
	 * of all the previous shifts' card tips.
	 * 
	 * @return sum of all the previous shifts' card tips
	 */
	private int sumPrevCardTips() {
		int prevCardTips = 0;
		for (int i = 0; i < mPrevCardTip.size(); i++) {
			EditText prevCardTip = (EditText) findViewById(i);
			if (!prevCardTip.getText().toString().equals("")) {
				prevCardTips += parseTip(prevCardTip);
			}
		}
		
		return prevCardTips;
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
				mGratuity.setText("");
				mCardTipTotal.setText("");
			}
		});
		// show the alert message
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
