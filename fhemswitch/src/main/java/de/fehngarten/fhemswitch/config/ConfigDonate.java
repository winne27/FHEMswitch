package de.fehngarten.fhemswitch.config;

import android.app.Activity;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import de.fehngarten.fhemswitch.R;
import util.IabHelper;
import util.IabResult;

public class ConfigDonate extends Activity {

    IabHelper mHelper;
    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
        setContentView(R.layout.donate);

        Button googleButton = (Button) findViewById(R.id.googleButton);
        googleButton.setOnClickListener(googleOnClickListener);

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(cancelOnClickListener);

        Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(cancelOnClickListener);

        String base64EncodedPublicKey = getResources().getString(R.string.licenceKey);

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup((IabResult result) -> {
 /*
                if (!result.isSuccess()) {
                    Log.d("trace", "In-app Billing setup failed: " + result);
                } else {
                    Log.d("trace", "In-app Billing is set up OK");
                }
 */
        });
    }

    private Button.OnClickListener cancelOnClickListener = arg0 -> finish();

    private Button.OnClickListener googleOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            String donateValue = getDonateValue();
            mHelper.launchPurchaseFlow(activity, donateValue, 10001, mPurchaseFinishedListener, "mypurchasetoken");
        }
    };

    private String getDonateValue() {
        RadioGroup valueGroup = (RadioGroup) findViewById(R.id.valueGroup);
        RadioButton valueGroupButton = (RadioButton) findViewById(valueGroup.getCheckedRadioButtonId());
        //Log.i("value", donateValue);
        return valueGroupButton.getTag().toString();
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = (result, purchase) -> {
        if (!result.isFailure()) {
            //Log.d("trace", "In-app Billing success");
            findViewById(R.id.donateThanks).setVisibility(View.VISIBLE);
            findViewById(R.id.donateRequest).setVisibility(View.GONE);
        }
    };
}
