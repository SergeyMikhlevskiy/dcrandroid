package com.dcrandroid.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dcrandroid.R;
import com.dcrandroid.data.Account;
import com.dcrandroid.data.Constants;
import com.dcrandroid.util.DcrConstants;
import com.dcrandroid.util.PreferenceUtil;
import com.dcrandroid.util.Utils;
import com.google.zxing.EncodeHintType;

import net.glxn.qrgen.android.QRCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Macsleven on 28/11/2017.
 */

public class ReceiveFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemSelectedListener{
    ImageView imageView;
    LinearLayout ReceiveContainer;
    private TextView address;
    ArrayAdapter dataAdapter;
    List<String> categories;
    PreferenceUtil preferenceUtil;
    List<Integer> accountNumbers = new ArrayList<>();
    private DcrConstants constants;
    private boolean firstTrial = true;
    long startTime = 0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        if(getContext() == null){
            return null;
        }
        constants = DcrConstants.getInstance();
        preferenceUtil = new PreferenceUtil(getContext());
        View rootView = inflater.inflate(R.layout.content_receive, container, false);

        ReceiveContainer = rootView.findViewById(R.id.receive_container);
        imageView = rootView.findViewById(R.id.bitm);
        address = rootView.findViewById(R.id.barcode_address);
        Button buttonGenerate = rootView.findViewById(R.id.btn_gen_new_addr);
        final Spinner accountSpinner = rootView.findViewById(R.id.recieve_dropdown);
        accountSpinner.setOnItemSelectedListener(this);
        // Spinner Drop down elements
        categories = new ArrayList<>();

        dataAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        accountSpinner.setAdapter(dataAdapter);

        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.copyToClipboard(getContext(),address.getText().toString(), getString(R.string.address_copy_text));
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(address.getText().toString());
            }
        });
        buttonGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAddress(accountNumbers.get(accountSpinner.getSelectedItemPosition()));
            }
        });
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle(getString(R.string.receive));
        startTime = System.currentTimeMillis();
        prepareAccounts();

        return rootView;
    }

    public void copyToClipboard(String copyText) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(copyText);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText(getString(R.string.your_address), copyText);
            clipboard.setPrimaryClip(clip);
        }
        Toast toast = Toast.makeText(getContext(),
                R.string.address_copy_text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, -190);
        toast.show();
    }

    private void prepareAccounts(){
        try{
            final ArrayList<Account> accounts  = Account.parse(constants.wallet.getAccounts(preferenceUtil.getBoolean(Constants.SPEND_UNCONFIRMED_FUNDS) ? 0 : Constants.REQUIRED_CONFIRMATIONS));
            accountNumbers.clear();
            categories.clear();
            for(int i = 0; i < accounts.size(); i++){
                if(accounts.get(i).getAccountName().trim().equalsIgnoreCase(Constants.IMPORTED)){
                    continue;
                }
                categories.add(i, accounts.get(i).getAccountName());
                accountNumbers.add(accounts.get(i).getAccountNumber());
            }
            getAddress(0);
            dataAdapter.notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getAddress(final int accountNumber){
        try {
            final String receiveAddress = constants.wallet.addressForAccount(accountNumber);
            preferenceUtil.set(Constants.RECENT_ADDRESS,receiveAddress);
            address.setText(receiveAddress);
            imageView.setImageBitmap(QRCode.from("decred:"+receiveAddress).withHint(EncodeHintType.MARGIN, 0).withSize(300, 300).withColor(Color.BLACK, Color.TRANSPARENT).bitmap());
        } catch (Exception e) {
            e.printStackTrace();
            Looper.prepare();
            Toast.makeText(ReceiveFragment.this.getContext(),getString(R.string.error_occurred_getting_address)+accountNumber,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(!firstTrial){
            getAddress(accountNumbers.get(position));
        }
        firstTrial = false;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
