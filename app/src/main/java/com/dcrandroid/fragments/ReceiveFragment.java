package com.dcrandroid.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.glxn.qrgen.android.MatrixToImageConfig;
import net.glxn.qrgen.android.MatrixToImageWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Macsleven on 28/11/2017.
 */

public class ReceiveFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
    private static final int REQUEST_CODE = 123;
    ImageView imageView;
    LinearLayout ReceiveContainer;
    ArrayAdapter dataAdapter;
    List<String> categories;
    PreferenceUtil preferenceUtil;
    List<Integer> accountNumbers = new ArrayList<>();
    private TextView address;
    private DcrConstants constants;
    private Map<EncodeHintType, Object> qrHints = new HashMap<>();
    private Spinner accountSpinner;
    private Bitmap generatedQR;
    private Uri imageUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getContext() == null) {
            return null;
        }
        constants = DcrConstants.getInstance();
        preferenceUtil = new PreferenceUtil(getContext());
        View rootView = inflater.inflate(R.layout.content_receive, container, false);

        ReceiveContainer = rootView.findViewById(R.id.receive_container);
        imageView = rootView.findViewById(R.id.bitm);
        address = rootView.findViewById(R.id.barcode_address);

        accountSpinner = rootView.findViewById(R.id.recieve_dropdown);
        accountSpinner.setOnItemSelectedListener(this);
        // Spinner Drop down elements
        categories = new ArrayList<>();

        dataAdapter = new ArrayAdapter(getContext(), R.layout.spinner_list_item, categories);
        dataAdapter.setDropDownViewResource(R.layout.dropdown_item_1);
        accountSpinner.setAdapter(dataAdapter);

        address.setOnTouchListener(this);

        imageView.setOnTouchListener(this);

        getActivity().setTitle(getString(R.string.receive));

        qrHints.put(EncodeHintType.MARGIN, 0);
        qrHints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        prepareAccounts();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.receive_page_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.generate_address:
                try {
                    int position = accountSpinner.getSelectedItemPosition();
                    String newAddress = constants.wallet.nextAddress(accountNumbers.get(position));
                    setAddress(newAddress);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.share_qr_code:
                shareImageToApps();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void prepareAccounts() {
        try {
            final ArrayList<Account> accounts = Account.parse(constants.wallet.getAccounts(preferenceUtil.getBoolean(Constants.SPEND_UNCONFIRMED_FUNDS) ? 0 : Constants.REQUIRED_CONFIRMATIONS));
            accountNumbers.clear();
            categories.clear();
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.get(i).getAccountName().trim().equalsIgnoreCase(Constants.IMPORTED)) {
                    continue;
                }
                categories.add(i, accounts.get(i).getAccountName());
                accountNumbers.add(accounts.get(i).getAccountNumber());
            }
            dataAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAddress(String accountAddress) {
        try {
            address.setText(accountAddress);
            QRCodeWriter qrWriter = new QRCodeWriter();
            BitMatrix matrix = qrWriter.encode("decred:" + accountAddress,
                    BarcodeFormat.QR_CODE,
                    300,
                    300,
                    qrHints);

            System.out.println("Image Width: " + imageView.getWidth() + " Image Height: " + imageView.getHeight());

            generatedQR = MatrixToImageWriter.toBitmap(matrix, new MatrixToImageConfig(Color.BLACK, Color.TRANSPARENT));
            imageUri = saveImage(generatedQR);

            //TODO:  Requires a logo
//            Bitmap tempLogo = BitmapFactory.decodeResource(getResources(), R.drawable.decred_logo);
//
//            int logoHeight = (int) (generatedQR.getHeight() * 0.30);
//            int logoWidth = (int) (generatedQR.getWidth() * 0.30);
//
//            Bitmap decredLogo = Bitmap.createScaledBitmap(tempLogo, logoWidth, logoHeight, false);
//
//            System.out.println("Decred Logo Height: "+ decredLogo.getHeight()+ " Width: "+decredLogo.getWidth());
//
//            Bitmap bmOverlay = Bitmap.createBitmap(generatedQR.getWidth(),
//                    generatedQR.getHeight(), generatedQR.getConfig());
//
//            Canvas canvas = new Canvas(bmOverlay);
//            canvas.drawBitmap(generatedQR, new Matrix(), null);
//
//            int leftPosition = (generatedQR.getWidth() / 2) - (logoWidth / 2);
//            int topPosition = (generatedQR.getHeight() / 2) - (logoHeight / 2);
//            canvas.drawBitmap(decredLogo, leftPosition, topPosition, null);
            imageView.setImageBitmap(generatedQR);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ReceiveFragment.this.getContext(), getString(R.string.error_occurred_getting_address), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
            String address = constants.wallet.addressForAccount(accountNumbers.get(position));
            setAddress(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (getContext() == null) {
                    return false;
                }
                Utils.copyToClipboard(getContext(), address.getText().toString(), getString(R.string.address_copy_text));
                return true;
        }
        return false;
    }

    public void shareImageToApps() {
        if (generatedQR != null) {
            Picasso.get().load(imageUri).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent, "Choose an app"));
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });

        }
    }

    private Uri saveImage(Bitmap image) {
        Uri uri = null;
        try {
            File cachePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream stream = new FileOutputStream(cachePath);
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(getActivity().getApplicationContext(), "com.decred.dcrandroid.fileprovider", cachePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;
    }

    public Bitmap getVectorDrawable(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getMinimumWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                break;
        }
    }
}
