package com.example.bbqtipcalculator;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 *  
 *
 * @author Ji jiwpark90
 */
public class MainActivity extends Activity {
	public static final String TAG = "MainActivity";

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

	// references to the radio buttons indicating # servers
	private RadioGroup mRadioGroup;

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

		// reference to the RadioGroup indicating # servers
		mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
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
		switch(((RadioButton) view).getId()) {
		case R.id.radio1:
			mNumServers = 1;
			mRadioGroup.check(R.id.radio1);
			break;
		case R.id.radio2:
			mNumServers = 2;
			mRadioGroup.check(R.id.radio2);
			break;
		case R.id.radio3:
			mNumServers = 3;
			mRadioGroup.check(R.id.radio3);
			break;
		}
	}

	/**
	 * Calculates the current shift's tip based on the current user input.
	 * 
	 * @param view TODO
	 */
	public void calculateTip(View view) {
		// initialize all the temporary variables
		int cashTip;
		int currentCardTip;
		int prevCardTips;
		int currentCardTipAfterFee;
		int totalTip;
		int kitchenTip;
		int totalServerTip;
		int cashToWithdraw;

		// check to see that the user selected # of servers
		if (!checkNumServers()) {
			return;
		}

		// add the cash tip to the total. as of now, totalTip 
		// only contains cashTip. must save for later when
		// figuring out the cash to withdraw from the cash register
		cashTip = totalTip = parseTip(mCashTip);

		// add gratuity to the total
		totalTip += parseTip(mGratuity);

		// store current card tip total
		currentCardTip = parseTip(mCardTipTotal);

		// call helper method to loop through the previous shifts' card tips 
		// and subtract them from the total card tip reported to get the 
		// current shift's card tip
		prevCardTips = sumPrevCardTips(currentCardTip);
		if (prevCardTips < 0) {
			return;
		} else {
			currentCardTip -= prevCardTips;
		}

		// get the current card tip minus the 5% (in case of BBQ)
		currentCardTipAfterFee = (int) (currentCardTip * 
				CARD_FEE_MULTIPLIER);

		// add the summed card tip to the total
		totalTip += currentCardTipAfterFee;

		// calculate how much to take out from the cash register
		cashToWithdraw = totalTip - cashTip;

		// calculate the kitchen tip
		kitchenTip = (int) (totalTip * KITCHEN_TIP_PERCENTAGE);

		// get server tips
		totalServerTip = totalTip - kitchenTip;
		int[] tipArr = new int[mNumServers];
		getServerTips(tipArr, totalServerTip);

		// report the results in a dialog box
		reportResults(currentCardTip, currentCardTipAfterFee, totalTip, 
				kitchenTip, tipArr, totalServerTip, cashToWithdraw);
	}
	
	/*
	 * Check to see that the user selected # of servers for the current shift.
	 */
	private Boolean checkNumServers() {
		if (mRadioGroup.getCheckedRadioButtonId() < 0) {
			Toast.makeText(MainActivity.this, R.string.alert_num_servers, 
					Toast.LENGTH_LONG).show();
			mRadioGroup.requestFocus();
			return false;
		}

		return true;
	}

	/*
	 * If there were any card tips from the previous shifts, returns the sum
	 * of all the previous shifts' card tips.
	 * 
	 * @return sum of all the previous shifts' card tips. -1 if the summed amount
	 * 		   is invalid (i.e. the summed previous card tips exceed the current
	 * 		   card tip amount)
	 */
	private int sumPrevCardTips(int currentCardTip) {
		int prevCardTips = 0;
		for (int i = 0; i < mPrevCardTip.size(); i++) {
			EditText prevCardTip = (EditText) findViewById(i);
			if (!prevCardTip.getText().toString().equals("")) {
				prevCardTips += parseTip(prevCardTip);
			}
		}
		
		// check for a valid sum
		if (prevCardTips > currentCardTip) {
			Toast.makeText(MainActivity.this, R.string.error_card_tip_amount, 
					Toast.LENGTH_LONG).show();
			return -1;
		} else {
			return prevCardTips;
		}
	}
	
	/*
	 * Returns an array with each element representing the tip amount 
	 * for each individual server.
	 */
	private void getServerTips(int[] tipArr, int totalServerTip) {
		// collect information for the server tip(s)
		int individualServerTip, lastServerTip;
		individualServerTip = lastServerTip = totalServerTip / mNumServers;

		// if the tip is being split among multiple servers, the last server's
		// tip may vary by up to 2 cents
		if (mNumServers > 1) {
			lastServerTip = individualServerTip + (totalServerTip % mNumServers);

			// if the last server's tip varies than the rest's by more than
			// 1 cent, adjust it
			if (lastServerTip - individualServerTip > 1) {
				lastServerTip -= 2;
				individualServerTip++;
			}
		}

		// add in the servers' tips
		for (int i = 0; i < mNumServers; i++) {
			if (i != mNumServers - 1) {
				tipArr[i] = individualServerTip;
			} else {
				// adds to the array the last server's tip at the end
				tipArr[i] = lastServerTip;
			}
		}
	}

	/*
	 * Given a reference to an EditText of one of the tip fields, returns
	 * an int representation of tip.
	 * 
	 * @EditText tip Reference to the EditTex that contains the user input
	 * 			 of a tip (varies in types [kitchen, server, etc.].
	 */
	private int parseTip(EditText tip) {
		if (tip.getText().toString().equals("")) {
			return 0;
		} else {
			return (int) (Double.parseDouble(tip.getText().
					toString()) * US_DOLLAR_IN_CENTS);
		}
	}
	
	/*
	 * Reports the result of the tip calculations.
	 */
	private void reportResults(int cardTipBeforeFee, int cardTipAfterFee, 
			int totalTip, int kitchenTip, int[] tipArr, int totalServerTip,
			int cashToWithdraw) {
		Dialog dialog = new Dialog(this);
		dialog.setTitle(R.string.title_tip_report);
		dialog.setContentView(R.layout.dialog_report);
		dialog.setCanceledOnTouchOutside(true);
		
		((TextView) dialog.findViewById(R.id.textview_card_tip_before_fee_report)).
				setText("\t$" + (double) cardTipBeforeFee / US_DOLLAR_IN_CENTS);
		((TextView) dialog.findViewById(R.id.textview_card_tip_after_fee_report)).
				setText("\t$" + (double) cardTipAfterFee / US_DOLLAR_IN_CENTS);
		((TextView) dialog.findViewById(R.id.textview_total_tip_report)).
				setText("\t$" + (double) totalTip / US_DOLLAR_IN_CENTS + "");
		((TextView) dialog.findViewById(R.id.textview_kitchen_tip_report)).
				setText("\t$" + (double) kitchenTip / US_DOLLAR_IN_CENTS + "");
		((TextView) dialog.findViewById(R.id.textview_total_server_tip_report)).
				setText("\t$" + (double) totalServerTip / US_DOLLAR_IN_CENTS + "");
		((TextView) dialog.findViewById(R.id.textview_cash_to_withdraw_report)).
				setText("\t$" + (double) cashToWithdraw / US_DOLLAR_IN_CENTS + "");
		
		// get the layout to insert the individual server tip reports into
		LinearLayout serverTipReportLayout = (LinearLayout) dialog.
				findViewById(R.id.layout_server_tip_report);
		
		// loop through to insert the individual server tips
		for (int i = 0; i < mNumServers; i++) {
			TextView newServerTip = (TextView) dialog.getLayoutInflater().
					inflate(R.layout.individual_server_tip_template, 
					serverTipReportLayout, false);
			newServerTip.setText("\tServer " + (i + 1) + ": $" + 
					(double) tipArr[i] / US_DOLLAR_IN_CENTS);
			serverTipReportLayout.addView(newServerTip);
		}

		dialog.show();
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

				// reset the radio button selection
				mRadioGroup.clearCheck();
			}
		});
		// show the alert message
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
