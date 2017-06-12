package de.fehngarten.fhemswitch.config;

import android.app.Activity;
import android.os.Bundle;
//import android.util.Log;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import de.fehngarten.fhemswitch.BuildConfig;
import de.fehngarten.fhemswitch.R;
import util.IabHelper;

import static de.fehngarten.fhemswitch.global.Settings.settingLicenceKey;

public class ConfigDonate extends Activity {

    IabHelper mHelper;
    Activity activity;
    private final String TAG = "ConfigDonate";
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

        String base64EncodedPublicKey = settingLicenceKey;
        RadioButton rbu1 =(RadioButton)findViewById(R.id.donate1);
        rbu1.setChecked(true);

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(result -> {
            if (!result.isSuccess()) {
                if (BuildConfig.DEBUG) Log.d("x", "In-app Billing setup failed: " + result);
            } else {
                if (BuildConfig.DEBUG) Log.d("x", "In-app Billing is set up OK");
            }
        });
    }

    private Button.OnClickListener cancelOnClickListener = arg0 -> finish();

    private Button.OnClickListener googleOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            String donateValue = getDonateValue();
            if (donateValue != "") {
                mHelper.launchPurchaseFlow(activity, donateValue, 10001, mPurchaseFinishedListener);
            }
            //mHelper.launchPurchaseFlow(activity, donateValue, 10001, mPurchaseFinishedListener, "de.fehngarten.fhemswitch.inapp");
        }
    };

    private String getDonateValue() {
        RadioGroup valueGroup = (RadioGroup) findViewById(R.id.valueGroup);
        RadioButton valueGroupButton = (RadioButton) findViewById(valueGroup.getCheckedRadioButtonId());
        String value = "";
        if (valueGroupButton.getTag() != null) {
            value = valueGroupButton.getTag().toString();
        }
        return value;
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = (result, purchase) -> {
        if (!result.isFailure()) {
            //if (BuildConfig.DEBUG) Log.d("trace", "In-app Billing success");
            findViewById(R.id.donateThanks).setVisibility(View.VISIBLE);
            findViewById(R.id.donateRequest).setVisibility(View.GONE);
        }
    };
}
